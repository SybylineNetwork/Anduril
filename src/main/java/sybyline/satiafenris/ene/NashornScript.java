package sybyline.satiafenris.ene;

import jdk.nashorn.api.scripting.*;

@SuppressWarnings("restriction")
public class NashornScript extends AbstractScript {

	private static final NashornScriptEngineFactory NASHORN = new NashornScriptEngineFactory();
	
	public final NashornScriptEngine nashorn;
	public final ScriptObjectMirror Java;

	NashornScript() {
		super(predicate -> NASHORN.getScriptEngine(/*name -> predicate.test(name)*/));
		nashorn = (NashornScriptEngine) engine;
		Java = get("Java");
	}

}
