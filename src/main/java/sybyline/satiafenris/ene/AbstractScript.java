package sybyline.satiafenris.ene;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.*;
import jdk.internal.dynalink.beans.StaticClass;
import sybyline.anduril.util.rtc.RuntimeTricks;

@SuppressWarnings({"unchecked", "restriction"})
public abstract class AbstractScript implements Script {

	private static final String
		ctxdecl = "Includes={};",
		packages = Stream.of(
			"net", "sybyline", "siege", "jsm"
			).map(s->s+"=Packages."+s+";").collect(Collectors.joining()),
		_new_array = "_new_array=function(){return [];};",
		_new_object = "_new_object=function(){return {};};",
		println = "println=function(o){java.lang.System.out.println(o);};",
		errln = "errln=function(o){java.lang.System.err.println(o);};",
		evaluation = ctxdecl + packages +  _new_array + _new_object + println + errln;
	
	protected final ScriptEngine engine;
	protected final ScriptContext context;
	protected final Invocable methods;

	protected boolean isStrict = false;
	protected Function<String, Stream<String>> inclusions = null;
	protected final List<String> whitelist_class;
	protected final List<String> whitelist_package;
	
	protected AbstractScript(Function<Predicate<String>, ScriptEngine> factory) {
		this(factory, Invocable.class::cast);
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
        
        this.bind("Types", TypeConstants.INSTANCE);
        this.bindMethod("define", this::define);
        this.bindMethodVarargs("use", this::importJS);
        this.eval(evaluation);
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
	public Script setInclusions(Function<String, Stream<String>> include) {
		inclusions = include;
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

	@Override
	public boolean isPreprocessorFlag(String flag) {
		return flags.test(flag);
	}
	private Predicate<String> flags = Preprocessor.flags();
	@Override
	public Script setPreprocessorFlags(String... flags) {
		this.flags = Preprocessor.flags(flags);
		return this;
	}

	private Object importJS(Object... args) {
		String clazz = String.valueOf(args[0]);
		String shorthand = args.length >= 2 ? String.valueOf(args[1]) : clazz.substring(clazz.lastIndexOf(".") + 1);
		StaticClass type = StaticClass.forClass(RuntimeTricks.procrastinate(() -> Class.forName(clazz)));
		this.bind(shorthand, type);
		return type;
	}

	public Object define(jdk.nashorn.api.scripting.JSObject definition) {
		if (!definition.isFunction())
			throw new IllegalArgumentException("Tried to extend with a non-function!");
		Class<?> sub = Subclasser.create(definition);
		return StaticClass.forClass(sub);
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
	public Stream<String> inclusion(String resource) {
		if (this.inclusions != null) {
			Stream<String> incl = this.inclusions.apply(resource);
			if (incl != null) return incl;
		}
		return Preprocessor.DEFAULT_INCLUDE.apply(resource);
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
	static final Script java() {
		return new JavaScript();
	}

	private static final boolean supportsGraal;
	private static final Supplier<Script> supplyGraal;

	static {
		boolean supported;
		Supplier<Script> supplier;
		if (Boolean.getBoolean("useGraalJS")) try {
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
		} else {
			supported = false;
			supplier = NashornScript::new;
		}
		supportsGraal = supported;
		supplyGraal = supplier;
	}

}
