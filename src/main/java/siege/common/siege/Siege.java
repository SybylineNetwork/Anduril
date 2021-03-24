package siege.common.siege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants;
import siege.common.SiegeModeMain;
import siege.common.addon.AddonHooks;
import siege.common.addon.AddonTeleporter;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;
import siege.common.mode.Mode;
import siege.common.mode.ModeDefault;

public class Siege
{
	private boolean needsSave = false;
	private boolean deleted = false;
	
	private UUID siegeID;
	private String siegeName;
	
	private boolean isLocationSet = false;
	private DimensionType dimension = DimensionType.OVERWORLD;
	private int xPos;
	private int zPos;
	private int radius;
	public static final int MAX_RADIUS = 2000;
	private static final double EDGE_PUT_RANGE = 2D;
	
	private int ticksRemaining = 0;
	private static final int SCORE_INTERVAL = 30 * 20;
	private boolean announceActive = true;
	private static final int ANNOUNCE_ACTIVE_INTERVAL = 60 * 20;
	
	private List<SiegeTeam> siegeTeams = new ArrayList<>();
	private int maxTeamDifference = 3;
	private boolean friendlyFire = false;
	private boolean mobSpawning = false;
	private boolean terrainProtect = true;
	private boolean terrainProtectInactive = false;
	private boolean dispelOnEnd = false;
	
	private Map<UUID, SiegePlayerData> playerDataMap = new HashMap<>();
	private static final int KILLSTREAK_ANNOUNCE = 3;
	private int respawnImmunity = 5;
	
	// required to ensure each sent scoreboard objective is a unique objective
	public static int siegeObjectiveNumber = 0;
	
	public Siege(String s)
	{
		siegeID = UUID.randomUUID();
		siegeName = s;
	}
	
	public UUID getSiegeID()
	{
		return siegeID;
	}
	
	public String getSiegeName()
	{
		return siegeName;
	}
	
	public void rename(String s)
	{
		String oldName = siegeName;
		siegeName = s;
		markDirty();
		SiegeDatabase.renameSiege(this, oldName);
	}
	
	public void setCoords(DimensionType dim, int x, int z, int r)
	{
		dimension = dim;
		xPos = x;
		zPos = z;
		radius = r;
		isLocationSet = true;
		markDirty();
	}
	
	public boolean isLocationInSiege(Vec3d vec)
	{
		double dx = vec.x - (xPos + 0.5D);
		double dz = vec.z - (zPos + 0.5D);
		double dSq = dx * dx + dz * dz;
		return dSq <= (double)radius * (double)radius;
	}
	
	public SiegeTeam getTeam(String teamName)
	{
		for (SiegeTeam team : siegeTeams)
		{
			if (team.getTeamName().equals(teamName))
			{
				return team;
			}
		}
		return null;
	}
	
	public void createNewTeam(String teamName)
	{
		SiegeTeam team = new SiegeTeam(this, teamName);
		siegeTeams.add(team);
		markDirty();
	}
	
	public boolean removeTeam(String teamName)
	{
		SiegeTeam team = getTeam(teamName);
		if (team != null)
		{
			siegeTeams.remove(team);
			team.remove();
			markDirty();
			return true;
		}
		return false;
	}
	
	public List<String> listTeamNames()
	{
		List<String> names = new ArrayList<>();
		for (SiegeTeam team : siegeTeams)
		{
			names.add(team.getTeamName());
		}
		return names;
	}
	
	public int getSmallestTeamSize()
	{
		boolean flag = false;
		int smallestSize = -1;
		for (SiegeTeam team : siegeTeams)
		{
			int size = team.onlinePlayerCount();
			if (!flag || size < smallestSize)
			{
				smallestSize = size;
			}
			flag = true;
		}
		return smallestSize;
	}
	
	public boolean hasPlayer(PlayerEntity entityplayer)
	{
		return getPlayerTeam(entityplayer) != null;
	}
	
	public SiegeTeam getPlayerTeam(PlayerEntity entityplayer)
	{
		return getPlayerTeam(entityplayer.getUniqueID());
	}
	
	public SiegeTeam getPlayerTeam(UUID playerID)
	{
		for (SiegeTeam team : siegeTeams)
		{
			if (team.containsPlayer(playerID))
			{
				return team;
			}
		}
		return null;
	}
	
	public SiegePlayerData getPlayerData(PlayerEntity entityplayer)
	{
		return getPlayerData(entityplayer.getUniqueID());
	}
	
