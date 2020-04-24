package sybyline.anduril.scripting.server;

import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import sybyline.anduril.scripting.server.cmd.ServerCommands;
import sybyline.anduril.scripting.server.events.ServerEvents;

public final class ServerScripting {

	private ServerScripting() {}

	public static final ServerScripting INSTANCE = new ServerScripting();

	public void setup(FMLServerStartingEvent event) {
		server = event.getServer();
		resources = server.getResourceManager();
		commands = new ServerCommands(this);
		events = new ServerEvents(this);
	}

	public MinecraftServer server;
	public IReloadableResourceManager resources;
	public ServerCommands commands;
	public ServerEvents events;

}
