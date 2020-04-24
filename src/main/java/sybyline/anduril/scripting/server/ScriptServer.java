package sybyline.anduril.scripting.server;

import sybyline.anduril.scripting.api.server.IScriptServer;
import sybyline.anduril.scripting.common.ScriptUtil;

public class ScriptServer implements IScriptServer {

	private ScriptServer() {}

	public static final ScriptServer INSTANCE = new ScriptServer();

	@Override
	public void command(String command, Object... parameters) {
		String exec = ScriptUtil.INSTANCE.format(command, parameters);
		ServerScripting.INSTANCE.server.getCommandManager().handleCommand(ServerScripting.INSTANCE.server.getCommandSource(), exec);
	}

}
