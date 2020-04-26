package sybyline.anduril.scripting.common;

import net.minecraft.nbt.CompoundNBT;
import sybyline.anduril.scripting.data.ScriptData;
import sybyline.anduril.scripting.server.ScriptServer;
import sybyline.anduril.util.data.ICachable;

public class ScriptServerData implements ICachable<CompoundNBT> {

	public ScriptServerData(String domain) {
		this.domain = domain;
		this.scriptserver = new ScriptServer(this);
		this.scriptdata = new ScriptData();
		this.ephemeral = ScriptWrapper.newScript().newObjectType();
	}

	public final String domain;
	public final ScriptServer scriptserver;
	public ScriptData scriptdata;
	public final Object ephemeral;

	@Override
	public void construct() {
	}

	@Override
	public void readFrom(CompoundNBT nbt) {
		scriptdata = new ScriptData(nbt.getCompound("data"));
	}

	@Override
	public void firstLoad() {
	}

	@Override
	public void writeTo(CompoundNBT nbt) {
		nbt.put("data", scriptdata.toCompound());
	}

	@Override
	public boolean shouldKeep() {
		return true;
	}

}
