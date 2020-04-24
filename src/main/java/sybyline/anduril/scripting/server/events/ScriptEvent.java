package sybyline.anduril.scripting.server.events;

import java.util.function.Consumer;

import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.api.server.IScriptEvent;

public class ScriptEvent implements IScriptEvent {

	public ScriptEvent(ScriptEventWrapper scriptEventWrapper) {
		this.scriptEventWrapper = scriptEventWrapper;
	}

	private final ScriptEventWrapper scriptEventWrapper;

	@Override
	public void onPlayerJoin(Consumer<IScriptPlayer> action) {
		scriptEventWrapper.listen_playerJoin.add(action);
	}

	@Override
	public void onPlayerTick(Consumer<IScriptPlayer> action) {
		scriptEventWrapper.listen_playerTick.add(action);
	}

	@Override
	public void onPlayerLeave(Consumer<IScriptPlayer> action) {
		scriptEventWrapper.listen_playerLeave.add(action);
	}

}
