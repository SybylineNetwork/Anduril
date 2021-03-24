package siege.common.rule;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;
import siege.common.mode.Mode;
import siege.common.siege.Siege;

public class RuleHandler {
	private final Mode mode;
	public RuleHandler(Mode mode) {
		this.mode = mode;
	}
	public void tick(Siege siege) {
		for(Rule rule : mode.rules) rule.tick(siege);
	}
	public void playerLogin(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerLogin(siege, player);
	}
	public void playerLogout(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerLogout(siege, player);
	}
	public void playerJoin(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerJoin(siege, player);
	}
	public void playerLeave(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerLeave(siege, player);
	}
	public void playerDie(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerDie(siege, player);
	}
	public void toNBT(CompoundNBT nbt) {
		ListNBT strs = new ListNBT();
		for(Rule rule : mode.rules) strs.add(StringNBT.valueOf(rule.rule().identifier()));
		nbt.put("VinyarionAddon_RuleNames", strs);
		for(Rule rule : mode.rules) rule.toNBT(nbt);
	}
	public void fromNBT(CompoundNBT nbt) {
		ListNBT strs = nbt.getList("VinyarionAddon_RuleNames", NBT.TAG_STRING);
		for(int i = 0; i < strs.size(); i++) {
			String str = strs.getString(i);
			mode.rules.add(Rule.of(str));
		}
		for(Rule rule : mode.rules) rule.fromNBT(nbt);
	}
	public void playerRespawn(Siege siege, PlayerEntity player) {
		for(Rule rule : mode.rules) rule.playerRespawn(siege, player);
	}
}
