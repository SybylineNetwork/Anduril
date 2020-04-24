package sybyline.anduril.scripting.common;

import java.util.*;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundNBT;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.api.data.IScriptData;
import sybyline.anduril.scripting.data.ScriptData;
import sybyline.anduril.scripting.server.ServerScripting;
import sybyline.anduril.util.data.ICachable;

public class ScriptPlayerData implements ICachable<CompoundNBT> {

	public ScriptPlayerData(UUID uuid) {
		this.uuid = uuid;
	}

	final UUID uuid;
	private final Map<String, ScriptPlayer> scriptdata = Maps.newHashMap();
	private final Map<String, ScriptData> domaindata = Maps.newHashMap();

	public final IScriptPlayer scriptdata(String domain) {
		return scriptdata.computeIfAbsent(domain, dom -> new ScriptPlayer(dom, this, () -> ServerScripting.INSTANCE.server.getPlayerList().getPlayerByUUID(uuid)));
	}

	public IScriptData data(String domain) {
		return domaindata.computeIfAbsent(domain, dom -> new ScriptData());
	}

	@Override
	public void construct() {
	}

	@Override
	public void readFrom(CompoundNBT nbt) {
		CompoundNBT nbt_data = nbt.getCompound("data");
		System.out.println(nbt_data);
		for (String key : nbt_data.keySet()) {
			CompoundNBT nbt_domain = nbt_data.getCompound(key);
			domaindata.put(key, new ScriptData(nbt_domain));
		}
	}

	@Override
	public void firstLoad() {
	}

	@Override
	public void writeTo(CompoundNBT nbt) {
		CompoundNBT nbt_data = new CompoundNBT();
		domaindata.forEach((domain, sd) -> {
			nbt_data.put(domain, sd.toCompound());
		});
		System.out.println(nbt_data);
		nbt.put("data", nbt_data);
	}

	@Override
	public boolean shouldKeep() {
		return ServerScripting.INSTANCE.server.getPlayerList().getPlayerByUUID(uuid) != null;
	}

}
