package sybyline.satiafenris.ene;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.*;
import javax.script.*;

import sybyline.anduril.util.rtc.RuntimeTricks;

@SuppressWarnings("unchecked")
public abstract class AbstractScript implements Script {

	protected final ScriptEngine engine;
	protected final ScriptContext context;
	protected final Invocable methods;

	protected boolean isStrict = false;
	protected final List<String> whitelist_class;
	protected final List<String> whitelist_package;
	
	protected AbstractScript(Function<Predicate<String>, ScriptEngine> factory) {
		this(factory, engine -> (Invocable)engine);
	}

	protected AbstractScript(Function<Predicate<String>, ScriptEngine> factory, Function<ScriptEngine, Invocable> invocable) {
		engine = factory.apply(this::checkAllow);
		context = engine.getContext();
		methods = invocable.apply(engine);
		whitelist_class = new ArrayList<String>();
		whitelist_package = new ArrayList<String>();
		
		unbind("load");
		unbind("quit");
        unbind("loadWithNewGlobal");
        unbind("exit");
        
        // Messy, really should get bindOverloadedMethod to work
        this.bindMethod("_using", (Consumer<String>)this::importJS);
        this.bindMethod("_using_as", (BiConsumer<String, String>)this::importJS);
        this.eval("use=function(one,two){if(typeof two==\"undefined\"){_using(one);}else{_using_as(one,two);}};");
        this.eval("exists=function(obj){if(typeof obj==\"undefined\"){return false;}else{return obj!=null;}};");
        this.eval("format=function(format){var args=Array.prototype.slice.call(arguments,1);return format.replace(/{(\\d+)}/g,function(match,number){return typeof args[number]!='undefined'?args[number]:match;});};");
        this.eval("_new_array=function(){return [];};");
        this.eval("_new_object=function(){return {};};");
	}

	public boolean checkAllow(String className) {
		if (!isStrict) return true;
		if (whitelist_package.stream().anyMatch(p -> className.startsWith(p)))
			return true;
		if (whitelist_class.stream().anyMatch(p -> className.equals(p)))
			return true;
		return false;
	}

	@Override
	public Script strict() {
		isStrict = true;
		return this;
	}

	@Override
	public void allowClasses(Object... classes) {
		for (Object o : classes) {
			String clazz;
			if (o instanceof Class) {
				clazz = ((Class<?>)o).getName();
			} else {
				clazz = String.valueOf(o);
			}
			if (clazz.indexOf('*') == clazz.length() - 1) {
				whitelist_package.add(clazz.substring(0, clazz.length() - 1));
			} else {
				whitelist_class.add(clazz);
			}
		}
	}

	private void importJS(String clazz) {
		String shorthand = clazz.substring(clazz.lastIndexOf(".") + 1);
		this.importJS(clazz, shorthand);
	}

	private void importJS(String clazz, String shorthand) {
		Class<?> type = RuntimeTricks.procrastinate(() -> Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()));
		this.bind(shorthand, type);
	}

	@Override
	public void unbind(String name) {
		int scope = context.getAttributesScope(name);
		if (scope != -1) context.removeAttribute(name, scope);
	}

	@Override
	public <T> void set(String name, T object) {
		context.setAttribute(name, object, ScriptContext.ENGINE_SCOPE);
	}

	@Override
	public <T> T get(String name) {
		return (T) context.getAttribute(name);
	}

	@Override
	public <T> T eval(String expression) {
		try {
			return (T) engine.eval(expression);
		} catch (ScriptException e) {
			throw new ScriptRuntimeException(e);
		}
	}

	@Override
	public <T> ScriptMethod<T> method(String name) {
		return args -> methods.invokeFunction(name, args);
	}

	@Override
	public <T> ScriptVariable<T> variable(String name) {
		return new ScriptVariableImpl<T>(this, name);
	}

	@Override
	public <T> T newObjectType() {
		try {
			return (T) methods.invokeFunction("_new_object");
		} catch (Exception e) {
			throw new ScriptRuntimeException(e);
		}
	}

	@Override
	public <T> T newArrayType() {
		try {
			return (T) methods.invokeFunction("_new_array");
		} catch (Exception e) {
			throw new ScriptRuntimeException(e);
		}
	}

	static final Script javascript(boolean useGraalIfPossible) {
		Script ret;
		if (useGraalIfPossible && supportsGraal) {
			ret = supplyGraal.get();
		} else {
			ret = new NashornScript();
		}
		return ret;
	}

	private static final boolean supportsGraal;
	private static final Supplier<Script> supplyGraal;

	static {
		boolean supported;
		Supplier<Script> supplier;
		try {
			ClassLoader ctx = Thread.currentThread().getContextClassLoader();
			Class.forName("com.oracle.truffle.js.scriptengine.GraalJSEngineFactory", true, ctx);
			Class<? extends Script> graalclass = (Class<? extends Script>)
				Class.forName("sybyline.satiafenris.ene.graal.GraalJSScript", true, ctx);
			Constructor<? extends Script> graalconstructor = graalclass.getDeclaredConstructor(new Class[0]);
			supplier = RuntimeTricks.procrastinate_supplier(graalconstructor::newInstance);
			supported = true;
		} catch(Exception e) {
			supported = false;
			supplier = NashornScript::new;
		}
		supportsGraal = supported;
		supplyGraal = supplier;
	}

}
