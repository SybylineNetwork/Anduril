package siege.common.mode;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants.NBT;
import siege.common.rule.Rule;
import siege.common.rule.RuleSequentialDomination;
import siege.common.siege.Siege;
import siege.common.siege.SiegePlayerData;
import siege.common.siege.SiegeTeam;
import siege.common.zone.ZoneControl;

public class ModeDomination extends Mode {

	public List<ZoneControl> zones = Lists.newArrayList();
	public boolean isSequential = false;
	public List<ZoneControl> activeZones = Lists.newArrayList();
	public Iterator<ZoneControl> orderedZones = null;

	@Override
	public SiegeMode mode() {
		return SiegeMode.DOMINATION;
	}

	public boolean tick() {
		if (!activeZones.isEmpty()) {
			Iterator<ZoneControl> active = activeZones.iterator();
			for (ZoneControl zone = active.next(); active.hasNext(); zone = active.next()) {
				if (zone.tick()) active.remove();
				if (isSequential) continue;
				if (zone.occupiers == null) continue;
				if (siege.getTicksRemaining() % 20 == 0) {
					zone.occupiers.score++;
				}
			}
		}
		if (isSequential) {
			if (activeZones.isEmpty()) {
				if (orderedZones.hasNext()) {
					activeZones.add(orderedZones.next());
				} else {
					return true;
				}
			}
		} else {
			for (SiegeTeam team : siege.teams()) {
				if (team.score >= this.pointsNeededToWin) {
					return true;
				}
			}
		}
		return false;
	}

	public String score(World world, Siege siege, SiegeTeam team) {
		return isSequential
			? (team.color + team.getTeamName() + TextFormatting.GOLD)
			: (team.color + team.getTeamName() + TextFormatting.GOLD + ": Score: " + team.score);
	}

	public boolean isReadyParticular() {
		return zones.size() > 0;
	}

	protected void fromNBT0(Siege siege, CompoundNBT nbt) {
		ListNBT czs = nbt.getList("VinyarionAddon_ControlZones", NBT.TAG_COMPOUND);
		zones.clear();
		for (int i = 0; i < czs.size(); i++) {
			ZoneControl zone = new ZoneControl();
			zone.fromNBT(siege, czs.getCompound(i));
			zones.add(zone);
		}
	}

	protected void toNBT0(Siege siege, CompoundNBT nbt) {
		ListNBT czs = new ListNBT();
		for (ZoneControl zone : zones) {
			czs.add(zone.toNBT(siege, new CompoundNBT()));
		}
		nbt.put("VinyarionAddon_ControlZones", czs);
	}

	public int scoringMethod(Siege siege, SiegeTeam team) {
		return team.score;
	}

	public String endMessage(Siege siege, SiegeTeam team, String message) {
		return super.endMessage(siege, team, message) + ", Points: " + team.score;
	}

	protected String object() {
		return "point";
	}

	public void scoreboard(List<Score> list, Scoreboard board, ScoreObjective objective, String timeRemaining, SiegeTeam team, ServerPlayerEntity entityplayer, SiegePlayerData playerdata) {
		list.add(null);
		if (isSequential) {
//			list.add(new Score(board, objective, "Score: " + team.score));
		} else {
			list.add(new Score(board, objective, "Score: " + team.score));
		}
		for(ZoneControl zone : zones) {
			if(zone.box.intersects(entityplayer.getBoundingBox()) && team == zone.attackers) {
				list.add(new Score(board, objective, "Progress: " + String.valueOf(MathHelper.floor((double)zone.ticksHeld / (double)zone.ticksTillOccupation))));
			}
		}
	}
	
	public void printMVP(Siege siege2, List<SiegeTeam> siegeTeams) {
		UUID mvpID = null;
		int mvpKills = 0;
		int mvpDeaths = 0;
		int mvpScore = Integer.MIN_VALUE;
		UUID longestKillstreakID = null;
		int longestKillstreak = 0;
		for (SiegeTeam team : siegeTeams) {
			for (UUID player : team.getPlayerList()) {
				SiegePlayerData playerData = siege.getPlayerData(player);
				int kills = playerData.getKills();
				int deaths = playerData.getDeaths();
				int score = playerData.addonData.personalscore;
				if (score > mvpScore || (score == mvpScore && deaths < mvpDeaths)) {
					mvpID = player;
					mvpKills = kills;
					mvpDeaths = deaths;
					mvpScore = score;
				}
				int streak = playerData.getLongestKillstreak();
				if (streak > longestKillstreak) {
					longestKillstreakID = player;
					longestKillstreak = streak;
				}
			}
		}
		if (mvpID != null) {
			String mvp = UsernameCache.getLastKnownUsername(mvpID);
			siege.announceToAllPlayers("MVP was " + mvp + " (" + siege.getPlayerTeam(mvpID).getTeamName() + TextFormatting.GOLD + ") with " + mvpKills + " kills / " + mvpDeaths + " deaths / " + mvpScore + " occupation points");
		}
		if (longestKillstreakID != null) {
			String streakPlayer = UsernameCache.getLastKnownUsername(longestKillstreakID);
			siege.announceToAllPlayers("Longest killstreak was " + streakPlayer + " (" + siege.getPlayerTeam(longestKillstreakID).getTeamName() + TextFormatting.GOLD + ") with a killstreak of " + longestKillstreak);
		}
	}
	
	public void startSiege() {
		for(SiegeTeam team : siege.teams()) {
			team.score = team.antiscore = 0;
		}
		zones.sort(null);
		isSequential = false;
		for(Rule rule : rules) {
			if (rule instanceof RuleSequentialDomination) {
				isSequential = true;
				orderedZones = zones.iterator();
			}
		}
		for(ZoneControl zone : zones) {
			zone.attackers = null;
			zone.occupiers = null;
			zone.ticksHeld = 0;
		}
	}

	public CommandException generateExceptionParticular() {
		return new CommandException(new StringTextComponent(String.format("Siege %s cannot be started - it requires a location, at least one team, and at least one zone", siege.getSiegeName())));
	}

}
