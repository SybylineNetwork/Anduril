package sybyline.anduril.extensions.event;

import net.minecraft.server.MinecraftServer;

public class SybylineServerEvent extends SybylineEvent {

	protected SybylineServerEvent(MinecraftServer server) {
		this.server = server;
	}

	public final MinecraftServer server;

}
