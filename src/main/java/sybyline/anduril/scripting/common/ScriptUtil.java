package sybyline.anduril.scripting.common;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import sybyline.anduril.scripting.api.common.*;
import sybyline.satiafenris.ene.Convert;

public final class ScriptUtil implements IScriptUtil {

	private ScriptUtil() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static final ScriptUtil INSTANCE = new ScriptUtil();

	// Async

	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final List<PendingTask> pending = Lists.newArrayList();
	private final List<PendingTask> pending_add = Lists.newArrayList();
	private final Semaphore semaphore = new Semaphore(1);
	public BiFunction<String, Object[], String> i18n = (key, args) -> key;

	public void call_async(Callable<Runnable> taskReturnsSync) {
		this.tryAdd(PendingTask.of(exec.submit(taskReturnsSync)));
		
	}

	public void call_sync(Runnable taskSync, int ticks) {
		this.tryAdd(PendingTask.of(taskSync, ticks));
	}

	// Text

	public String color(String text) {
		return escapeReplace(text, '&', '\u00a7');
	}

	public String uncolor(String text) {
		return escapeReplace(text, '\u00a7', '&');
	}

	public void debug_info(Object message) {
		CommonScripting.INSTANCE.println_debug(String.valueOf(message));
	}

	public void debug(Object message) {
		System.out.println(Convert.js_string_of(message));
	}

	public void log(Object message) {
		System.out.println(message);
	}

	public void error(Object message) {
		System.err.println(message);
	}

	// Internal

	private String escapeReplace(String text, char from, char to) {
		StringBuilder string = new StringBuilder();
		StringReader reader = new StringReader(text);
		while (reader.canRead()) {
			char chr = reader.read();
			if (chr == from) {
				string.append(to);
				if (reader.canRead()) {
					string.append(reader.read());
				}
				continue;
			}
			string.append(chr);
		}
		return string.toString();
	}

	private void tryAdd(PendingTask task) {
		if (semaphore.tryAcquire()) {
			pending.add(task);
			semaphore.release();
		} else {
			synchronized (pending_add) {
				pending_add.add(task);
			}
		}
	}

	private void completeTasks() {
		try {
			semaphore.acquireUninterruptibly();
			if (pending.isEmpty()) return;
			pending.removeIf(future -> {
				boolean remove;
				if (remove = future.isDone()) {
					Runnable sync = null;
					try {
						sync = future.get();
					} catch(Exception e) {
						CommonScripting.LOGGER.error("A script async task failed:", e);
					}
					if (sync != null) try {
						sync.run();
					} catch(Exception e) {
						CommonScripting.LOGGER.error("A script async task's finalization failed:", e);
					}
				} else {
					future.tick();
				}
				return remove;
			});
		} finally {
			synchronized (pending_add) {
				pending.addAll(pending_add);
				pending_add.clear();
			}
			if (semaphore.availablePermits() == 0) {
				semaphore.release();
			}
		}
	}

	// Event

	@SubscribeEvent
	void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;
		if (event.world.isRemote) return;
		if (event.world.dimension.getType() != DimensionType.OVERWORLD) return;
		completeTasks();
	}

}