	public SiegePlayerData getPlayerData(UUID player)
	{
		SiegePlayerData data = playerDataMap.get(player);
		if (data == null)
		{
			data = new SiegePlayerData(this);
			playerDataMap.put(player, data);
		}
		return data;
	}
	
	public List<String> listAllPlayerNames()
	{
		List<String> names = new ArrayList<>();
		List<ServerPlayerEntity> playerList = SiegeModeMain.instance.getServer().getPlayerList().getPlayers();
		for (ServerPlayerEntity player : playerList)
		{
			if (hasPlayer(player))
			{
				names.add(player.getScoreboardName());
			}
		}
		return names;
	}
	
	public int getMaxTeamDifference()
	{
		return maxTeamDifference;
	}
	
	public void setMaxTeamDifference(int d)
	{
		maxTeamDifference = d;
		markDirty();
	}
	
	public int getRespawnImmunity()
	{
		return respawnImmunity;
	}
	
	public void setRespawnImmunity(int seconds)
	{
		respawnImmunity = seconds;
		markDirty();
	}
	
	public boolean getFriendlyFire()
	{
		return friendlyFire;
	}
	
	public void setFriendlyFire(boolean flag)
	{
		friendlyFire = flag;
		markDirty();
	}
	
	public boolean getMobSpawning()
	{
		return mobSpawning;
	}
	
	public void setMobSpawning(boolean flag)
	{
		mobSpawning = flag;
		markDirty();
	}
	
	public boolean getTerrainProtect()
	{
		return terrainProtect;
	}
	
	public void setTerrainProtect(boolean flag)
	{
		terrainProtect = flag;
		markDirty();
	}
	
	public boolean getTerrainProtectInactive()
	{
		return terrainProtectInactive;
	}
	
	public void setTerrainProtectInactive(boolean flag)
	{
		terrainProtectInactive = flag;
		markDirty();
	}
	
	public boolean getDispelEnd()
	{
		return dispelOnEnd;
	}
	
	public void setDispelOnEnd(boolean flag)
	{
		dispelOnEnd = flag;
		markDirty();
	}
	
	public boolean isSiegeWorld(World world)
	{
		return world.dimension.getType() == dimension;
	}
	
	public boolean canBeStarted()
	{
		// TODO : Vinyarion's Addon start
		if(!mode.isReady()) return false;
		// Addon end
		return isLocationSet && !siegeTeams.isEmpty();
	}
	
	public void startSiege(int duration)
	{
		playerDataMap.clear();
		for (SiegeTeam team : siegeTeams)
		{
			team.clearPlayers();
		}
		ticksRemaining = duration;
		// TODO : Vinyarion's Addon start
		mode.startSiege();
		// Addon end
		markDirty();
		
		announceActiveSiege();
	}
	
	public void extendSiege(int duration)
	{
		if (isActive())
		{
			ticksRemaining += duration;
			markDirty();
		}
	}
	
	public int getTicksRemaining()
	{
		return ticksRemaining;
	}
	
	public static String ticksToTimeString(int ticks)
	{
		int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        
        String sSeconds = String.valueOf(seconds);
        if (sSeconds.length() < 2)
        {
        	sSeconds = "0" + sSeconds;
        }
        
        String sMinutes = String.valueOf(minutes);
        
        String timeDisplay = sMinutes + ":" + sSeconds;
        return timeDisplay;
	}
	
