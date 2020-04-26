package sybyline.anduril.scripting.api.server;

import sybyline.anduril.scripting.api.data.IScriptData;

public interface IScriptServer {

	public void command(String command, Object... inserts);

	public IScriptData persistant();

	public Object ephemeral();

	public IPermissionConfigure permissions();

}
