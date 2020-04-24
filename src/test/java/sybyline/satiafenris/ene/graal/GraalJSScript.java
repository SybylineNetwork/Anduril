package sybyline.satiafenris.ene.graal;

import java.util.function.*;
import javax.script.ScriptEngine;
import sybyline.satiafenris.ene.AbstractScript;

public class GraalJSScript extends AbstractScript {

//	private static final com.oracle.truffle.js.scriptengine.GraalJSEngineFactory GRAALJS = new com.oracle.truffle.js.scriptengine.GraalJSEngineFactory();
	private static Function<Predicate<String>, ScriptEngine> factory = 
//		predicate -> GRAALJS.getScriptEngine();
		null;

	public GraalJSScript() {
		super(factory);
	}

}