	public void endSiege()
	{
		ticksRemaining = 0;

		announceToAllPlayers("The siege has ended!");
		
		List<SiegeTeam> winningTeams = new ArrayList<>();
		int winningScore = -1;
		for (SiegeTeam team : siegeTeams)
		{
			/* TODO : Vinyarion's Addon replace start
			int score = team.getTeamKills();
			*/
			int score = mode.scoringMethod(this, team);
			// Addon end
			if (score > winningScore)
			{
				winningScore = score;
				winningTeams.clear();
				winningTeams.add(team);
			}
			else if (score == winningScore)
			{
				winningTeams.add(team);
			}
		}
		String winningTeamName = "";
		if (!winningTeams.isEmpty())
		{
			if (winningTeams.size() == 1)
			{
				SiegeTeam team = winningTeams.get(0);
				winningTeamName = team.color + team.getTeamName();
			}
			else
			{
				for (SiegeTeam team : winningTeams)
				{
					if (!winningTeamName.isEmpty())
					{
						winningTeamName += TextFormatting.GOLD + ", ";
					}
					winningTeamName += team.color + team.getTeamName();
				}
			}
		}
		/* TODO : Vinyarion's Addon replace start
		if (winningTeams.size() == 1)
		{
			announceToAllPlayers("Team " + winningTeamName + " won with " + winningScore + " kills!");
		}
		else
		{
			announceToAllPlayers("Teams " + winningTeamName + " tied with " + winningScore + " kills each!");
		}
		*/
		boolean plural = winningTeams.size() != 1;
		announceToAllPlayers((plural ? "Teams " : "Team ") + winningTeamName + TextFormatting.GOLD + (plural ? " tied with " : " won with ") + winningScore + " " + mode.object(this, winningScore != 1) + (plural ? " each!" : "!"));
		// Addon end
		announceToAllPlayers("---");
		for (SiegeTeam team : siegeTeams)
		{
			String teamMsg = team.getSiegeEndMessage();
			announceToAllPlayers(teamMsg);
		}
		announceToAllPlayers("---");
		// TODO : Vinyarion's addon start
		if(mode instanceof ModeDefault) {
		// Addon end
		UUID mvpID = null;
		int mvpKills = 0;
		int mvpDeaths = 0;
		int mvpScore = Integer.MIN_VALUE;
		UUID longestKillstreakID = null;
		int longestKillstreak = 0;
		for (SiegeTeam team : siegeTeams)
		{
			for (UUID player : team.getPlayerList())
			{
				SiegePlayerData playerData = getPlayerData(player);
				
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
				
				int streak = playerData.getLongestKillstreak();
				if (streak > longestKillstreak)
				{
					longestKillstreakID = player;
					longestKillstreak = streak;
				}
			}
		}
		if (mvpID != null)
		{
			String mvp = UsernameCache.getLastKnownUsername(mvpID);
			announceToAllPlayers("MVP was " + mvp + " (" + getPlayerTeam(mvpID).getTeamName() + TextFormatting.GOLD + ") with " + mvpKills + " kills / " + mvpDeaths + " deaths");
		}
		if (longestKillstreakID != null)
		{
			String streakPlayer = UsernameCache.getLastKnownUsername(longestKillstreakID);
			announceToAllPlayers("Longest killstreak was " + streakPlayer + " (" + getPlayerTeam(longestKillstreakID).getTeamName() + TextFormatting.GOLD + ") with a killstreak of " + longestKillstreak);
		}
		// TODO : Vinyarion's addon start
		} else {
			mode.printMVP(this, siegeTeams);
		}
		// Addon end
		announceToAllPlayers("---");
		// TODO : Vinyarion's Addon start
		// announceToAllPlayers("Congratulations to " + winningTeamName + ", and well played by all!");
		announceToAllPlayers("Congratulations to " + winningTeamName + TextFormatting.GOLD + ", and well played by all!");
		// Addon end
		List<ServerPlayerEntity> playerList = this.world().getServer().getPlayerList().getPlayers();
		for (ServerPlayerEntity player : playerList)
		{
			if (hasPlayer(player))
			{
				SiegePlayerData playerData = getPlayerData(player);
				
				boolean flag = false;
				
				String mostKilled = playerData.getMostKilled();
				String mostKilledBy = playerData.getMostKilledBy();
				if (mostKilled != null)
				{
					announcePlayer(player, "You slew " + mostKilled + " most");
					flag = true;
				}
				if (mostKilledBy != null)
				{
					announcePlayer(player, "You were slain most by " + mostKilledBy);
					flag = true;
				}
				if (mostKilled != null && mostKilled.equals(mostKilledBy))
				{
					announcePlayer(player, "Your nemesis was " + mostKilled + "...");
					flag = true;
				}
				
				if (flag)
				{
					announcePlayer(player, "---");
				}
			}
		}
		
		//announceToAllPlayers("Congratulations to " + winningTeamName + ", and well played by all!");
		
		for (ServerPlayerEntity player : playerList)
		{
			if (hasPlayer(player))
			{
				leavePlayer(player, true);
			}
		}
		playerDataMap.clear();
		
		for (SiegeTeam team : siegeTeams)
		{
			team.onSiegeEnd();
		}
		
		markDirty();
	}
	
	public boolean isActive()
	{
		return ticksRemaining > 0;
	}
	
