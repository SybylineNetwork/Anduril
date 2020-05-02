package sybyline.anduril.scripting.api.server;

import java.util.function.BiConsumer;

public interface IScriptCommand {

	// Sender arguments -- must have one

	public IScriptCommand arg_player_self(String name);

	public IScriptCommand arg_player_one(String name);

	public IScriptCommand arg_player_multi(String name);

	public IScriptCommand arg_server();

	// Primitive args

	public default IScriptCommand arg_boolean(String name) {
		return this.arg_boolean(name, null);
	}

	public IScriptCommand arg_boolean(String name, Boolean def);

	public default IScriptCommand arg_integer(String name) {
		return this.arg_integer(name, null);
	}

	public default IScriptCommand arg_integer(String name, Integer def) {
		return this.arg_integer(name, def, Integer.MIN_VALUE);
	}

	public default IScriptCommand arg_integer(String name, Integer def, int min) {
		return this.arg_integer(name, def, min, Integer.MAX_VALUE);
	}

	public IScriptCommand arg_integer(String name, Integer def, int min, int max);

	public default IScriptCommand arg_double(String name) {
		return this.arg_double(name, null);
	}

	public default IScriptCommand arg_double(String name, Double def) {
		return this.arg_double(name, def, -Double.MAX_VALUE);
	}

	public default IScriptCommand arg_double(String name, Double def, double min) {
		return this.arg_double(name, def, min, Double.MAX_VALUE);
	}

	public IScriptCommand arg_double(String name, Double def, double min, double max);

	// Text args

	public default IScriptCommand arg_string_one(String name) {
		return this.arg_string_one(name, null);
	}

	public IScriptCommand arg_string_one(String name, String def);

	public IScriptCommand arg_string_oneof(String name, Object possibilities);

	public default IScriptCommand arg_string_quotable(String name) {
		return this.arg_string_quotable(name, null);
	}

	public IScriptCommand arg_string_quotable(String name, String def);

	public default IScriptCommand arg_string_rest(String name) {
		return this.arg_string_rest(name, null);
	}

	public IScriptCommand arg_string_rest(String name, String def);

	// Data args

	public default IScriptCommand arg_nbt_compound(String name) {
		return this.arg_nbt_compound(name, null);
	}

	public IScriptCommand arg_nbt_compound(String name, Object def);

	// Run -- First object is a map of names to arguments, second object is
	//   a map of names to whether the default value was used instead. We
	//   need the second because if the argument is defaulted or present,
	//   if (arg.name) will be true.
	public void runs(BiConsumer<Object, Object> command);

}
