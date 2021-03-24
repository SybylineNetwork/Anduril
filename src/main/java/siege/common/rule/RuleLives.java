package siege.common.rule;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import siege.common.siege.Siege;

public abstract class RuleLives extends Rule {

	public abstract void playerDie(Siege siege, PlayerEntity player);
	public abstract void playerJoin(Siege siege, PlayerEntity player);

	public int lives;
	protected String type;

	public void toNBT(CompoundNBT nbt) {
		super.toNBT(nbt);
		nbt.putInt(type, lives);
	}

	public void fromNBT(CompoundNBT nbt) {
		super.fromNBT(nbt);
		lives = nbt.getInt(type);
	}
	
	public void setValue(CommandSource sender, int val) {
		this.lives = val;
	}

}