	public void updateSiege(World world)
	{
		if (isActive())
		{
			ticksRemaining--;
			if (world.getServer().getTickCounter() % 100 == 0)
			{
				markDirty();
			}
			
			// TODO : Vinyarion's addon start
			boolean flag;
			if (flag = mode.tick()) {
				mode.preEndSiege();
			}
			// Addon end, but added the flag thing below
			
			if (ticksRemaining <= 0 || flag)
			{
				endSiege();
			}
			else
			{
				if (announceActive && ticksRemaining % ANNOUNCE_ACTIVE_INTERVAL == 0)
				{
					announceActiveSiege();
				}
				
				List<? extends PlayerEntity> playerList = world.getPlayers();
				for (PlayerEntity player : playerList)
				{
					ServerPlayerEntity entityplayer = (ServerPlayerEntity)player;
					boolean inSiege = hasPlayer(entityplayer);
					updatePlayer(entityplayer, inSiege);
				}
				
				if (ticksRemaining % SCORE_INTERVAL == 0)
				{
					List<SiegeTeam> teamsSorted = new ArrayList<>();
					teamsSorted.addAll(siegeTeams);
					Collections.sort(teamsSorted, new Comparator<SiegeTeam>()
					{
						@Override
						public int compare(SiegeTeam team1, SiegeTeam team2)
						{
							int score1 = team1.getTeamKills();
							int score2 = team2.getTeamKills();
							if (score1 > score2)
							{
								return -1;
							}
							else if (score1 < score2)
							{
								return 1;
							}
							else
							{
								return team1.getTeamName().compareTo(team2.getTeamName());
							}
						}
					});
					
					for (SiegeTeam team : teamsSorted)
					{
						warnAllPlayers(team.getSiegeOngoingScore());
					}
				}
			}
		}
	}
	
	public boolean isPlayerInDimension(PlayerEntity entityplayer)
	{
		return entityplayer.dimension == dimension;
	}
	
	public boolean joinPlayer(PlayerEntity entityplayer, SiegeTeam team, Kit kit)
	{
		boolean hasAnyItems = false;
		checkForItems:
		for (int i = 0; i < entityplayer.inventory.getSizeInventory(); i++)
		{
			ItemStack itemstack = entityplayer.inventory.getStackInSlot(i);
			if (!itemstack.isEmpty())
			{
				hasAnyItems = true;
				break checkForItems;
			}
		}
		
		// TODO : Vinyarion's addon start
		if (hasAnyItems) {
			if (kit != null) {
				if (kit.isSelfSupplied()) {
					hasAnyItems = false;
				}
			}
		}
		// Addon end
		
		if (hasAnyItems)
		{
			warnPlayer(entityplayer, "Your inventory must be clear before joining the siege!");
			warnPlayer(entityplayer, "Put your items somewhere safe");
			return false;
		}
		else
		{
			team.joinPlayer(entityplayer);

			// TODO : Vinyarion's addon start
			AddonHooks.playerJoinsSiege(entityplayer, this, team, kit);
			// Addon end
			
			BlockPos teamSpawn = team.getRespawnPoint();
			entityplayer.setPositionAndUpdate(teamSpawn.getX() + 0.5D, teamSpawn.getY(), teamSpawn.getZ() + 0.5D);
			
			if (kit != null && team.isKitAvailable(kit))
			{
				getPlayerData(entityplayer).setChosenKit(kit);
			}
			applyPlayerKit(entityplayer);
			
			return true;
		}
	}
	
	public void leavePlayer(ServerPlayerEntity entityplayer, boolean forceClearScores)
	{
		// TODO: implement a timer or something; for now scores stay until they relog, better than immediately disappearing
		getPlayerData(entityplayer).updateSiegeScoreboard(entityplayer, forceClearScores);
		
		SiegeTeam team = getPlayerTeam(entityplayer);
		team.leavePlayer(entityplayer);

		// TODO : Vinyarion's addon start
		AddonHooks.playerLeavesSiege(entityplayer, this, team);
		// TODO : Unnecessary. // shadowPlayerDataMap.put(entityplayer.getUniqueID(), playerDataMap.get(entityplayer.getUniqueID()));
		// Addon end
		
		restoreAndClearBackupSpawnPoint(entityplayer);
		// TODO : Vinyarion's addon start
		if (!KitDatabase.getKit(getPlayerData(entityplayer).getChosenKit()).isSelfSupplied())
		// Addon end
		Kit.clearPlayerInvAndKit(entityplayer);
		
		UUID playerID = entityplayer.getUniqueID();
		playerDataMap.remove(playerID);
		
		if (dispelOnEnd)
		{
			dispel(entityplayer);
		}
	}
	
