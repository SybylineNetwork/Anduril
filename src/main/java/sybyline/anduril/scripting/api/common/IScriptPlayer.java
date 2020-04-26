package sybyline.anduril.scripting.api.common;

import java.util.UUID;

import sybyline.anduril.scripting.api.server.IPermission;

public interface IScriptPlayer extends IScriptLiving {

	public boolean is_online();

	// Profile

	public String name();

	public UUID uuid();

	public void send_chat(String text);

	public boolean hasPermission(IPermission permission);

	public void grantPermission(IPermission permission);

	public void revokePermission(IPermission permission);

}
