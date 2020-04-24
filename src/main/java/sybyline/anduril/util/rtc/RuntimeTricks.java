package sybyline.anduril.util.rtc;

import java.lang.reflect.*;
import java.util.function.Supplier;

import sybyline.anduril.util.Hacky;

@Hacky("By nature.")
@SuppressWarnings("unchecked")
public class RuntimeTricks {

	public static final <T> T procrastinate(Failable<T> task) {
		try {
			return task.fail();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static final <T> Supplier<T> procrastinate_supplier(Failable<T> task) {
		return () -> {
			try {
				return task.fail();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static final sun.misc.Unsafe u = procrastinate(() -> ReflectionTricks.getPrivateValue(sun.misc.Unsafe.class, null, "theUnsafe"));

	public static final <T> T makeUnsafely(Class<T> type) {
		return procrastinate(() -> (T) u.allocateInstance(type));
	}

	public static final <T> T makeReflectivelyOrUnsafely(Class<T> type) {
		try {
			return (T) type.newInstance();
		} catch (Exception e) {
			return makeUnsafely(type);
		}
	}

	public static final <T> T shallowCopy(Class<T> clazz, T from) {
		T to = makeReflectivelyOrUnsafely(clazz);
		shallowCopy(clazz, from, to);
		return to;
	}

	public static final <T> void shallowCopy(Class<T> clazz, T from, T to) {
		for (Field f : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				try {
					f.set(to, f.get(from));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final long typeOffsetInts = 8L;

	public static final <T> RuntimeTypeChanger<T> getTypeChangerInts(Class<T> clazz) {
		final int jvmTypeHandle = u.getInt(makeUnsafely(clazz), typeOffsetInts);
		return object -> {
			u.putInt(object, typeOffsetInts, jvmTypeHandle);
			return (T) object;
		};
	}

	private static final long typeOffsetLongs = 4L;

	public static final <T> RuntimeTypeChanger<T> getTypeChangerLongs(Class<T> clazz) {
		final long jvmTypeHandle = u.getInt(makeUnsafely(clazz), typeOffsetLongs);
		return object -> {
			u.putLong(object, typeOffsetLongs, jvmTypeHandle);
			return (T) object;
		};
	}

}
