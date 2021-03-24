package sybyline.anduril.util.function;

import java.util.function.*;

public final class Noop {

	private Noop() {}

	public static final Runnable run() {
		return () -> {};
	}

	public static final <T> Consumer<T> accept() {
		return (one) -> {};
	}

	public static final DoubleConsumer acceptDouble() {
		return (one) -> {};
	}

	public static final <T, U> BiConsumer<T, U> biaccept() {
		return (one, two) -> {};
	}

	public static final <T, U, V> TriConsumer<T, U, V> triaccept() {
		return (one, two, three) -> {};
	}

	public static final VarConsumer varaccept() {
		return (args) -> {};
	}

	public static <T> Supplier<T> constant(T value) {
		return () -> value;
	}

	public static <T> Predicate<T> truth() {
		return __ -> true;
	}

	public static <T> Predicate<T> falsity() {
		return __ -> false;
	}

}
