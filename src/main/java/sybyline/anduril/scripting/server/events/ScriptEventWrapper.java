package sybyline.anduril.scripting.server.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.common.Anduril;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.common.ScriptWrapper;
import sybyline.anduril.util.data.IFormat;
import sybyline.satiafenris.ene.Subclasser;

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
		generics.clear();
		events.clear();
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
	final Map<String, Consumer<? extends Event>> generics = new HashMap<>();
	final Map<Class<? extends Event>, List<Consumer<Event>>> events = new HashMap<>();

	public void registerGeneric(String id, Object forgeEventClass, Consumer<Event> handler) {
		if (id == null) {
			Anduril.LOGGER.error(id + ": ID was null!", new RuntimeException());
			return;
		}
		if (forgeEventClass == null) {
			Anduril.LOGGER.error(id + ": Event class was null!", new RuntimeException());
			return;
		}
		if (handler == null) {
			Anduril.LOGGER.error(id + ": Handler was null!", new RuntimeException());
			return;
		}
		if (generics.containsKey(id)) {
			Anduril.LOGGER.error(id + ": Already registered a handler with this id!", new RuntimeException());
			return;
		}
		Class<? extends Event> eventClass;
		try {
			eventClass = Subclasser.classify(forgeEventClass).asSubclass(Event.class);
		} catch(Exception ex) {
			Anduril.LOGGER.error(id + ": Not a valid Event class: '"+forgeEventClass+"'", ex);
			ex.printStackTrace();
			return;
		}
		events.computeIfAbsent(eventClass, __ -> new ArrayList<>()).add(handler);
	}

	@SubscribeEvent
	void event_all(Event event) {
		Class<?> clazz = event.getClass();
		do {
			List<Consumer<Event>> list = events.get(clazz);
			if (list !=  null) list.forEach(c -> c.accept(event));
			clazz = clazz.getSuperclass();
		} while (clazz != Object.class && clazz != null);
	}

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
