package sybyline.satiafenris.ene;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.*;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sybyline.anduril.util.function.*;

/**
 * Encapsulation for scripting, allowing for more intuitive handling on the java side.
 * Has a few built-in binding functions to ease the use of method references.
 * Note that only encapsulating references to functions can be obtained.
 */
public interface Script {

	/**
	 * Public common logger object.
	 */
	public static final Logger LOG = LogManager.getLogger("EneScript");

	/**
	 * Script instances have engine-context-level functions: {@code use(String)},
	 * which emulates a java {@code import} keyword.
	 * Ex.: {@code use("java.util.ArrayList")}, {@code use("java.util.ArrayList", "JavaList")}
	 * @return A new Nashorn Script instance.
	 */
	public static Script nashorn() {
		return AbstractScript.javascript(false);
	}

	/**
	 * Script instances have two engine-context-level functions: {@code use(String)},
	 * which emulates a java {@code import} keyword.
	 * Ex.: {@code use("java.util.ArrayList")}, {@code use("java.util.ArrayList", "JavaList")}
	 * @return A new GraalJS Script instance, or a Nashorn Script instance if the environment
	 * does not support Graal.
	 */
	public static Script graalOrNashorn() {
		return AbstractScript.javascript(true);
	}

	/**
	 * @return A new Java script
	 */
	public static Script java() {
		return AbstractScript.java();
	}

	/**
	 * Forces the ClassFilter to be enabled
	 * @return this
	 */
	public Script strict();

	public default boolean isJS() {
		return true;
	}

	public default Script setInclusions(Function<String, Stream<String>> include) {
		return this;
	}

	/**
	 * Allow the script to reference and possibly instantiate the types specified.
	 * Ex.: {@code script.allowClasses(ArrayList.class, "java.util.Set");}
	 * Allows packages, like so: {@code script.allowClasses("java.math.*");}
	 * The script calls {@code Java.type("package.sub.Name")} or equivalent
	 * @param classes The types to allow, either a Class or a String
	 */
	public void allowClasses(Object... classes);

	/**
	 * Binds a Java object to the script instance.
	 * Same as {@code Script.set(String, T)}
	 * @param name The name of the variable
	 * @param object The object to be bound
	 */
	public default <T> void bind(String name, T object) {
		this.set(name, object);
	}

	/**
	 * Unbinds a Java object from the script instance
	 * @param name The name of the variable to be released
	 */
	public void unbind(String name);

	/**
	 * Sets a variable at the script context level
	 * @param name The name of the variable to be set
	 * @param object The new value of the variable
	 */
	public <T> void set(String name, T object);

	/**
	 * Gets a variable at the script context level
	 * @param name The name of the variable to be retrieved
	 * @return The value of the variable
	 */
	public <T> T get(String name) throws ScriptRuntimeException;

	/**
	 * Evaluates an expression
	 * @param expression The expression
	 * @return The value of the expression, or null if the expression doesn't resolve to a value
	 */
	public <T> T eval(String expression) throws ScriptRuntimeException;

	/**
	 * Evaluates an expression, run through the preprocessor
	 * @param expression The expression
	 * @param include The inclusion method
	 * @return The value of the expression, or null if the expression doesn't resolve to a value
	 */
	public default <T> T evalPrerocessor(String expression, Function<String, Stream<String>> include) throws ScriptRuntimeException {
		return this.eval(Preprocessor.preprocess(expression, include, this::isPreprocessorFlag));
	}

	public boolean isPreprocessorFlag(String flag);

	public Script setPreprocessorFlags(String... string);

	/**
	 * Evaluates an expression, run through the preprocessor
	 * @param expression The expression
	 * @return The value of the expression, or null if the expression doesn't resolve to a value
	 */
	public default <T> T evalPreprocessor(String expression) throws ScriptRuntimeException {
		return this.evalPrerocessor(expression, this::inclusion);
	}

	public default Stream<String> inclusion(String resource) {
		return Preprocessor.DEFAULT_INCLUDE.apply(resource);
	}

	/**
	 * Obtains an encapsulating reference to a function defined at the script context level
	 * @param name The function to reference
	 * @return The reference to the function
	 */
	public <T> ScriptMethod<T> method(String name);

	/**
	 * Obtains an encapsulating reference to a variable defined at the script context level
	 * @param name The variable to reference
	 * @return The reference to the variable
	 */
	public <T> ScriptVariable<T> variable(String name);

	public default <Interface> void bindMethodGeneric(String name, Interface method) { bind(name, method); }

	public default <T> void bindMethodVarargs(String name, VarFunction<T> method) { bind(name, method); }

	public default void bindMethod(String name, Runnable method) { bind(name, method); }
	public default <A> void bindMethod(String name, Consumer<A> method) { bind(name, method); }
	public default <A, B> void bindMethod(String name, BiConsumer<A ,B> method) { bind(name, method); }
	public default <A> void bindMethod(String name, Supplier<A> method) { bind(name, method); }
	public default <A, B> void bindMethod(String name, Function<A, B> method) { bind(name, method); }
	public default <A, B, C> void bindMethod(String name, BiFunction<A, B, C> method) { bind(name, method); }

	@Deprecated
	public default void bindOverloadedMethod(String overloadedName, Object object) {
		this.bindOverloadedMethod(overloadedName, object, overloadedName);
	}

	@Deprecated
	public default void bindOverloadedMethod(String overloadedName, Object object, String as) {
		this.bind("internal_temporary_variable", object);
		Object method = this.eval("internal_temporary_variable." + overloadedName);
		this.bind(as, method);
		this.unbind("internal_temporary_variable");
	}

	public default <T> VarFunction<T> wrap(String name) {
		return wrap(name, null);
	}

	public default <T> VarFunction<T> wrap(String name, T def) {
		if (get(name) == null) {
			return args -> def;
		}
		ScriptMethod<T> method = method(name);
		return method::call;
	}

	public default <Interface> Interface implementProxy(Class<Interface> clazz) {
		@SuppressWarnings("unchecked")
		Interface ret = (Interface) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ clazz }, (Object thiz, Method method, Object[] args) -> {
			return this.method(method.getName()).implementation_call(args);
		});
		return ret;
	}

	public <T> T newObjectType();

	public <T> T newArrayType();

}
