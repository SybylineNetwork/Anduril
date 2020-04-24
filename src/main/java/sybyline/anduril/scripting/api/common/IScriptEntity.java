package sybyline.anduril.scripting.api.common;

import java.util.function.Consumer;

import sybyline.anduril.scripting.api.data.IScriptData;
import sybyline.anduril.util.math.Vector;
import sybyline.satiafenris.ene.ScriptBridge;
import sybyline.satiafenris.ene.ScriptRuntimeException;

public interface IScriptEntity extends ScriptBridge {

	public IScriptData data();

	// Casting

	public default boolean is_living() {
		return this instanceof IScriptLiving;
	}

	public default IScriptLiving get_as_living() {
		if (!is_living()) throw new ScriptRuntimeException("Script didn't check is_living()!");
		return (IScriptLiving)this;
	}

	public default void if_is_living(Consumer<IScriptLiving> task) {
		if (is_living()) task.accept(get_as_living());
	}

	public default boolean is_player() {
		return this instanceof IScriptPlayer;
	}

	public default IScriptPlayer get_as_player() {
		if (!is_player()) throw new ScriptRuntimeException("Script didn't check is_player()!");
		return (IScriptPlayer)this;
	}

	public default void if_is_player(Consumer<IScriptPlayer> task) {
		if (is_player()) task.accept(get_as_player());
	}

	// Positional

	public Vector pos();

	public void pos(Vector position);

	public void move(Vector position);

	public Vector look();

	public void look(Vector look);

}
