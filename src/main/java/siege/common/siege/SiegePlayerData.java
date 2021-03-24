package siege.common.siege;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.ScoreCriteria.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import siege.common.addon.AddonPlayerData;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class SiegePlayerData
{
	private Siege theSiege;

	private BackupSpawnPoint backupSpawnPoint;
	private UUID currentKit;
	private UUID chosenKit;
	private boolean clearedLimitedKit = false;
	private String nextTeam;
	
	private int kills;
	private int deaths;
	private int killstreak;
	private int longestKillstreak;
	private UUID lastKilledBy;
	private UUID lastKill;
	private Map<String, Integer> killedByTable = new HashMap<>();
	private Map<String, Integer> killTable = new HashMap<>();
	
	@SuppressWarnings("unused")
	private ScoreObjective lastSentSiegeObjective = null;
	// TODO : Vinyarion's Addon start
	public final AddonPlayerData addonData = new AddonPlayerData(this, theSiege);
	// Addon end

	public SiegePlayerData(Siege siege)
	{
		theSiege = siege;
	}
	
	public void writeToNBT(CompoundNBT nbt)
	{
		if (backupSpawnPoint != null ? backupSpawnPoint.spawnCoords != null : false)
		{
			nbt.putString("BSP_Dim", backupSpawnPoint.dimension.getRegistryName().toString());
			BlockPos bspCoords = backupSpawnPoint.spawnCoords;
			nbt.putInt("BSP_X", bspCoords.getX());
			nbt.putInt("BSP_Y", bspCoords.getY());
			nbt.putInt("BSP_Z", bspCoords.getZ());
			nbt.putBoolean("BSP_Forced", backupSpawnPoint.spawnForced);
		}
		
		if (currentKit != null)
		{
			nbt.putString("CurrentKit", currentKit.toString());
		}
		
		if (chosenKit != null)
		{
			nbt.putString("Kit", chosenKit.toString());
		}
		
		nbt.putBoolean("ClearedLimitedKit", clearedLimitedKit);
		
		if (nextTeam != null)
		{
			nbt.putString("NextTeam", nextTeam);
		}
		
		nbt.putInt("Kills", kills);
		nbt.putInt("Deaths", deaths);
		nbt.putInt("Killstreak", killstreak);
		nbt.putInt("LongestKillstreak", longestKillstreak);
		// TODO : Vinyarion's Addon start
		addonData.toNBT(nbt);
		// Addon end
		
		if (lastKilledBy != null)
		{
			nbt.putString("LastKilledBy", lastKilledBy.toString());
		}
		if (lastKill != null)
		{
			nbt.putString("LastKill", lastKill.toString());
		}
		
		ListNBT killedByTags = new ListNBT();
		for (Entry<String, Integer> e : killedByTable.entrySet())
		{
			String name = e.getKey();
			int count = e.getValue();
			CompoundNBT data = new CompoundNBT();
			data.putString("Name", name);
			data.putInt("Count", count);
			killedByTags.add(data);
		}
		nbt.put("KilledByTable", killedByTags);
		
		ListNBT killTags = new ListNBT();
		for (Entry<String, Integer> e : killTable.entrySet())
		{
			String name = e.getKey();
			int count = e.getValue();
			CompoundNBT data = new CompoundNBT();
			data.putString("Name", name);
			data.putInt("Count", count);
			killTags.add(data);
		}
		nbt.put("KillTable", killTags);
	}
	
	public void readFromNBT(CompoundNBT nbt)
	{
		backupSpawnPoint = null;
		if (nbt.contains("BSP_Dim", Constants.NBT.TAG_INT))
		{
			DimensionType bspDim = DimensionType.byName(new ResourceLocation(nbt.getString("BSP_Dim")));
			int bspX = nbt.getInt("BSP_X");
			int bspY = nbt.getInt("BSP_Y");
			int bspZ = nbt.getInt("BSP_Z");
			boolean bspForced = nbt.getBoolean("BSP_Forced");
			BlockPos bspCoords = new BlockPos(bspX, bspY, bspZ);
			backupSpawnPoint = new BackupSpawnPoint(bspDim, bspCoords, bspForced);
		}
		
		if (nbt.contains("CurrentKit", Constants.NBT.TAG_STRING))
		{
			currentKit = UUID.fromString(nbt.getString("CurrentKit"));
		}
		
		if (nbt.contains("Kit", Constants.NBT.TAG_STRING))
		{
			chosenKit = UUID.fromString(nbt.getString("Kit"));
		}
		
		clearedLimitedKit = nbt.getBoolean("ClearedLimitedKit");
		
		nextTeam = nbt.getString("NextTeam");
		
		kills = nbt.getInt("Kills");
		deaths = nbt.getInt("Deaths");
		killstreak = nbt.getInt("Killstreak");
		longestKillstreak = nbt.getInt("LongestKillstreak");
		// TODO : Vinyarion's Addon start
		addonData.fromNBT(nbt);
		// Addon end
		
		lastKilledBy = null;
		if (nbt.contains("LastKilledBy", Constants.NBT.TAG_STRING))
		{
			lastKilledBy = UUID.fromString(nbt.getString("LastKilledBy"));
		}
		lastKill = null;
		if (nbt.contains("LastKill", Constants.NBT.TAG_STRING))
		{
			lastKill = UUID.fromString(nbt.getString("LastKill"));
		}
		
		killedByTable.clear();
		ListNBT killedByTags = nbt.getList("KilledByTable", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < killedByTags.size(); i++)
		{
			CompoundNBT data = killedByTags.getCompound(i);
			String name = data.getString("Name");
			int count = data.getInt("Count");
			if (count > 0)
			{
				killedByTable.put(name, count);
			}
		}
		
		killTable.clear();
		ListNBT killTags = nbt.getList("KillTable", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < killTags.size(); i++)
		{
			CompoundNBT data = killTags.getCompound(i);
			String name = data.getString("Name");
			int count = data.getInt("Count");
			if (count > 0)
			{
				killTable.put(name, count);
			}
		}
	}
	
	public BackupSpawnPoint getBackupSpawnPoint()
	{
		return backupSpawnPoint;
	}
	
	public void setBackupSpawnPoint(BackupSpawnPoint bsp)
	{
		backupSpawnPoint = bsp;
		theSiege.markDirty();
	}
	
	public UUID getCurrentKit()
	{
		return currentKit;
	}

	public void setCurrentKit(Kit kit)
	{
		currentKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public UUID getChosenKit()
	{
		return chosenKit;
	}

	public void setChosenKit(Kit kit)
	{
		chosenKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public void setRandomChosenKit()
	{
		setChosenKit(null);
	}
	
	public String getNextTeam()
	{
		return nextTeam;
	}

	public void setNextTeam(String team)
	{
		nextTeam = team;
		theSiege.markDirty();
	}
	
	public int getKills()
	{
		return kills;
	}
	
	public void onKill(PlayerEntity entityplayer)
	{
		kills++;
		killstreak++;
		if (killstreak > longestKillstreak)
		{
			longestKillstreak = killstreak;
		}
		
		lastKill = entityplayer.getUniqueID();
		
		String name = entityplayer.getScoreboardName();
		int tableCount = killTable.containsKey(name) ? killTable.get(name) : 0;
		tableCount++;
		killTable.put(name, tableCount);
		
		theSiege.markDirty();
	}
	
	public int getDeaths()
	{
		return deaths;
	}
	
	public void onDeath(PlayerEntity entityplayer)
	{
		deaths++;
		killstreak = 0;
		lastKill = null;
		
		if (entityplayer == null)
		{
			lastKilledBy = null;
		}
		else
		{
			lastKilledBy = entityplayer.getUniqueID();
			
			String name = entityplayer.getScoreboardName();
			int tableCount = killedByTable.containsKey(name) ? killedByTable.get(name) : 0;
			tableCount++;
			killedByTable.put(name, tableCount);
		}

		theSiege.markDirty();
	}
	
	public int getKillstreak()
	{
		return killstreak;
	}
	
	public int getLongestKillstreak()
	{
		return longestKillstreak;
	}
	
	public void onTeamChange()
	{
		kills = 0;
		deaths = 0;
		killstreak = 0;
		longestKillstreak = 0;
		lastKilledBy = null;
		lastKill = null;
		killTable.clear();
		killedByTable.clear();
		theSiege.markDirty();
	}
	
	public UUID getLastKilledBy()
	{
		return lastKilledBy;
	}
	
	public UUID getLastKill()
	{
		return lastKill;
	}
	
	public String getMostKilledBy()
	{
		String mostName = null;
		int most = 0;
		boolean dupe = false;
		for (Entry<String, Integer> e : killedByTable.entrySet())
		{
			String name = e.getKey();
			int count = e.getValue();
			if (count > 0)
			{
				if (count == most)
				{
					mostName = name;
					dupe = true;
				}
				else if (count > most)
				{
					mostName = name;
					most = count;
					dupe = false;
				}
			}
		}
		
		if (dupe)
		{
			return null;
		}
		else
		{
			return mostName;
		}
	}
	
	public String getMostKilled()
	{
		String mostName = null;
		int most = 0;
		boolean dupe = false;
		for (Entry<String, Integer> e : killTable.entrySet())
		{
			String name = e.getKey();
			int count = e.getValue();
			if (count > 0)
			{
				if (count == most)
				{
					mostName = name;
					dupe = true;
				}
				else if (count > most)
				{
					mostName = name;
					most = count;
					dupe = false;
				}
			}
		}
		
		if (dupe)
		{
			return null;
		}
		else
		{
			return mostName;
		}
	}
	
	public void onLogin(ServerPlayerEntity entityplayer)
	{
		if (clearedLimitedKit)
		{
			clearedLimitedKit = false;
			Siege.warnPlayer(entityplayer, "Your limited kit was deselected on logout so others may use it!");
			Siege.warnPlayer(entityplayer, "Switching to random kit selection after death");
			theSiege.markDirty();
		}
	}
	
	public void onLogout(ServerPlayerEntity entityplayer)
	{
		lastSentSiegeObjective = null;
		
		SiegeTeam team = theSiege.getPlayerTeam(entityplayer);
		if (team != null)
		{
			Kit kit = KitDatabase.getKit(chosenKit);
			if (kit != null && team.isKitLimited(kit))
			{
				clearedLimitedKit = true;
				setRandomChosenKit();
				theSiege.markDirty();
			}
		}
	}
	
	public void updateSiegeScoreboard(ServerPlayerEntity entityplayer, boolean forceClear)
	{
		World world = entityplayer.world;
		SiegeTeam team = theSiege.getPlayerTeam(entityplayer);

		Scoreboard scoreboard = world.getScoreboard();
		ScoreObjective siegeObjective = null;
		
		// TODO: change this to account for when the siege ends: remove scoreboards / start a timer etc.
		// Addon: fulfill above
		if (forceClear)
		{
			IPacket<?> pktDisplay = new SDisplayObjectivePacket(1, null);
			entityplayer.connection.sendPacket(pktDisplay);
		}
		// Addon end
		boolean inSiege = team != null;
		if (inSiege && !forceClear)
		{
			// create a new siege objective, with a new name, so we can send all the scores one by one, and only then display it
			String newObjName = "siege" + Siege.siegeObjectiveNumber;
			Siege.siegeObjectiveNumber++;
			StringTextComponent displayName = new StringTextComponent("SiegeMode: " + theSiege.getSiegeName());
			siegeObjective = new ScoreObjective(scoreboard, newObjName, ScoreCriteria.DUMMY, displayName, RenderType.INTEGER);
			
			String kitName = "";
			Kit currentKit = KitDatabase.getKit(getCurrentKit());
			if (currentKit != null)
			{
				kitName = currentKit.getKitName();
			}
			
			String timeRemaining = theSiege.isActive() ? ("Time: " + Siege.ticksToTimeString(theSiege.getTicksRemaining())) : "Ended";
			
			// clever trick to control the ordering of the objectives: put actual scores in the 'playernames', and put the desired order in the 'scores'!
			
			List<Score> allSiegeStats = new ArrayList<>();
			allSiegeStats.add(new Score(scoreboard, siegeObjective, timeRemaining));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team: " + team.color + team.getTeamName()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Kit: " + kitName));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Kills: " + getKills()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Deaths: " + getDeaths()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Killstreak: " + getKillstreak()));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team K: " + team.getTeamKills()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team D: " + team.getTeamDeaths()));
			
			// TODO : Vinyarion's addon start
			theSiege.mode.scoreboard(allSiegeStats, scoreboard, siegeObjective, timeRemaining, team, entityplayer, this);
			// Addon end
			
			// recreate the siege objective (or create for first time if not sent before)
			IPacket<?> pktObjective = new SScoreboardObjectivePacket(siegeObjective, 0);
			entityplayer.connection.sendPacket(pktObjective);
			
			int index = allSiegeStats.size();
			int gaps = 0;
			for (Score score : allSiegeStats)
			{
				if (score == null)
				{
					// create a unique gap string, based on how many gaps we've already had
					String gapString = "";
					for (int l = 0; l <= gaps; l++)
					{
						gapString += "-";
					}
					score = new Score(scoreboard, siegeObjective, gapString);
					gaps++;
				}
				
				// avoid string too long in packet // 1.15: not needed
				String scoreName = score.getPlayerName();
				int maxLength = 64;//16;
				if (scoreName.length() > maxLength)
				{
					scoreName = scoreName.substring(0, Math.min(scoreName.length(), maxLength));
				}
				score = new Score(score.getScoreScoreboard(), score.getObjective(), scoreName);
				
				score.setScorePoints(index);
				IPacket<?> pktScore = new SUpdateScorePacket(ServerScoreboard.Action.CHANGE, score.getObjective().getName(), score.getPlayerName(), index);
				entityplayer.connection.sendPacket(pktScore);
				index--;
			}
		}
		
		// try disabling this to avoid the rare crash when the last objective has failed to send and it tries to remove a nonexistent objective
		// remove last objective only AFTER sending new objective & all scores
		/*if (lastSentSiegeObjective != null)
		{
			IPacket<?> pkt = new SDisplayObjectivePacket(1, lastSentSiegeObjective);
			entityplayer.connection.sendPacket(pkt);
			lastSentSiegeObjective = null;
		}*/
		
		// if a new objective was sent, display it
		if (siegeObjective != null)
		{
			IPacket<?> pktDisplay = new SDisplayObjectivePacket(1, siegeObjective);
			entityplayer.connection.sendPacket(pktDisplay);
			lastSentSiegeObjective = siegeObjective;
		}
	}
}
