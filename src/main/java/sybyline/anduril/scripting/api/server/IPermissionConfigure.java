package sybyline.anduril.scripting.api.server;

public interface IPermissionConfigure {

	public IPermission new_node(String node, String desc);

	public IPermission get_node(String node);

	public IPermission get_command(String command);

}
