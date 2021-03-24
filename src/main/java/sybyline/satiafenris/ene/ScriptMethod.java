package sybyline.satiafenris.ene;

import java.lang.reflect.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.function.TriFunction;
import sybyline.anduril.util.rtc.RuntimeTricks;

public interface ScriptMethod<T> {

	public Object implementation_call(Object[] args) throws Exception;

	public default T call(Object... args) throws ScriptRuntimeException {
		try {
			@SuppressWarnings("unchecked")
			T ret = (T) this.implementation_call(args);
			return ret;
		} catch(Exception e) {
			throw new ScriptRuntimeException(e);
		}
	}

	public default <Interface> Interface implement(Class<Interface> clazz) {
		@SuppressWarnings("unchecked")
		Interface ret = (Interface) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ clazz }, (Object thiz, Method method, Object[] args) -> {
			return this.implementation_call(args);
		});
		return ret;
	}

	public default <Err extends Throwable> ScriptMethod<T> printErr(BiFunction<Throwable, Object[], T> catchBlock) {
		return args -> {
			try {
				return this.implementation_call(args);
			} catch(Throwable err) {
				err.printStackTrace();
				return catchBlock.apply(err, args);
			}
		};
	}
	public default <Err extends Throwable> ScriptMethod<T> printErr() {
		return this.printErr((err, args) -> Util.throwSilent(err));
	}

	@SuppressWarnings("unchecked")
	public default <Err extends Throwable> ScriptMethod<T> catching(Class<Err> errClass, BiFunction<Err, Object[], T> catchBlock) {
		return args -> {
			try {
				return this.implementation_call(args);
			} catch(Throwable err) {
				return errClass.isInstance(err)
					? catchBlock.apply((Err)err, args)
					: Util.throwSilent(err);
			}
		};
	}
	public default ScriptMethod<T> catching(BiFunction<Throwable, Object[], T> catchBlock) {
		return catching(Throwable.class, catchBlock);
	}

	public default <Err extends Throwable> ScriptMethod<T> catching0(Class<Err> errClass, Function<Err, T> catchBlock) {
		return catching(errClass, (err, args) -> catchBlock.apply(err));
	}
	public default ScriptMethod<T> catching0(Function<Throwable, T> catchBlock) {
		return catching0(Throwable.class, catchBlock);
	}

	@SuppressWarnings("unchecked")
	public default <Err extends Throwable, A> ScriptMethod<T> catching1(Class<Err> errClass, BiFunction<Err, A, T> catchBlock) {
		return catching(errClass, (err, args) -> catchBlock.apply(err, (A)args[0]));
	}
	public default <A> ScriptMethod<T> catching1(BiFunction<Throwable, A, T> catchBlock) {
		return catching1(Throwable.class, catchBlock);
	}

	@SuppressWarnings("unchecked")
	public default <Err extends Throwable, A, B> ScriptMethod<T> catching2(Class<Err> errClass, TriFunction<Err, A, B, T, Throwable> catchBlock) {
		return catching(errClass, (err, args) -> RuntimeTricks.procrastinate(() -> catchBlock.apply(err, (A)args[0], (B)args[1])));
	}
	public default <A, B> ScriptMethod<T> catching2(TriFunction<Throwable, A, B, T, Throwable> catchBlock) {
		return catching2(Throwable.class, catchBlock);
	}

}
