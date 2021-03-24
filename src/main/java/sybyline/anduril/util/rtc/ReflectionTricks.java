package sybyline.anduril.util.rtc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.objectweb.asm.Type;
import net.minecraftforge.coremod.api.ASMAPI;
import sybyline.anduril.common.AndurilCommands;

@SuppressWarnings("unchecked")
public class ReflectionTricks {

	public static final Class<?>[] NO_ARG = {,}, OBJ_ARG = {Object.class,}, STR_ARG = {String.class,}, STROBJ_ARG = {String.class,Object.class,};

	public static Class<?> callingClass() {
		return callingClass(1);
	}

	public static Class<?> callingClass(int depth) {
		try {
			return Class.forName(caller(depth).getClassName());
		} catch(Exception e) {
			e.printStackTrace();
			return Object.class;
		}
	}

	public static String callingMethod() {
		return callingMethod(1);
	}

	public static String callingMethod(int depth) {
		try {
			StackTraceElement element = caller(depth);
			return element.getClassName()+":"+element.getMethodName();
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static StackTraceElement caller() {
		return caller(1);
	}

	public static String callerInfo() {
		StackTraceElement ste = caller(1);
		return ste.getClassName()+":"+ste.getMethodName();
	}

	public static StackTraceElement caller(int depth) {
		return Thread.currentThread().getStackTrace()[4 + depth];
	}

	public static <A extends AccessibleObject> A access(A a) {
		a.setAccessible(true);
		return a;
	}

	public static <T> Class<T> subclass_endingwith(Class<?> parent, String name) {
		for (Class<?> clazz : parent.getDeclaredClasses()) {
			if (clazz.getSimpleName().endsWith(name)) {
				return (Class<T>) clazz;
			}
		}
		return null;
	}

	public static Class<?>[] allTypes(Class<?> clazz) {
		Set<Class<?>> ret = new HashSet<>();
		ret.add(clazz);
		do {
			ret.add(clazz);
			ret.addAll(Arrays.asList(clazz.getInterfaces()));
		} while ((clazz = clazz.getSuperclass()) != null);
		return ret.toArray(new Class<?>[0]);
	}

	public static Method[] allMethods(Class<?> clazz) {
		Map<String, Method> ret = new HashMap<>();
		for (Class<?> type : allTypes(clazz)) try {
			for (Method meth : type.getDeclaredMethods())
				ret.put(meth.getName()+Type.getMethodDescriptor(meth), meth);
		} catch(Exception e) {
			System.err.println(clazz);
			if (AndurilCommands.isDebug()) e.printStackTrace();
		}
		return ret.values().toArray(new Method[0]);
	}

	public static <T> List<T> scrapeDeclaredFields(Class<?> clazz, Object instance, List<T> ret, Predicate<T> filter) {
		for(Field f : clazz.getDeclaredFields()) {
			try {
				T t = (T)f.get(instance);
				if(t != null && filter.test(t)) {
					ret.add(t);
				}
			} catch(Exception e) { }
		}
		return ret;
	}

	public static Field[] allFields(Class<?> clazz) {
		Set<Field> ret = new HashSet<>();
		for (Class<?> type : allTypes(clazz))
			for (Field grass : type.getDeclaredFields())
				ret.add(grass);
		return ret.toArray(new Field[0]);
	}

	public static void printFields(Object instance) {
		System.out.println(instance.getClass().getSimpleName()+":");
		for(Field f : instance.getClass().getDeclaredFields()) {
			try {
				System.out.println("  "+f.getName()+": "+f.get(instance));
			} catch(Exception e) { }
		}
	}

	public static final Method findMethodSRG(Class<?> clazz, Class<?>[] types, String srg) {
		srg = ASMAPI.mapMethod(srg);
		for (Method method : clazz.getDeclaredMethods()) {
			if (srg.equals(method.getName())) {
				if (Arrays.equals(method.getParameterTypes(), types)) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		return null;
	}

	public static final Method findMethod(Class<?> clazz, Class<?>[] types, String... names) {
		for (Method method : clazz.getDeclaredMethods()) {
			for (String name : names) {
				name = ASMAPI.mapMethod(name);
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
				name = ASMAPI.mapMethod(name);
				if (name.equals(method.getName())) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		return null;
	}

	public static final Field findFieldSRG(Class<?> clazz, String srg) {
		srg = ASMAPI.mapField(srg);
		for (Field field : clazz.getDeclaredFields()) {
			if (srg.equals(field.getName())) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}

	public static final Field findField(Class<?> clazz, String... names) {
		for (Field field : clazz.getDeclaredFields()) {
			for (String name : names) {
				name = ASMAPI.mapField(name);
				if (name.equals(field.getName())) {
					field.setAccessible(true);
					return field;
				}
			}
		}
		return null;
	}

	public static final Field findFieldNomap(Class<?> clazz, String... names) {
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

	public static final <T> T getPrivateValue(Class<?> clazz, Object instance, Class<?> type) throws Exception {
		Field field = findFieldTyped(clazz, type);
		if (field != null) {
			return (T) field.get(instance);
		} else {
			return null;
		}
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

	public static final <T> T swapPrivateValue(Class<?> clazz, Object instance, Object value, String... names)
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
		if (clazz == null) return;
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
