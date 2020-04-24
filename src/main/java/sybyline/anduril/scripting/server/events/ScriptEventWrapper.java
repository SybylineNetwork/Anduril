package sybyline.anduril.scripting.server.events;

import java.util.function.Consumer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.common.*;
import sybyline.anduril.util.data.IFormat;

public class ScriptEventWrapper extends ScriptWrapper<Void> {

	public ScriptEventWrapper(String name, String source) {
		super(name, source);
	}

	public final ScriptEvent event = new ScriptEvent(this);

	@Override
	public Void setupWithContext(Void context) {
		this.setupInternal();
		MinecraftForge.EVENT_BUS.register(this);
		return context;
	}

	final void unlisten() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Override
	protected void bindVariables() {
		this.script.bind("event", this.event);
	}

	public static final IFormat<ScriptEventWrapper> FORMAT = ScriptWrapper.formatOf(ScriptEventWrapper::new);

	@Override
	protected LogicalSide side() {
		return LogicalSide.SERVER;
	}

	// Event handlers

	final ScriptEventList<Consumer<IScriptPlayer>> listen_playerJoin = new ScriptEventList<Consumer<IScriptPlayer>>();
	final ScriptEventList<Consumer<IScriptPlayer>> listen_playerTick = new ScriptEventList<Consumer<IScriptPlayer>>();
	final ScriptEventList<Consumer<IScriptPlayer>> listen_playerLeave = new ScriptEventList<Consumer<IScriptPlayer>>();

	@SubscribeEvent
	void event_playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (listen_playerJoin.shouldRun()) {
			IScriptPlayer player = CommonScripting.INSTANCE.getScriptPlayerFor(event.getPlayer(), this.domain);
			listen_playerJoin.run(handler -> handler.accept(player));
		}
	}

	@SubscribeEvent
	void event_playerQuit(TickEvent.PlayerTickEvent event) {
		if (listen_playerTick.shouldRun()) {
			IScriptPlayer player = CommonScripting.INSTANCE.getScriptPlayerFor(event.player, this.domain);
			listen_playerTick.run(handler -> handler.accept(player));
		}
	}

	@SubscribeEvent
	void event_playerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
		if (listen_playerLeave.shouldRun()) {
			IScriptPlayer player = CommonScripting.INSTANCE.getScriptPlayerFor(event.getPlayer(), this.domain);
			listen_playerLeave.run(handler -> handler.accept(player));
		}
	}

}