	public static void warnPlayer(PlayerEntity entityplayer, String text)
	{
		messagePlayer(entityplayer, text, TextFormatting.RED);
	}
	
	private void announcePlayer(PlayerEntity entityplayer, String text)
	{
		messagePlayer(entityplayer, text, TextFormatting.GOLD);
	}
	
	public static void messagePlayer(PlayerEntity entityplayer, String text, TextFormatting color)
	{
		ITextComponent message = new StringTextComponent(text);
		message.getStyle().setColor(color);
		entityplayer.sendMessage(message);
	}
	
	public void announceToAllPlayers(String text)
	{
		messageAllPlayers(text, TextFormatting.GOLD, true);
	}
	
	public void warnAllPlayers(String text)
	{
		messageAllPlayers(text, TextFormatting.RED, true);
	}
	
	public void messageAllPlayers(String text, TextFormatting color, boolean onlyInSiege)
	{
		List<ServerPlayerEntity> playerList = this.world().getServer().getPlayerList().getPlayers();
		for (ServerPlayerEntity player : playerList)
		{
			if (!onlyInSiege || hasPlayer(player))
			{
				messagePlayer(player, text, color);
			}
		}
	}
	
	private void announceActiveSiege()
	{
		String name = getSiegeName();
		String joinMsg = "To join the active siege " + name + ", put your items somewhere safe, then do /siege_play join " + name + " [use TAB key to choose team and kit]";
		messageAllPlayers(joinMsg, TextFormatting.YELLOW, false);
	}
	
	private void updatePlayer(ServerPlayerEntity entityplayer, boolean inSiege)
	{
		World world = entityplayer.world;
		SiegePlayerData playerData = getPlayerData(entityplayer);
//		SiegeTeam team = getPlayerTeam(entityplayer);
		
		if (!entityplayer.isCreative())
		{
			boolean inSiegeRange = isLocationInSiege(entityplayer.getPositionVec());
			double dx = entityplayer.getPosX() - (xPos + 0.5D);
			double dz = entityplayer.getPosZ() - (zPos + 0.5D);
			float angle = (float)Math.atan2(dz, dx);
			
			if (inSiege)
			{
				if (!inSiegeRange)
				{
					double putRange = radius - EDGE_PUT_RANGE;
					int newX = xPos + MathHelper.floor(putRange * MathHelper.cos(angle));
					int newZ = zPos + MathHelper.floor(putRange * MathHelper.sin(angle));
//					int newY = world.getTopSolidOrLiquidBlock(newX, newZ);
					int newY = getTopSolidOrLiquidBlock(world, newX, newZ);
					entityplayer.setPositionAndUpdate(newX + 0.5D, newY + 1.5D, newZ + 0.5D);
					
					warnPlayer(entityplayer, "Stay inside the siege area!");
				}
				// TODO : IMC
//				FMLInterModComms.sendRuntimeMessage(SiegeMode.instance, "lotr", "SIEGE_ACTIVE", entityplayer.getScoreboardName());
			}
			else
			{
				if (inSiegeRange)
				{
					double putRange = radius + EDGE_PUT_RANGE;
					int newX = xPos + MathHelper.floor(putRange * MathHelper.cos(angle));
					int newZ = zPos + MathHelper.floor(putRange * MathHelper.sin(angle));
//					int newY = world.getTopSolidOrLiquidBlock(newX, newZ);
					int newY = getTopSolidOrLiquidBlock(world, newX, newZ);
					entityplayer.setPositionAndUpdate(newX + 0.5D, newY + 1.5D, newZ + 0.5D);
					
					warnPlayer(entityplayer, "A siege is occurring here - stay out of the area!");
				}
			}
		}
		
		playerData.updateSiegeScoreboard(entityplayer, false);
	}
	
	//Addon
	private static int getTopSolidOrLiquidBlock(World world, int x, int z) {
		for (
			BlockPos pos = new BlockPos(x, world.getMaxHeight(), z);
			pos.getY() > 0;
			pos = pos.down()
		) {
			if (!world.getBlockState(pos).isAir(world, pos)) {
				return pos.getY();
			}
		}
		return 0;
	}
	//Addon end
	
