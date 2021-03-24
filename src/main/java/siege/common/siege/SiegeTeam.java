package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class SiegeTeam
{
	private Siege theSiege;
	private String teamName;
	private List<UUID> teamPlayers = new ArrayList<>();
	private List<UUID> teamKits = new ArrayList<>();
	private Map<UUID, Integer> teamKitLimits = new HashMap<>();
	
	private int respawnX;
	private int respawnY;
	private int respawnZ;
	
	private int teamKills;
	private int teamDeaths;

	public SiegeTeam(Siege siege)
	{
		theSiege = siege;
	}
	
	public SiegeTeam(Siege siege, String s)
	{
		this(siege);
		teamName = s;
	}
	
	public void remove()
	{
		theSiege = null;
	}
	
	public String getTeamName()
	{
		return teamName;
	}
	
	public void rename(String s)
	{
		teamName = s;
		theSiege.markDirty();
	}
	
	public boolean containsPlayer(PlayerEntity entityplayer)
	{
		return containsPlayer(entityplayer.getUniqueID());
	}
	
	public boolean containsPlayer(UUID playerID)
	{
		return teamPlayers.contains(playerID);
	}
	
	public List<UUID> getPlayerList()
	{
		return teamPlayers;
	}
	
	public int onlinePlayerCount()
	{
		int i = 0;
		List<ServerPlayerEntity> playerList = theSiege.world().getServer().getPlayerList().getPlayers();
		for (ServerPlayerEntity player : playerList)
		{
			if (containsPlayer(player))
			{
				i++;
			}
		}
		return i;
	}
	
	public boolean canPlayerJoin(PlayerEntity entityplayer)
	{
		int count = onlinePlayerCount();
		int lowestCount = theSiege.getSmallestTeamSize();
		if (count - lowestCount >= theSiege.getMaxTeamDifference())
		{
			return false;
		}
		
		return true;
	}
	
	public void joinPlayer(PlayerEntity entityplayer)
	{
		if (!containsPlayer(entityplayer))
		{
			UUID playerID = entityplayer.getUniqueID();
			teamPlayers.add(playerID);
			theSiege.markDirty();
		}
	}
	
	public void leavePlayer(PlayerEntity entityplayer)
	{
		if (containsPlayer(entityplayer))
		{
			UUID playerID = entityplayer.getUniqueID();
			teamPlayers.remove(playerID);
			theSiege.markDirty();
		}
	}
	
	public void clearPlayers()
	{
		teamPlayers.clear();
		theSiege.markDirty();
	}
	
	public Kit getRandomKit(Random random)
	{
		List<Kit> availableKits = new ArrayList<>();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null && isKitAvailable(kit))
			{
				availableKits.add(kit);
			}
		}
		
		if (availableKits.isEmpty())
		{
			return null;
		}
		
		Kit kit = availableKits.get(random.nextInt(availableKits.size()));
		return kit;
	}
	
	public boolean containsKit(Kit kit)
	{
		return teamKits.contains(kit.getKitID());
	}
	
	public void addKit(Kit kit)
	{
		teamKits.add(kit.getKitID());
		theSiege.markDirty();
	}
	
	public void removeKit(Kit kit)
	{
		teamKits.remove(kit.getKitID());
		theSiege.markDirty();
	}
	
	public boolean isKitLimited(Kit kit)
	{
		return getKitLimit(kit) >= 0;
	}
	
	public int getKitLimit(Kit kit)
	{
		UUID kitID = kit.getKitID();
		if (teamKitLimits.containsKey(kitID))
		{
			return teamKitLimits.get(kitID);
		}
		return -1;
	}
	
	public void limitKit(Kit kit, int limit)
	{
		teamKitLimits.put(kit.getKitID(), limit);
		theSiege.markDirty();
	}
	
	public void unlimitKit(Kit kit)
	{
		teamKitLimits.remove(kit.getKitID());
		theSiege.markDirty();
	}
	
	public boolean isKitAvailable(Kit kit)
	{
		if (isKitLimited(kit))
		{
			int limit = getKitLimit(kit);
			int using = countPlayersUsingKit(kit);
			if (using >= limit)
			{
				return false;
			}
		}
		return true;
	}
	
	private int countPlayersUsingKit(Kit kit)
	{
		UUID kitID = kit.getKitID();
		int i = 0;
		for (UUID player : teamPlayers)
		{
			SiegePlayerData playerData = theSiege.getPlayerData(player);
			if (playerData != null && kitID.equals(playerData.getChosenKit()))
			{
				i++;
			}
		}
		return i;
	}
	
	public List<String> listKitNames()
	{
		List<String> names = new ArrayList<>();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null)
			{
				names.add(kit.getKitName());
			}
		}
		return names;
	}
	
	public List<String> listUnincludedKitNames()
	{
		List<String> names = KitDatabase.getAllKitNames();
		names.removeAll(listKitNames());
		return names;
	}
	
	public BlockPos getRespawnPoint()
	{
		return new BlockPos(respawnX, respawnY, respawnZ);
	}
	
	public void setRespawnPoint(int i, int j, int k)
	{
		respawnX = i;
		respawnY = j;
		respawnZ = k;
		theSiege.markDirty();
	}
	
	public int getTeamKills()
	{
		return teamKills;
	}
	
	public void addTeamKill()
	{
		teamKills++;
		theSiege.markDirty();
	}
	
	public int getTeamDeaths()
	{
		return teamDeaths;
	}
	
	public void addTeamDeath()
	{
		teamDeaths++;
		theSiege.markDirty();
	}
	
	// TODO : Vinyarion's addon start
	public int score = 0;
	public TextFormatting color = TextFormatting.WHITE;
	public int antiscore = 0;
	public Siege getSiege() {
		return this.theSiege;
	}
	public List<UUID> shadowTeamPlayers = new ArrayList<>();
	// Addon end
	
	public String getSiegeOngoingScore()
	{
		// TODO : Vinyarion's addon start
		String moddedScore = theSiege.mode.score(theSiege.world(), theSiege, this);
		if (moddedScore != null) return moddedScore;
		// Addon end
		return teamName + ": Kills: " + teamKills;
	}
	
	public String getSiegeEndMessage()
	{
		UUID mvpID = null;
		int mvpKills = 0;
		int mvpDeaths = 0;
		int mvpScore = Integer.MIN_VALUE;
		for (UUID player : teamPlayers)
		{
			SiegePlayerData playerData = theSiege.getPlayerData(player);
			int kills = playerData.getKills();
			int deaths = playerData.getDeaths();
			int score = kills - deaths;
			if (score > mvpScore || (score == mvpScore && deaths < mvpDeaths))
			{
				mvpID = player;
				mvpKills = kills;
				mvpDeaths = deaths;
				mvpScore = score;
			}
		}
		
		String message = teamName + ": Kills: " + teamKills + ", Deaths: " + teamDeaths;
		// TODO : Vinyarion's addon start
		message = theSiege.mode.endMessage(theSiege, this, message);
		// Addon end
		if (mvpID != null)
		{
			String mvp = UsernameCache.getLastKnownUsername(mvpID);
			message += (", MVP: " + mvp + " with " + mvpKills + " kills / " + mvpDeaths + " deaths");
		}
		return message;
	}
	
	public void onSiegeEnd()
	{
		teamPlayers.clear();
		teamKills = 0;
		teamDeaths = 0;
		theSiege.markDirty();
	}
	
	public void writeToNBT(CompoundNBT nbt)
	{
		nbt.putString("Name", teamName);
		
		ListNBT playerTags = new ListNBT();
		for (UUID player : teamPlayers)
		{
			playerTags.add(StringNBT.valueOf(player.toString()));
		}
		nbt.put("Players", playerTags);
		
		ListNBT kitTags = new ListNBT();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null)
			{
				CompoundNBT kitData = new CompoundNBT();
				String kitName = kit.getKitName();
				kitData.putString("Name", kitName);
				if (teamKitLimits.containsKey(kitID))
				{
					int limit = teamKitLimits.get(kitID);
					kitData.putInt("Limit", limit);
				}
				kitTags.add(kitData);
			}
		}
		nbt.put("TeamKits", kitTags);
		
		nbt.putInt("RespawnX", respawnX);
		nbt.putInt("RespawnY", respawnY);
		nbt.putInt("RespawnZ", respawnZ);
		
		nbt.putInt("Kills", teamKills);
		nbt.putInt("Deaths", teamDeaths);
		// TODO : Vinyarion's addon start
		nbt.putInt("VinyarionAddon_Score", score);
		nbt.putInt("VinyarionAddon_Color", color.ordinal());
		for (UUID player : teamPlayers) {
			if(!shadowTeamPlayers.contains(player)) {
				shadowTeamPlayers.add(player);
			}
		}
		ListNBT shadowPlayerTags = new ListNBT();
		for (UUID player : shadowTeamPlayers) {
			shadowPlayerTags.add(StringNBT.valueOf(player.toString()));
		}
		nbt.put("VinyarionAddon_ShadowPlayers", shadowPlayerTags);
		// Addon end
	}
	
	public void readFromNBT(CompoundNBT nbt)
	{
		teamName = nbt.getString("Name");
		
		teamPlayers.clear();
		if (nbt.contains("Players", Constants.NBT.TAG_LIST))
		{
			ListNBT playerTags = nbt.getList("Players", Constants.NBT.TAG_STRING);
			for (int i = 0; i < playerTags.size(); i++)
			{
				UUID player = UUID.fromString(playerTags.get(i).getString());
				if (player != null)
				{
					teamPlayers.add(player);
				}
			}
		}
		
		teamKits.clear();
		teamKitLimits.clear();
		if (nbt.contains("TeamKits", Constants.NBT.TAG_LIST))
		{
			ListNBT kitTags = nbt.getList("TeamKits", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < kitTags.size(); i++)
			{
				CompoundNBT kitData = kitTags.getCompound(i);
				String kitName = kitData.getString("Name");
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					teamKits.add(kit.getKitID());
					if (kitData.contains("Limit", Constants.NBT.TAG_INT))
					{
						int limit = kitData.getInt("Limit");
						teamKitLimits.put(kit.getKitID(), limit);
					}
				}
			}
		}
		else if (nbt.contains("Kits", Constants.NBT.TAG_LIST))
		{
			ListNBT kitTags = nbt.getList("Kits", Constants.NBT.TAG_STRING);
			for (int i = 0; i < kitTags.size(); i++)
			{
				String kitName = kitTags.getString(i);
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					teamKits.add(kit.getKitID());
				}
			}
		}
		
		respawnX = nbt.getInt("RespawnX");
		respawnY = nbt.getInt("RespawnY");
		respawnZ = nbt.getInt("RespawnZ");
		
		teamKills = nbt.getInt("Kills");
		teamDeaths = nbt.getInt("Deaths");
		// TODO : Vinyarion's addon start
		score = nbt.getInt("VinyarionAddon_Score");
		int c = nbt.getInt("VinyarionAddon_Color");
		color = TextFormatting.values()[c < TextFormatting.values().length ? c >= 0 ? c : 0 : 0];
		if (nbt.contains("VinyarionAddon_ShadowPlayers", Constants.NBT.TAG_LIST)) {
			ListNBT playerTags = nbt.getList("VinyarionAddon_ShadowPlayers", Constants.NBT.TAG_STRING);
			for (int i = 0; i < playerTags.size(); i++) {
				UUID player = UUID.fromString(playerTags.getString(i));
				if (player != null) {
					shadowTeamPlayers.add(player);
				}
			}
		}
		// Addon end
	}
}
