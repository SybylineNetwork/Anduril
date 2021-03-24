package sybyline.anduril.util.rtc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.annotation.Hacky;

@Hacky("By nature.")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodWrapper<T> {

	public static <T> MethodWrapper<T> of(Class clazz, String... names) {
		try {
			return of(ReflectionTricks.findMethod_notype(clazz, names));
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public static <T> MethodWrapper<T> of(Class clazz, Class[] types, String... names) {
		try {
			return of(ReflectionTricks.findMethod(clazz, types, names));
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public static <T> MethodWrapper<T> of(Method method) {
		try {
			method.setAccessible(true);
			return new MethodWrapper(MethodHandles.lookup().unreflect(method));
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public static <T> MethodWrapper<T> ofSpecial(Class clazz, Class<?> specialCaller, String... names) {
		try {
			return ofSpecial(ReflectionTricks.findMethod_notype(clazz, names), specialCaller);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public static <T> MethodWrapper<T> ofSpecial(Class clazz, Class<?> specialCaller, Class[] types, String... names) {
		try {
			return ofSpecial(ReflectionTricks.findMethod(clazz, types, names), specialCaller);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public static <T> MethodWrapper<T> ofSpecial(Method method, Class<?> specialCaller) {
		try {
			method.setAccessible(true);
			return new MethodWrapper(MethodHandles.lookup().unreflectSpecial(method, specialCaller));
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	private MethodWrapper(MethodHandle m) {
		mh = m;
	}

	private final MethodHandle mh;
	private MethodHandle mhb = null;

	public MethodWrapper<T> without() {
		mhb = null;
		return this;
	}

	public MethodWrapper<T> with(Object o) {
		mhb = mh.bindTo(o);
		return this;
	}

	public T call(Object arg) {
		try {
			return (T) (mhb == null ? mh : mhb).invoke(arg);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T call(Object arg, Object arg2) {
		try {
			return (T) (mhb == null ? mh : mhb).invoke(arg, arg2);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T call(Object arg, Object arg2, Object arg3) {
		try {
			return (T) (mhb == null ? mh : mhb).invoke(arg, arg2, arg3);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T call(Object arg, Object arg2, Object arg3, Object arg4) {
		try {
			return (T) (mhb == null ? mh : mhb).invoke(arg, arg2, arg3, arg4);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T call(Object arg, Object arg2, Object arg3, Object arg4, Object arg5) {
		try {
			return (T) (mhb == null ? mh : mhb).invoke(arg, arg2, arg3, arg4, arg5);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T call(Object... args) {
		try {
			return (T) (mhb == null ? mh : mhb).invokeWithArguments(args);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object arg) {
		try {
			return (T) mh.invoke(arg);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object arg, Object arg2) {
		try {
			return (T) mh.invoke(arg, arg2);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object arg, Object arg2, Object arg3) {
		try {
			return (T) mh.invoke(arg, arg2, arg3);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object arg, Object arg2, Object arg3, Object arg4) {
		try {
			return (T) mh.invoke(arg, arg2, arg3, arg4);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object arg, Object arg2, Object arg3, Object arg4, Object arg5) {
		try {
			return (T) mh.invoke(arg, arg2, arg3, arg4, arg5);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T calls(Object... args) {
		try {
			return (T) mh.invokeWithArguments(args);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

}