	public void onPlayerDeath(PlayerEntity entityplayer, DamageSource source)
	{
		if (hasPlayer(entityplayer))
		{
			boolean firstBlood = true;
			for (SiegeTeam aTeam : siegeTeams)
			{
				if (aTeam.getTeamKills() > 0)
				{
					firstBlood = false;
					break;
				}
			}
			
			UUID playerID = entityplayer.getUniqueID();
			SiegePlayerData playerData = getPlayerData(playerID);
			SiegeTeam team = getPlayerTeam(entityplayer);
			
			if (!entityplayer.isCreative())
			{
				playerData.onDeath(entityplayer);
				team.addTeamDeath();
				
				// TODO : Vinyarion's Addon start
				AddonHooks.playerDies(entityplayer, this, team);
				// Addon end
				
				PlayerEntity killingPlayer = null;
				Entity killer = source.getTrueSource();
				if (killer instanceof PlayerEntity)
				{
					killingPlayer = (PlayerEntity)killer;
				}
				else
				{
					LivingEntity lastAttacker = entityplayer.getAttackingEntity();
					if (lastAttacker instanceof PlayerEntity)
					{
						killingPlayer = (PlayerEntity)lastAttacker;
					}
				}
				
				// make sure that killer is actually in the siege
				if (killingPlayer != null && (!hasPlayer(killingPlayer) || killingPlayer.isCreative()))
				{
					killingPlayer = null;
				}
				
				UUID killedLastKill = playerData.getLastKill();
				
				playerData.onDeath(killingPlayer);
				team.addTeamDeath();
				
				if (killingPlayer != null)
				{
					SiegePlayerData killingPlayerData = getPlayerData(killingPlayer);
					killingPlayerData.onKill(entityplayer);
					SiegeTeam killingTeam = getPlayerTeam(killingPlayer);
					killingTeam.addTeamKill();
					
					if (firstBlood)
					{
						announceToAllPlayers(killingPlayer.getScoreboardName() + " (" + killingTeam.getTeamName() + ") claimed the first kill!");
					}
					
					int killstreak = killingPlayerData.getKillstreak();
					if (killstreak >= KILLSTREAK_ANNOUNCE)
					{
						announceToAllPlayers(killingPlayer.getScoreboardName() + " (" + killingTeam.getTeamName() + ") has a killstreak of " + killstreak + "!");
					}
					
					if (killedLastKill != null)
					{
						SiegePlayerData lastKillData = getPlayerData(killedLastKill);
						if (lastKillData != null && getPlayerTeam(killedLastKill) == killingTeam)
						{
							announceToAllPlayers(killingPlayer.getScoreboardName() + " (" + killingTeam.color + killingTeam.getTeamName() + TextFormatting.RED + ") has a killstreak of " + killstreak + "!");
							UUID lastKillLastKilledBy = lastKillData.getLastKilledBy();
							if (lastKillLastKilledBy != null && lastKillLastKilledBy.equals(playerID))
							{
								PlayerEntity avengedPlayer = entityplayer.world.getPlayerByUuid(killedLastKill);
								if (avengedPlayer != null)
								{
									announcePlayer(killingPlayer, "You avenged " + avengedPlayer.getScoreboardName() + "'s death!");
									announcePlayer(avengedPlayer, killingPlayer.getScoreboardName() + " avenged your death!");
								}
							}
						}
					}
				}
			}
			
			String nextTeamName = playerData.getNextTeam();
			if (nextTeamName != null)
			{
				SiegeTeam nextTeam = getTeam(nextTeamName);
				if (nextTeam != null && nextTeam != team)
				{
					team.leavePlayer(entityplayer);
					nextTeam.joinPlayer(entityplayer);
					team = getPlayerTeam(entityplayer);
					
					playerData.onTeamChange();
					
					warnAllPlayers(entityplayer.getScoreboardName() + " is now playing on team " + team.getTeamName());
				}
				
				playerData.setNextTeam(null);
			}
			
			// to not drop siege kit
			Kit.clearPlayerInvAndKit(entityplayer);
			
			DimensionType dim = entityplayer.dimension;
			BlockPos coords = entityplayer.getBedLocation(dim);
			boolean forced = entityplayer.isSpawnForced(dim);
			
			BackupSpawnPoint bsp = new BackupSpawnPoint(dim, coords, forced);
			playerData.setBackupSpawnPoint(bsp);
			markDirty();
			
			BlockPos teamSpawn = team.getRespawnPoint();
			entityplayer.setSpawnPoint(teamSpawn, true, false, dim);
		}
	}
	
	public void onPlayerRespawn(PlayerEntity entityplayer)
	{
		if (hasPlayer(entityplayer))
		{
			restoreAndClearBackupSpawnPoint(entityplayer);
			applyPlayerKit(entityplayer);
		}
	}
	
