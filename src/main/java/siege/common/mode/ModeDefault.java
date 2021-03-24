package siege.common.mode;

import net.minecraft.command.CommandException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import siege.common.siege.Siege;
import siege.common.siege.SiegeTeam;

public class ModeDefault extends Mode {
	
	public ModeDefault() {
		this.pointsNeededToWin = -1;
	}

	@Override
	public SiegeMode mode() {
		return SiegeMode.DEATHMATCH;
	}

	public boolean tick() {
		return false;
	}

	public String score(World world, Siege siege, SiegeTeam team) {
		return team.color + team.getTeamName() + TextFormatting.GOLD + ": Kills: " + team.getTeamKills();
	}

	protected void fromNBT0(Siege siege, CompoundNBT nbt) {
	}

	protected void toNBT0(Siege siege, CompoundNBT nbt) {
	}

	protected String object() {
		return "kill";
	}

	public int scoringMethod(Siege siege, SiegeTeam team) {
		return team.getTeamKills();
	}

	public boolean isReadyParticular() {
		return true;
	}

	public CommandException generateExceptionParticular() {
		return null;
	}

}