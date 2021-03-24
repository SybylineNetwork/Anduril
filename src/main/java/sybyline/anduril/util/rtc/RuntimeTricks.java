package sybyline.anduril.util.rtc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.*;
import sun.misc.Unsafe;
import sybyline.anduril.util.Util;

@SuppressWarnings({ "unchecked", "restriction" })
public class RuntimeTricks {

	public static final <T> T procrastinate(Failable<T> task) {
		try {
			return task.fail();
		} catch (Throwable e) {
			return Util.throwSilent(e);
		}
	}

	public static final <T> Supplier<T> procrastinate_supplier(Failable<T> task) {
		return () -> procrastinate(task);
	}

	private static final Unsafe u = procrastinate(() -> ReflectionTricks.getPrivateValue(Unsafe.class, null, Unsafe.class));

	public static Unsafe getUnsafe() {
		return u;
	}

	public static long fieldOffset(Class<?> clazz, String declared) {
		try {
			Field f = clazz.getDeclaredField(declared);
			return Modifier.isStatic(f.getModifiers())
				? getUnsafe().staticFieldOffset(f)
				: getUnsafe().objectFieldOffset(f);
		} catch(Exception e) {
			throw new AssertionError(e);
		}
	}

	public static Class<?> defineClass(boolean unsafe, String clazzNameJava, byte[] bytes, ClassLoader classLoader) {
		return Impl.defineClass(unsafe, clazzNameJava, bytes, classLoader);
	}

	public static final void checkInterrupt() {
		if (Thread.interrupted()) {
			throwInterrupt();
		}
	}

	public static final <T> T throwInterrupt() {
		return rethrow(new InterruptedException());
	}

	public static final <T> T rethrow(Throwable t) {
		u.throwException(t);
		return null;
	}

	public static final RuntimeException rethrowRuntime(Throwable t) {
		u.throwException(t);
		return null;
	}

	public static final void replaceInterface(Class<?> clazz, Class<?> from, Class<?> to) {
		
	}

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

	public static <T> Supplier<T> createConstructorSupplier(Class<T> clazz, ConstructionStrategy strat) {
		if (clazz == null)
			throw new NullPointerException("clazz");
		return strat.create(clazz);
	}

	public enum ConstructionStrategy {
		NEWINSTANCE { @Override <T> Supplier<T> create(Class<T> clazz) {
			return RuntimeTricks.procrastinate_supplier(() -> clazz.newInstance());
		}},
		REFLECTION { @Override <T> Supplier<T> create(Class<T> clazz) {
			try {
				Constructor<T> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				return RuntimeTricks.procrastinate_supplier(() -> constructor.newInstance());
			} catch (Exception e) {
				return RuntimeTricks.rethrow(e);
			}
		}},
		METHODHANDLE { @Override <T> Supplier<T> create(Class<T> clazz) {
			try {
				Constructor<T> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				MethodHandle mh = MethodHandles.lookup().unreflectConstructor(constructor);
				return RuntimeTricks.procrastinate_supplier(() -> (T)mh.invoke());
			} catch (Exception e) {
				return RuntimeTricks.rethrow(e);
			}
		}},
		@Deprecated
		ASM { @Override <T> Supplier<T> create(Class<T> clazz) {
			throw new UnsupportedOperationException(clazz.getName());
		}},
		UNSAFE { @Override <T> Supplier<T> create(Class<T> clazz) {
			return () -> RuntimeTricks.makeUnsafely(clazz);
		}},
		CASCADE { @Override <T> Supplier<T> create(Class<T> clazz) {
			for (ConstructionStrategy strat : values()) try {
				if (strat != this) return strat.create(clazz);
			} catch(Throwable t) {}
			throw new RuntimeException(clazz.getName());
		}},
		;
		abstract <T> Supplier<T> create(Class<T> clazz);
	}

	public static final <T> T shallowCopy(T from) {
		if (from == null) return null;
		Class<T> clazz = (Class<T>)from.getClass();
		T to = makeReflectivelyOrUnsafely(clazz);
		shallowCopy(clazz, from, to);
		return to;
	}

