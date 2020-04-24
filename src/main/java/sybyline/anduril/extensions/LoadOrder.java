package sybyline.anduril.extensions;

import java.util.Set;

import com.google.common.collect.Sets;

public class LoadOrder {

	private static final Set<String> allowedLoads = Sets.newHashSet();
	private static final Set<String> completedLoads = Sets.newHashSet();

	public static boolean chain(String triggered, String allowed) {
		allow(allowed);
		return trigger(triggered);
	}

	public static boolean allow(String load) {
		return allowedLoads.add(load);
	}

	public static boolean trigger(String load, String... required) throws LoadOrderException {
		for(String required_ : required) if(!completedLoads.contains(required_)) throw new LoadOrderException(load, required_);
		return trigger(load);
	}

	public static boolean trigger(String load) throws LoadOrderException {
		if(!allowedLoads.contains(load)) throw new LoadOrderException(load);
		return completedLoads.add(load);
	}

	public static class LoadOrderException extends RuntimeException {

		private static final long serialVersionUID = 2098678653164951863L;

		private LoadOrderException(String load) {
			super("Out of order! " + load + " was loaded before that was allowed.");
		}

		private LoadOrderException(String load, String required) {
			super("Out of order! " + load + " was loaded before " + required);
		}

	}

}
