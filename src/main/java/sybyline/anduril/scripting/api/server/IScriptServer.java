package sybyline.anduril.scripting.api.server;

public interface IScriptServer {

	public void command(String command, Object... inserts);

}