	private void restoreAndClearBackupSpawnPoint(PlayerEntity entityplayer)
	{
		UUID playerID = entityplayer.getUniqueID();
		SiegePlayerData playerData = getPlayerData(playerID);
		
		BackupSpawnPoint bsp = playerData.getBackupSpawnPoint();
		if (bsp != null)
		{
			entityplayer.setSpawnPoint(bsp.spawnCoords, bsp.spawnForced, false, bsp.dimension);
		}
		playerData.setBackupSpawnPoint(null);
	}
	
	public void applyPlayerKit(PlayerEntity entityplayer)
	{
		SiegeTeam team = getPlayerTeam(entityplayer);
		UUID playerID = entityplayer.getUniqueID();
		SiegePlayerData playerData = getPlayerData(playerID);
		
		Kit kit = KitDatabase.getKit(playerData.getChosenKit());
		if (kit == null || !team.containsKit(kit))
		{
			kit = team.getRandomKit(entityplayer.getRNG());
			warnPlayer(entityplayer, "No kit chosen! Using a random kit: " + kit.getKitName());
		}
		
		kit.applyTo(entityplayer);
		playerData.setCurrentKit(kit);
		setHasSiegeGivenKit(entityplayer, true);
		
		if (respawnImmunity > 0)
		{
			entityplayer.addPotionEffect(new EffectInstance(Effects.RESISTANCE, respawnImmunity * 20, 64));
		}
	}
	
	public static boolean hasSiegeGivenKit(PlayerEntity entityplayer)
	{
		return entityplayer.getPersistentData().getBoolean("HasSiegeKit");
	}
	
	public static void setHasSiegeGivenKit(PlayerEntity entityplayer, boolean flag)
	{
		entityplayer.getPersistentData().putBoolean("HasSiegeKit", flag);
	}
	
	public static void dispel(PlayerEntity entityplayer)
	{
		// TODO : Vinyarion's Addon start
		SiegePlayerData spd = AddonHooks.lastLeft.get();
		if(spd != null) {
			DimensionType todim = spd.addonData.joinedSiegeDim;
			Vec3d topos = spd.addonData.joinedSiegePos;
			if(entityplayer.dimension != todim) {
				entityplayer.changeDimension(todim, new AddonTeleporter((ServerWorld)entityplayer.world));
			}
			entityplayer.setPositionAndUpdate(topos.x, topos.y, topos.z);
		} else {
		// Addon end
		BlockPos spawnCoords = entityplayer.world.getSpawnPoint();
		if (spawnCoords != null)
		{
			entityplayer.setPositionAndUpdate(spawnCoords.getX() + 0.5D, spawnCoords.getY() + 0.5D, spawnCoords.getZ() + 0.5D);
		}
		// TODO : Vinyarion's Addon start
		else {
			entityplayer.sendMessage(new StringTextComponent("Sorry, but we couldn't manage to get your last location. Ask a staff member to teleport you to your previous location."));
		}
		}
		entityplayer.getPersistentData().remove("VinyarionAddon_Flag");
		AddonHooks.lastLeft.remove();
		// Addon end
	}
	
	public void onPlayerLogin(ServerPlayerEntity entityplayer)
	{
		SiegePlayerData playerData = getPlayerData(entityplayer);
		if (playerData != null)
		{
			playerData.onLogin(entityplayer);
		}
	}
	
	public void onPlayerLogout(ServerPlayerEntity entityplayer)
	{
		SiegePlayerData playerData = getPlayerData(entityplayer);
		if (playerData != null)
		{
			playerData.onLogout(entityplayer);
		}
	}
	
	public void markDirty()
	{
		needsSave = true;
	}
	
	public void markSaved()
	{
		needsSave = false;
	}
	
