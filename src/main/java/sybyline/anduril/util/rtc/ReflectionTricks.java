package sybyline.anduril.util.rtc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.objectweb.asm.Type;

@SuppressWarnings("unchecked")
public class ReflectionTricks {

	public static <T> Class<T> subclass_endingwith(Class<?> parent, String name) {
		for (Class<?> clazz : parent.getDeclaredClasses()) {
			if (clazz.getSimpleName().endsWith(name)) {
				return (Class<T>) clazz;
			}
		}
		return null;
	}

	public static final Method findMethod(Class<?> clazz, Class<?>[] types, String... names) {
		for (Method method : clazz.getDeclaredMethods()) {
			for (String name : names) {
				if (name.equals(method.getName())) {
					if (Arrays.equals(method.getParameterTypes(), types)) {
						method.setAccessible(true);
						return method;
					}
				}
			}
		}
		return null;
	}

	public static final Method findMethod_notype(Class<?> clazz, String... names) {
		for (Method method : clazz.getDeclaredMethods()) {
			for (String name : names) {
				if (name.equals(method.getName())) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		return null;
	}

	public static final Field findField(Class<?> clazz, String... names) {
		for (Field field : clazz.getDeclaredFields()) {
			for (String name : names) {
				if (name.equals(field.getName())) {
					field.setAccessible(true);
					return field;
				}
			}
		}
		return null;
	}

	public static final Field findFieldTyped(Class<?> clazz, Class<?> type) {
		for (Field field : clazz.getDeclaredFields()) {
			if (type.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}

	public static final <T> T getPrivateValue(Class<?> clazz, Object instance, String... names) throws Exception {
		Field field = findField(clazz, names);
		if (field != null) {
			return (T) field.get(instance);
		} else {
			return null;
		}
	}

	public static final void setPrivateValue(Class<?> clazz, Object instance, Object value, String... names)
			throws Exception {
		Field field = findField(clazz, names);
		if (field != null) {
			field.set(instance, value);
		}
	}

	public static final <T> T atomicPrivateValue(Class<?> clazz, Object instance, Object value, String... names)
			throws Exception {
		Field field = findField(clazz, names);
		if (field != null) {
			T ret = (T) field.get(instance);
			field.set(instance, value);
			return ret;
		} else {
			return null;
		}
	}

	public static void debugDeclared(Class<?> clazz) {
		debugDeclaredFields(clazz);
		debugDeclaredMethods(clazz);
	}

	public static void debugDeclaredMethods(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		System.out.println("Debug of declared methods in " + clazz.getName());
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			System.out.println(" " + i + ": " + m.getName()
					+ Type.getMethodDescriptor(Type.getType(m.getReturnType()), Type.getArgumentTypes(m)));
		}
		System.out.println("End of debug");
	}

	public static void debugDeclaredFields(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		System.out.println("Debug of declared methods in " + clazz.getName());
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			System.out.println(" " + i + ": " + f.getName() + Type.getDescriptor(f.getType()));
		}
		System.out.println("End of debug");
	}

}
