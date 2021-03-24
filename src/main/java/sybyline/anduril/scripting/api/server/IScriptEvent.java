package sybyline.anduril.scripting.api.server;

import java.util.function.Consumer;

import net.minecraftforge.eventbus.api.Event;
import sybyline.anduril.scripting.api.common.IScriptPlayer;

public interface IScriptEvent {

	public void onEvent(String id, Object forgeEventClass, Consumer<Event> handler);

	public void onPlayerJoin(Consumer<IScriptPlayer> action);

	public void onPlayerTick(Consumer<IScriptPlayer> action);

	public void onPlayerLeave(Consumer<IScriptPlayer> action);

}
