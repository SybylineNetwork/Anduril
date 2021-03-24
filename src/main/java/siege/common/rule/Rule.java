package siege.common.rule;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import siege.common.siege.Siege;

public abstract class Rule {

	public static Rule of(String string) {
		return SiegeRule.newRule(string);
	}

	public abstract SiegeRule rule();

	public void tick(Siege siege) {}
	public void playerLogin(Siege siege, PlayerEntity player) {}
	public void playerLogout(Siege siege, PlayerEntity player) {}
	public void playerJoin(Siege siege, PlayerEntity player) {}
	public void playerLeave(Siege siege, PlayerEntity player) {}
	public void playerDie(Siege siege, PlayerEntity player) {}
	public void toNBT(CompoundNBT nbt) {}
	public void fromNBT(CompoundNBT nbt) {}
	public void playerRespawn(Siege siege, PlayerEntity player) {}
	public void setValue(CommandSource sender, int val) {}
}
