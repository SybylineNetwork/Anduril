package sybyline.anduril.scripting.server;

import java.io.InputStream;
import java.net.URL;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.scripting.api.server.IPermissionConfigure;
import sybyline.anduril.scripting.api.server.IScriptServer;
import sybyline.anduril.scripting.common.CommonScriptingExtensions;
import sybyline.anduril.scripting.common.ScriptServerData;
import sybyline.anduril.util.Util;
import sybyline.satiafenris.ene.Convert;

public class ScriptServer implements IScriptServer {

	public ScriptServer(ScriptServerData domain) {
		this.domain = domain;
		this.configure = ServerManagement.INSTANCE.permissions.newConfigure(domain);
	}

	private final ScriptServerData domain;
	private final ServerPermissions.Configure configure;

	@Override
	public void command(String command, Object... parameters) {
		String exec = CommonScriptingExtensions.formatCommand(command, parameters);
		ServerScripting.INSTANCE.server.getCommandManager().handleCommand(ServerScripting.INSTANCE.server.getCommandSource(), exec);
	}

	@Override
	public CompoundNBT persistant() {
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

	public Object load_data(String type, ResourceLocation resource) {
		try {
			IResource res = ServerScripting.INSTANCE.resources.getResource(resource);
//			String path = wrap.location.getPath();
			InputStream stream = res.getInputStream();
			if (stream != null) {
				String data = Util.IO.readString(stream);
				if (data != null) {
					return Convert.js_object_from_data(type, data);
				}
			}
			return null;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object load_data_url(String type, String url) {
		if (Convert.supported_datas.contains(type)) try {
			String data = Util.IO.readString(new URL(url).openStream());
			if (data != null) {
				return Convert.js_object_from_data(type, data);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