	public static final <T> void shallowCopy(Class<?> clazz, T from, T to) {
		do {
			for (Field f : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers())) {
					try {
						f.setAccessible(true);
						f.set(to, f.get(from));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} while ((clazz = clazz.getSuperclass()) != null);
	}

	public static final <T> RuntimeTypeChanger<T> getTypeChangerInstance(T instance) {
		return LAYOUT.of(instance);
	}

	public static final <T> RuntimeTypeChanger<T> getTypeChanger(Class<T> clazz) {
		if (clazz == null)
			return RuntimeTypeChanger.castingUnchecked();
		return getTypeChangerInstance(makeUnsafely(clazz));
	}

	public static final RuntimeTypeChanger<?> getTypeChangerLazy(Function<Class<?>, Class<?>> classProducer) {
		Function<Class<?>, RuntimeTypeChanger<?>> lazyFactory = classProducer.andThen(RuntimeTricks::getTypeChanger);
		Map<Class<?>, RuntimeTypeChanger<?>> lazyMap = new HashMap<>();
		return object -> lazyMap.computeIfAbsent(object.getClass(), lazyFactory).changeType(object);
	}

	public static final MethodHandle RTC = procrastinate(() -> MethodHandles.lookup().findVirtual(RuntimeTypeChanger.class, "changeType", MethodType.methodType(Object.class, Object.class)));
	public static MethodHandle getTypeChangerHandle(Class<?> changing) {
		return RTC.bindTo(getTypeChanger(changing)).asType(MethodType.methodType(changing, Object.class));
	}

	/**
	@param you nOooO u cant just instantiate this ```uninstantiable placeholder class to
		hold a reference to the {@code Class} object representing the Java keyword void```
	@param me ha ha ha unsafe go brrrrrrrr
	*/
	public static final Void VOID = makeUnsafely(Void.class);

	public static final boolean IS_JVM_64;

	static {
		boolean is64 = false;
		for (String s : new String[] { "sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch" }) {
			String s1 = System.getProperty(s);
			is64 |= (s1 != null && s1.contains("64"));
		}
		IS_JVM_64 = is64;
	}

	private enum MemoryLayout {
		LAYOUT_32 () { @Override protected <T> RuntimeTypeChanger<T> of(T thing) {
			final int jvmTypeHandle = u.getInt(thing, typeOffset32);
			return object -> {
				u.putInt(object, typeOffset32, jvmTypeHandle);
				return (T) object;
			};
		}},
		LAYOUT_64 () { @Override protected <T> RuntimeTypeChanger<T> of(T thing) {
			final long jvmTypeHandle = u.getLong(thing, typeOffset64);
			return object -> {
				u.putLong(object, typeOffset64, jvmTypeHandle);
				return (T) object;
			};
		}},
		LAYOUT_64_CO () { @Override protected <T> RuntimeTypeChanger<T> of(T thing) {
			final int jvmTypeHandle = u.getInt(thing, typeOffset64);
			return object -> {
				u.putInt(object, typeOffset64, jvmTypeHandle);
				return (T) object;
			};
		}},
		;

		protected abstract <T> RuntimeTypeChanger<T> of(T thing);

		private static final long typeOffset32 = 4L;
		private static final long typeOffset64 = 8L;

	}

	private static final MemoryLayout LAYOUT;

	static {
		if (!IS_JVM_64) {
			LAYOUT = MemoryLayout.LAYOUT_32;
		} else {
			if (Boolean.getBoolean("sybyline.vm.nocompressedoops")) {
				LAYOUT = MemoryLayout.LAYOUT_64;
			} else {
				LAYOUT = MemoryLayout.LAYOUT_64_CO;
			}
		}
		System.out.println("Memory layout is " + LAYOUT.name());
	}

}

class Impl {

    static Class<?> defineClass(boolean unsafe, String name, byte[] bytes, ClassLoader classLoader) {
    	Class<?> ret = 
//    	unsafe && classLoader != null
//			? theUnsafe.defineClass(name.replace('/', '.'), bytes, 0, bytes.length, classLoader, domain)
//			: generator.defineCustomClass(name, bytes, 0, bytes.length, domain);
    	generator.compileAndLoad(name, bytes);
		return ret;
	}
	static final ProtectionDomain domain;
	static final GeneratedClassLoader generator = new GeneratedClassLoader();
    static {
    	URL url;
    	try {
    		url = new URL("https://github.com/SybylineNetwork");
    	} catch(Exception e) {
    		url = Util.throwSilent(e);
    	}
    	domain = new ProtectionDomain(new CodeSource(url, new CodeSigner[0]), null);
    }
}

class GeneratedClassLoader extends ClassLoader {

	private final Map<String, byte[]> classData = new HashMap<>();

	Class<?> defineCustomClass(String name, byte[] bytes, int offset, int length, ProtectionDomain domain) {
		bytes = Arrays.copyOfRange(bytes, offset, offset + length);
		classData.put(name, bytes);
		try {
			return Class.forName(name, true, null);
//			return this.loadClass(name.replace('/', '.'), true);
		} catch (ClassNotFoundException e) {
			return Util.throwSilent(e);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] code = classData.get(name.replace('.', '/'));
        if (code == null)
        	throw new ClassNotFoundException(name);
        return defineClass(name.replace('/', '.'), code, 0, code.length, Impl.domain);
	}

	Class<?> compileAndLoad(String name, byte[] bytes) {
	    Map<String,byte[]> compiled = new HashMap<>();
	    compiled.put(name.replace('/', '.'), bytes);
	    ClassLoader l = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
	        @Override
	        protected Class<?> findClass(String n) throws ClassNotFoundException {
	            byte[] code = compiled.get(n);
	            if (code == null) {
	            	System.out.println(compiled);
	            	throw new ClassNotFoundException(n);
	            }
	            return defineClass(n, code, 0, code.length);
	        }
	    };
	    try {
	        return Class.forName(name.replace('/', '.'), true, l);
	    } catch (ClassNotFoundException ex) {
	    	return Util.throwSilent(ex);
	    }
	}

}
