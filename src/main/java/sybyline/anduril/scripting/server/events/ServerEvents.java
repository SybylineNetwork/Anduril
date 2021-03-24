package sybyline.anduril.scripting.server.events;

import java.util.Map;
import com.google.common.collect.Maps;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.server.ServerScripting;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.FileReloadListener;

public class ServerEvents {

	public ServerEvents(ServerScripting serverScripting) {
		this.serverScripting = serverScripting;
		serverScripting.resources.addReloadListener(jsCommands);
	}

	private final ServerScripting serverScripting;
	public final FileReloadListener<ScriptEventWrapper> jsCommands = new FileReloadListener<ScriptEventWrapper>(ScriptEventWrapper.FORMAT, Util.ANDURIL + "/events", this::reloadJS);
	public final Map<ResourceLocation, ScriptEventWrapper> wrappers = Maps.newHashMap();

	private void reloadJS(Map<ResourceLocation, ScriptEventWrapper> data, IResourceManager resources, IProfiler profiler) {
		profiler.startSection("sybyline_events_register");
		cleanup();
		serverScripting.queueEvents(() -> data.forEach(this::registerJS));
		profiler.endSection();
	}

	private void registerJS(ResourceLocation location, ScriptEventWrapper event) {
		try {
			CommonScripting.INSTANCE.println_debug("Building script event: " + location);
			event.setupWithContext(null);
			wrappers.put(location, event);
		} catch(Exception e) {
			CommonScripting.LOGGER.error("A script command errored during initialization: ", e);
		}
	}

	public void cleanup() {
		wrappers.values().forEach(ScriptEventWrapper::unlisten);
		wrappers.clear();
	}

}
