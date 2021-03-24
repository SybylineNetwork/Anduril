package sybyline.anduril.scripting.server.cmd;

import jdk.nashorn.api.scripting.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.*;

@SuppressWarnings("restriction")
public class ArgMap {

	public static final SimpleCommandExceptionType REQUIRED_ARG = new SimpleCommandExceptionType(() -> "sybyline.scriptcmd.required");

	public ArgMap(ScriptCommandWrapper parent) {
		domain = parent.domain;
		args = parent.script.newObjectType();
		defs = parent.script.newObjectType();
	}

	public final String domain;
	private final JSObject args;
	private final JSObject defs;

	public Object args() {
		return args;
	}

	public Object defs() {
		return defs;
	}

	public void present(String name, Object object) {
		args.setMember(name, object);
		defs.setMember(name, true);
	}

	public void absent(String name, Object def) throws CommandSyntaxException {
		if (def == null)
			throw REQUIRED_ARG.createWithContext(new StringReader(name));
		args.setMember(name, def);
		defs.setMember(name, false);
	}

}
