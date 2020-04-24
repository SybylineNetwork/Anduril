package sybyline.anduril.scripting.api.common;

public interface IScriptPlayer extends IScriptLiving {

	public boolean is_online();

	// Profile

	public String name();

	public void send_chat(String text);

}
