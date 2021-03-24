package sybyline.anduril.util.rtc;

import java.util.function.*;
import sybyline.anduril.util.function.*;

@FunctionalInterface
public interface RuntimeTypeChanger<T> {

	public T changeType(Object object);

	public default RuntimeTypeChanger<T> then(Consumer<T> consumer) {
		return obj -> {
			T ret = changeType(obj);
			consumer.accept(ret);
			return ret;
		};
	}

	@SuppressWarnings("unchecked")
	public default <TT extends T> TT changeTypeGenericCasting(Object object) {
		return (TT) changeType(object);
	}

	public default Supplier<T> wrapNoArg(Supplier<?> originalConstructor) {
		return () -> changeType(originalConstructor.get());
	}

	public default <A> Function<A, T> wrapOneArg(Function<A, ?> originalConstructor) {
		return (a) -> changeType(originalConstructor.apply(a));
	}

	public default <A, B> BiFunction<A, B, T> wrapTwoArg(BiFunction<A, B, ?> originalConstructor) {
		return (a, b) -> changeType(originalConstructor.apply(a, b));
	}

	public default <A, B, C, Ex extends Throwable> TriFunction<A, B, C, T, Ex> wrapThreeArg(TriFunction<A, B, C, ?, Ex> originalConstructor) {
		return (a, b, c) -> changeType(originalConstructor.apply(a, b, c));
	}

	public default <Ex extends Throwable> ThrowingVarFunction<T, Ex> wrapVarArg(ThrowingVarFunction<?, Ex> originalConstructor) {
		return (args) -> changeType(originalConstructor.apply(args));
	}

	@SuppressWarnings("unchecked")
	public static <T> RuntimeTypeChanger<T> castingUnchecked() {
		return object -> (T)object;
	}

	public static <T> RuntimeTypeChanger<T> casting(Class<T> clazz) {
		return clazz::cast;
	}

}
