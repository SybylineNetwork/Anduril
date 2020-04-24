package sybyline.anduril.extensions.event;

import net.minecraftforge.eventbus.api.*;
import sybyline.anduril.util.Util;

public class SybylineExtensions {

	public static final IEventBus SUBMOD_BUS = BusBuilder.builder().setExceptionHandler(SybylineExtensions::handleExceptions).startShutdown().build();

	public static void start() {
		SUBMOD_BUS.start();
	}

	public static boolean post(SybylineEvent event) {
		try {
			return SUBMOD_BUS.post(event);
		} catch (Throwable e) {
			return false;
		}
	}

	public static <E extends SybylineEvent> E postEvent(E event) {
		try {
			SUBMOD_BUS.post(event);
		} catch (Throwable e) {
			// NOOP
		}
		return event;
	}

	private static void handleExceptions(IEventBus bus, Event event, IEventListener[] listeners, int i, Throwable exception) {
		Util.LOG.error("An error occured whilst firing a sybyline event:");
		exception.printStackTrace();
	}

}
