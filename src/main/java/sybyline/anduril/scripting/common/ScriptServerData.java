package sybyline.anduril.scripting.common;

import net.minecraft.nbt.CompoundNBT;
import sybyline.anduril.scripting.server.ScriptServer;
import sybyline.anduril.util.data.ICachable;
import sybyline.satiafenris.ene.Convert;

public class ScriptServerData implements ICachable<CompoundNBT> {

	public ScriptServerData(String domain) {
		this.domain = domain;
		this.scriptserver = new ScriptServer(this);
		this.scriptdata = new CompoundNBT();
		this.ephemeral = Convert.convert_script.newObjectType();
	}

	public final String domain;
	public final ScriptServer scriptserver;
	public CompoundNBT scriptdata;
	public final Object ephemeral;

	@Override
	public void construct() {
	}

	@Override
	public void readFrom(CompoundNBT nbt) {
		scriptdata = nbt.getCompound("data");
	}

	@Override
	public void firstLoad() {
	}

	@Override
	public void writeTo(CompoundNBT nbt) {
		nbt.put("data", scriptdata);
	}

	@Override
	public boolean shouldKeep() {
		return true;
	}

}
