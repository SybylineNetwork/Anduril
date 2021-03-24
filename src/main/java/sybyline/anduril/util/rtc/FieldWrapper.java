package sybyline.anduril.util.rtc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import sybyline.anduril.util.Util;
import sybyline.anduril.util.annotation.Doc;
import sybyline.anduril.util.annotation.Hacky;

@Hacky("By nature.")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FieldWrapper<T> {

	public static <T> FieldWrapper<T> of(Class<?> clazz, int index, @Doc("So we remember") String intendedName) {
		return of(clazz.getDeclaredFields()[index]);
	}

	public static <T> FieldWrapper<T> of(Class<?> clazz, String... names) {
		return of(ReflectionTricks.findField(clazz, names));
	}

	public static <T> FieldWrapper<T> of(Field f) {
		try {
			return new FieldWrapper(f);
		} catch (Throwable t) {
			return Util.throwSilent(t);
		}
	}

	private FieldWrapper(Field f) throws Exception {
		f.setAccessible(true);
		gets = MethodHandles.lookup().unreflectGetter(f);
		sets = MethodHandles.lookup().unreflectSetter(f);
		isStatic = Modifier.isStatic(f.getModifiers());
	}

	private final MethodHandle gets;
	private final MethodHandle sets;
	private MethodHandle getsb = null;
	private MethodHandle setsb = null;
	private Supplier<Object> supplier = null;
	private boolean isStatic = false;

	public FieldWrapper<T> asStatic() {
		isStatic = true;
		return this;
	}

	public FieldWrapper<T> asVirtual() {
		isStatic = false;
		return this;
	}

	public FieldWrapper<T> with(Object o) {
		getsb = gets.bindTo(o);
		setsb = sets.bindTo(o);
		return this;
	}

	public T withGet(Object o) {
		try {
			return (T) gets.bindTo(o).invoke();
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public FieldWrapper<T> with(Supplier<Object> os) {
		supplier = os;
		return this;
	}

	public FieldWrapper<T> without() {
		supplier = null;
		getsb = null;
		setsb = null;
		return this;
	}

	public T set(T thing) {
		try {
			if (isStatic) {
				sets.invoke(thing);
			} else if (supplier == null) {
				setsb.invoke(thing);
			} else {
				sets.invoke(supplier.get(), thing);
			}
			return thing;
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T get() {
		try {
			if (isStatic) {
				return (T) gets.invoke();
			} else if (supplier == null) {
				return (T) getsb.invoke();
			} else {
				return (T) gets.invoke(supplier.get());
			}
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T set(Object o, T thing) {
		try {
			sets.invoke(o, thing);
			return thing;
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

	public T get(Object o) {
		try {
			return (T) gets.invoke(o);
		} catch(Throwable t) {
			return Util.throwSilent(t);
		}
	}

}
