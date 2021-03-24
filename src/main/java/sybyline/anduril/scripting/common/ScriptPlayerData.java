package sybyline.anduril.scripting.common;

import java.util.*;
import com.google.common.collect.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.server.ServerScripting;
import sybyline.anduril.util.data.ICachable;

public class ScriptPlayerData implements ICachable<CompoundNBT> {

	public ScriptPlayerData(UUID uuid) {
		this.uuid = uuid;
		this.profile = ServerScripting.INSTANCE.getPlayerProfile(uuid);
	}

	final UUID uuid;
	public final GameProfile profile;
	private final Map<String, ScriptPlayer> scriptdata = Maps.newHashMap();
	private final Map<String, CompoundNBT> domaindata = Maps.newHashMap();
	public final Set<String> permissions = Sets.newHashSet();

	public final IScriptPlayer scriptdata(String domain) {
		return scriptdata.computeIfAbsent(domain, dom -> new ScriptPlayer(dom, this, () -> ServerScripting.INSTANCE.server.getPlayerList().getPlayerByUUID(uuid)));
	}

	public CompoundNBT data(String domain) {
		return domaindata.computeIfAbsent(domain, dom -> new CompoundNBT());
	}

	@Override
	public void construct() {
	}

	@Override
	public void readFrom(CompoundNBT nbt) {
		CompoundNBT nbt_data = nbt.getCompound("data");
		domaindata.clear();
		for (String key : nbt_data.keySet()) {
			CompoundNBT nbt_domain = nbt_data.getCompound(key);
			domaindata.put(key, nbt_domain);
		}
		ListNBT nbt_permissions = nbt.getList("permissions", NBT.TAG_STRING);
		permissions.clear();
		for (int i = 0; i < nbt_permissions.size(); i++) {
			permissions.add(nbt_permissions.getString(i));
		}
	}

	@Override
	public void firstLoad() {
	}

	@Override
	public void writeTo(CompoundNBT nbt) {
		CompoundNBT nbt_data = new CompoundNBT();
		domaindata.forEach((domain, sd) -> {
			nbt_data.put(domain, sd);
		});
		nbt.put("data", nbt_data);
		ListNBT nbt_permissions = new ListNBT();
		for (String perm : permissions) {
			nbt_permissions.add(StringNBT.valueOf(perm));
		}
		nbt.put("permissions", nbt_permissions);
	}

	@Override
	public boolean shouldKeep() {
		return ServerScripting.INSTANCE.server.getPlayerList().getPlayerByUUID(uuid) != null;
	}

}
