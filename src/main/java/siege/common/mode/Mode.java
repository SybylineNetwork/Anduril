package siege.common.mode;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import siege.common.rule.Rule;
import siege.common.rule.RuleHandler;
import siege.common.siege.Siege;
import siege.common.siege.SiegePlayerData;
import siege.common.siege.SiegeTeam;

public abstract class Mode {

	public static Mode of(String string) {
		return SiegeMode.newMode(string);
	}

	public int pointsNeededToWin = 0;
	protected Siege siege;

	public Mode setSiege(Siege siege) {
		this.siege = siege;
		return siege.mode = this;
	}

	public List<Rule> rules = new ArrayList<>(1);
	public final RuleHandler ruleHandler = new RuleHandler(this);
	
	public abstract SiegeMode mode();
	protected abstract String object();
	
	public abstract boolean tick();
	
	public final boolean isReady() {
		for (SiegeTeam team : siege.teams()) {
			BlockPos respawn = team.getRespawnPoint();
			if (respawn.getX() == 0 && respawn.getY() < 4 && respawn.getZ() == 0) {
				return false;
			}
		}
		return this.isReadyParticular();
	}
	
	public abstract boolean isReadyParticular();
	
	public abstract String score(World world, Siege siege, SiegeTeam team);
	
	public final void fromNBT(Siege siege, CompoundNBT nbt) {
		this.fromNBT0(siege, nbt);
		this.ruleHandler.fromNBT(nbt.getCompound("VinyarionAddon_Rules"));
		this.pointsNeededToWin = nbt.getInt("VinyarionAddon_PointsNeededToWin");
	}
	
	public final void toNBT(Siege siege, CompoundNBT nbt) {
		this.toNBT0(siege, nbt);
		if(!nbt.contains("VinyarionAddon_Rules", NBT.TAG_COMPOUND)) nbt.put("VinyarionAddon_Rules", new CompoundNBT());
		this.ruleHandler.toNBT(nbt.getCompound("VinyarionAddon_Rules"));
		nbt.putInt("VinyarionAddon_PointsNeededToWin", this.pointsNeededToWin);
	}

	protected abstract void fromNBT0(Siege siege, CompoundNBT nbt);
	
	protected abstract void toNBT0(Siege siege, CompoundNBT nbt);
	
	public abstract int scoringMethod(Siege siege, SiegeTeam team);
	
	public void preEndSiege() {}
	
	public void printMVP(Siege siege2, List<SiegeTeam> siegeTeams) {
		
	}
	
	public String endMessage(Siege siege, SiegeTeam team, String message) {
		return team.color + team.getTeamName() + TextFormatting.RED + ": Kills: " + team.getTeamKills() + ", Deaths: " + team.getTeamDeaths();
	}
	
	public String object(Siege siege, boolean plural) {
		return plural ? object() + "s" : object();
	}
	
	public void scoreboard(List<Score> list, Scoreboard board, ScoreObjective objective, String timeRemaining, SiegeTeam team, ServerPlayerEntity entityplayer, SiegePlayerData playerdata) {}
	
	public void startSiege() {}

	public final CommandException generateException() {
		for (SiegeTeam team : siege.teams()) {
			BlockPos respawn = team.getRespawnPoint();
			if (respawn.getX() == 0 && respawn.getY() < 4 && respawn.getZ() == 0) {
				return new CommandException(new StringTextComponent(String.format("Siege %s cannot be started - the team %s does not seem to have a respawn point.", siege.getSiegeName(), team.getTeamName())));
			}
		}
		return this.generateExceptionParticular();
	}

	public abstract CommandException generateExceptionParticular();
	
}
