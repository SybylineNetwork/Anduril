package sybyline.anduril.scripting.api.server;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IScriptServer {

	public void command(String command, Object... inserts);

	public CompoundNBT persistant();

	public Object ephemeral();

	public IPermissionConfigure permissions();

	public Object load_data(String type, ResourceLocation resource);

	public Object load_data_url(String type, String url);

}