	public boolean needsSave()
	{
		return needsSave;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void deleteSiege()
	{
		if (isActive())
		{
			endSiege();
		}
		
		deleted = true;
		markDirty();
	}
	
	public void writeToNBT(CompoundNBT nbt)
	{
		nbt.putString("SiegeID", siegeID.toString());
		nbt.putString("Name", siegeName);
		nbt.putBoolean("Deleted", deleted);
		
		nbt.putBoolean("LocationSet", isLocationSet);
		nbt.putString("Dim", dimension.getRegistryName().toString());
		nbt.putInt("XPos", xPos);
		nbt.putInt("ZPos", zPos);
		nbt.putInt("Radius", radius);
		
		nbt.putInt("TicksRemaining", ticksRemaining);
		
		ListNBT teamTags = new ListNBT();
		for (SiegeTeam team : siegeTeams)
		{
			CompoundNBT teamData = new CompoundNBT();
			team.writeToNBT(teamData);
			teamTags.add(teamData);
		}
		nbt.put("Teams", teamTags);
		
		nbt.putInt("MaxTeamDiff", maxTeamDifference);
		nbt.putBoolean("FriendlyFire", friendlyFire);
		nbt.putBoolean("MobSpawning", mobSpawning);
		nbt.putBoolean("TerrainProtect", terrainProtect);
		nbt.putBoolean("TerrainProtectInactive", terrainProtectInactive);
		nbt.putInt("RespawnImmunity", respawnImmunity);
		nbt.putBoolean("Dispel", dispelOnEnd);
		
		ListNBT playerTags = new ListNBT();
		for (Entry<UUID, SiegePlayerData> e : playerDataMap.entrySet())
		{
			UUID playerID = e.getKey();
			SiegePlayerData player = e.getValue();
			
			CompoundNBT playerData = new CompoundNBT();
			player.writeToNBT(playerData);
			playerData.putString("PlayerID", playerID.toString());
			playerTags.add(playerData);
		}
		nbt.put("PlayerData", playerTags);
		
		// TODO : Vinyarion's addon start
		nbt.putString("VinyarionAddon_Mode", mode.mode().identifier());
		mode.toNBT(this, nbt);
		// Addon end
		System.err.println(nbt);
	}
	
	public void readFromNBT(CompoundNBT nbt)
	{
		System.err.println(nbt);
		siegeID = UUID.fromString(nbt.getString("SiegeID"));
		siegeName = nbt.getString("Name");
		deleted = nbt.getBoolean("Deleted");
		
		isLocationSet = nbt.getBoolean("LocationSet");
		dimension = DimensionType.byName(new ResourceLocation(nbt.getString("Dim")));
		xPos = nbt.getInt("XPos");
		zPos = nbt.getInt("ZPos");
		radius = nbt.getInt("Radius");
		
		ticksRemaining = nbt.getInt("TicksRemaining");
		
		siegeTeams.clear();
		if (nbt.contains("Teams", Constants.NBT.TAG_LIST))
		{
			ListNBT teamTags = nbt.getList("Teams", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < teamTags.size(); i++)
			{
				CompoundNBT teamData = teamTags.getCompound(i);
				SiegeTeam team = new SiegeTeam(this);
				team.readFromNBT(teamData);
				siegeTeams.add(team);
			}
		}
		
		maxTeamDifference = nbt.getInt("MaxTeamDiff");
		friendlyFire = nbt.getBoolean("FriendlyFire");
		mobSpawning = nbt.getBoolean("MobSpawning");
		terrainProtect = nbt.getBoolean("TerrainProtect");
		terrainProtectInactive = nbt.getBoolean("TerrainProtectInactive");
		if (nbt.contains("RespawnImmunity", Constants.NBT.TAG_INT))
		{
			respawnImmunity = nbt.getInt("RespawnImmunity");
		}
		if (nbt.contains("Dispel", Constants.NBT.TAG_BYTE))
		{
			dispelOnEnd = nbt.getBoolean("Dispel");
		}
		
		playerDataMap.clear();
		if (nbt.contains("PlayerData", Constants.NBT.TAG_LIST))
		{
			ListNBT playerTags = nbt.getList("PlayerData", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < playerTags.size(); i++)
			{
				CompoundNBT playerData = playerTags.getCompound(i);
				UUID playerID = UUID.fromString(playerData.getString("PlayerID"));
				if (playerID != null)
				{
					SiegePlayerData player = new SiegePlayerData(this);
					player.readFromNBT(playerData);
					playerDataMap.put(playerID, player);
				}
			}
		}
		
		// TODO : Vinyarion's addon start
		mode = Mode.of(nbt.getString("VinyarionAddon_Mode")).setSiege(this);
		mode.fromNBT(this, nbt);
		// Addon end
	}
	
	// TODO : Vinyarion's addon start
	public List<SiegeTeam> teams() {
		return siegeTeams;
	}
	public Mode mode = new ModeDefault().setSiege(this);
	public World world() {
		return SiegeModeMain.instance.getServer().getWorld(dimension);
	}
	public MinecraftServer getServer() {
		return this.world().getServer();
	}
	// Addon end
	
}
