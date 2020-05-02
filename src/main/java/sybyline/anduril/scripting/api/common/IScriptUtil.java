package sybyline.anduril.scripting.api.common;

import java.util.concurrent.Callable;

public interface IScriptUtil {

	public void call_async(Callable<Runnable> taskReturnsSync);

	public default void call_sync(Runnable taskSync) {
		call_sync(taskSync, 0);
	}

	public void call_sync(Runnable taskSync, int ticks);

	// Text

	public String color(String text);

	public String uncolor(String text);

	public String translate(String key, Object... parameters);

	public String format(String key, Object... parameters);

	// Logging

	public void debug_info(Object message);

	public void debug(Object message);

	public void log(Object message);

	public void error(Object message);

	// Objects

	public IMCResource new_resource(String domainpath);

	public IMCResource new_resource(String domain, String path);

	public default IMCItem new_item(String item) {
		return this.new_item(item, 1);
	}

	public IMCItem new_item(String item, int size);

}
