package sybyline.anduril.scripting.server;

import sybyline.anduril.scripting.api.data.IScriptData;
import sybyline.anduril.scripting.api.server.IPermissionConfigure;
import sybyline.anduril.scripting.api.server.IScriptServer;
import sybyline.anduril.scripting.common.ScriptUtil;
import sybyline.anduril.scripting.data.ScriptServerData;

public class ScriptServer implements IScriptServer {

	public ScriptServer(ScriptServerData domain) {
		this.domain = domain;
		this.configure = ServerManagement.INSTANCE.permissions.newConfigure(domain);
	}

	private final ScriptServerData domain;
	private final ServerPermissions.Configure configure;

	@Override
	public void command(String command, Object... parameters) {
		String exec = ScriptUtil.INSTANCE.format(command, parameters);
		ServerScripting.INSTANCE.server.getCommandManager().handleCommand(ServerScripting.INSTANCE.server.getCommandSource(), exec);
	}

	@Override
	public IScriptData persistant() {
		return domain.scriptdata;
	}

	@Override
	public Object ephemeral() {
		return domain.ephemeral;
	}

	@Override
	public IPermissionConfigure permissions() {
		return configure;
	}

}
