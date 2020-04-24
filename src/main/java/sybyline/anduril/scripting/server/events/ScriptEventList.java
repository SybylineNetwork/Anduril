package sybyline.anduril.scripting.server.events;

import java.util.List;
import java.util.function.*;

import com.google.common.collect.Lists;

public final class ScriptEventList<T> {

	private final List<T> handlers = Lists.newArrayList();

	public void add(T handler) {
		handlers.add(handler);
	}

	public boolean shouldRun() {
		return !handlers.isEmpty();
	}

	public void run(Consumer<T> action) {
		handlers.removeIf(consumer -> {
			try {
				action.accept(consumer);
				return false;
			} catch(Exception e) {
				e.printStackTrace();
				return true;
			}
		});
	}

}
