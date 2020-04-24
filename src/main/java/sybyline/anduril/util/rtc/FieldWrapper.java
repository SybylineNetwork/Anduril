package sybyline.anduril.util.rtc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import sybyline.anduril.util.Doc;
import sybyline.anduril.util.Hacky;

@Hacky("By nature.")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FieldWrapper<T> {

	public static <T> FieldWrapper<T> of(Class<?> clazz, int index, @Doc("So we remember") String intendedName) {
		return RuntimeTricks.procrastinate(() -> new FieldWrapper(clazz.getDeclaredFields()[index]));
	}

	public static <T> FieldWrapper<T> of(Class<?> clazz, String... names) {
		return RuntimeTricks.procrastinate(() -> new FieldWrapper(ReflectionTricks.findField(clazz, names)));
	}

	public static <T> FieldWrapper<T> of(Field f) {
		return RuntimeTricks.procrastinate(() -> {
			return new FieldWrapper(f);
		});
	}

	private FieldWrapper(Field f) throws Exception {
		f.setAccessible(true);
		gets = MethodHandles.lookup().unreflectGetter(f);
		sets = MethodHandles.lookup().unreflectSetter(f);
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
		return (T) RuntimeTricks.procrastinate(() -> gets.bindTo(o).invoke());
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
		if (isStatic) {
			RuntimeTricks.procrastinate(() -> sets.invoke(thing));
		} else if (supplier == null) {
			RuntimeTricks.procrastinate(() -> setsb.invoke(thing));
		} else {
			RuntimeTricks.procrastinate(() -> sets.invoke(supplier.get(), thing));
		}
		return thing;
	}

	public T get() {
		if (isStatic) {
			return (T) RuntimeTricks.procrastinate(() -> gets.invoke());
		} else if (supplier == null) {
			return (T) RuntimeTricks.procrastinate(() -> getsb.invoke());
		} else {
			return (T) RuntimeTricks.procrastinate(() -> gets.invoke(supplier.get()));
		}
	}

	public T set(Object o, T thing) {
		RuntimeTricks.procrastinate(() -> sets.invoke(o, thing));
		return thing;
	}

	public T get(Object o) {
		return (T) RuntimeTricks.procrastinate(() -> gets.invoke(o));
	}

}
