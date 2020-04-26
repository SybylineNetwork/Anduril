package sybyline.anduril.scripting.server;

import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.server.permission.PermissionAPI;

public final class ServerManagement {

	private ServerManagement() {}

	public static final ServerManagement INSTANCE = new ServerManagement();

	public ServerPermissions permissions;

	public void serverStart(FMLServerStartingEvent event) {
		PermissionAPI.setPermissionHandler(permissions = new ServerPermissions(event.getServer()));
	}

	public void serverStop(FMLServerStoppingEvent event) {
		
	}

}
