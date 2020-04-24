package sybyline.anduril.util.rtc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import sybyline.anduril.util.Hacky;

@Hacky("By nature.")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodWrapper<T> {

	public static <T> MethodWrapper<T> of(Class clazz, String... names) {
		return RuntimeTricks.procrastinate(() -> new MethodWrapper(ReflectionTricks.findMethod_notype(clazz, names)));
	}

	public static <T> MethodWrapper<T> of(Class clazz, Class[] types, String... names) {
		return RuntimeTricks.procrastinate(() -> new MethodWrapper(ReflectionTricks.findMethod(clazz, types, names)));
	}

	public static <T> MethodWrapper<T> of(Method method) {
		return RuntimeTricks.procrastinate(() -> {
			method.setAccessible(true);
			return new MethodWrapper(method);
		});
	}

	private MethodWrapper(Method m) {
		mh = RuntimeTricks.procrastinate(() -> MethodHandles.lookup().unreflect(m));
	}

	private final MethodHandle mh;
	private MethodHandle mhb = null;

	public MethodWrapper<T> without() {
		mhb = null;
		return this;
	}

	public MethodWrapper<T> with(Object o) {
		mhb = RuntimeTricks.procrastinate(() -> mh.bindTo(o));
		return this;
	}

	public T call(Object arg) {
		return (T) RuntimeTricks.procrastinate(() -> (mhb == null ? mh : mhb).invoke(arg));
	}

	public T call(Object arg, Object arg2) {
		return (T) RuntimeTricks.procrastinate(() -> (mhb == null ? mh : mhb).invoke(arg, arg2));
	}

	public T call(Object arg, Object arg2, Object arg3) {
		return (T) RuntimeTricks.procrastinate(() -> (mhb == null ? mh : mhb).invoke(arg, arg2, arg3));
	}

	public T call(Object arg, Object arg2, Object arg3, Object arg4) {
		return (T) RuntimeTricks.procrastinate(() -> (mhb == null ? mh : mhb).invoke(arg, arg2, arg3, arg4));
	}

	public T call(Object arg, Object arg2, Object arg3, Object arg4, Object arg5) {
		return (T) RuntimeTricks.procrastinate(() -> (mhb == null ? mh : mhb).invoke(arg, arg2, arg3, arg4, arg5));
	}

	public T calls(Object arg) {
		return (T) RuntimeTricks.procrastinate(() -> mh.invoke(arg));
	}

	public T calls(Object arg, Object arg2) {
		return (T) RuntimeTricks.procrastinate(() -> mh.invoke(arg, arg2));
	}

	public T calls(Object arg, Object arg2, Object arg3) {
		return (T) RuntimeTricks.procrastinate(() -> mh.invoke(arg, arg2, arg3));
	}

	public T calls(Object arg, Object arg2, Object arg3, Object arg4) {
		return (T) RuntimeTricks.procrastinate(() -> mh.invoke(arg, arg2, arg3, arg4));
	}

	public T calls(Object arg, Object arg2, Object arg3, Object arg4, Object arg5) {
		return (T) RuntimeTricks.procrastinate(() -> mh.invoke(arg, arg2, arg3, arg4, arg5));
	}

}
