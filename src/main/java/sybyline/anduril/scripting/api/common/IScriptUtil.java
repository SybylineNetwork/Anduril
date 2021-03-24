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

	// Logging

	public void debug_info(Object message);

	public void debug(Object message);

	public void log(Object message);

	public void error(Object message);

}
