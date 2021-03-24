package sybyline.anduril.scripting.server;

import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.server.permission.PermissionAPI;
import sybyline.anduril.common.AndurilGameRules;

public final class ServerManagement {

	private ServerManagement() {}

	public static final ServerManagement INSTANCE = new ServerManagement();

	public ServerPermissions permissions;
	public final AndurilGameRules rules = AndurilGameRules.SERVER;

	public void serverStart(FMLServerStartingEvent event) {
		PermissionAPI.setPermissionHandler(permissions = new ServerPermissions(event.getServer()));
		rules.load(event.getServer());
	}

	public void serverStop(FMLServerStoppingEvent event) {
		rules.save(event.getServer());
	}

}
