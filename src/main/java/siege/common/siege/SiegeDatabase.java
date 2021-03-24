package siege.common.siege;

import java.io.File;
import java.util.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableList;
import siege.common.SiegeModeMain;

public class SiegeDatabase
{
	private static Map<UUID, Siege> siegeMap = new HashMap<>();
	private static Map<String, UUID> siegeNameMap = new HashMap<>();

	public static Siege getSiege(String name)
	{
		return siegeMap.get(getSiegeNameID(name));
	}
	
	public static UUID getSiegeNameID(String name)
	{
		return siegeNameMap.get(name);
	}
	
	public static boolean siegeExists(String name)
	{
		return getSiege(name) != null;
	}
	
	public static List<String> getAllSiegeNames()
	{
		return new ArrayList<>(siegeNameMap.keySet());
	}
	
	public static List<String> listActiveSiegeNames()
	{
		List<String> names = new ArrayList<>();
		for (Siege siege : siegeMap.values())
		{
			if (siege.isActive())
			{
				names.add(siege.getSiegeName());
			}
		}
		return names;
	}
	
	public static List<String> listInactiveSiegeNames()
	{
		List<String> names = new ArrayList<>();
		for (Siege siege : siegeMap.values())
		{
			if (!siege.isActive())
			{
				names.add(siege.getSiegeName());
			}
		}
		return names;
	}
	
	public static boolean validSiegeName(String name)
	{
		return StringUtils.isAlphanumeric(name.replaceAll("_", ""));
	}
	
	public static boolean validTeamName(String name)
	{
		return StringUtils.isAlphanumeric(name.replaceAll("_", ""));
	}
	
	public static void addAndSaveSiege(Siege siege)
	{
		siegeMap.put(siege.getSiegeID(), siege);
		putSiegeNameAndID(siege);
		saveSiegeToFile(siege);
	}
	
	private static void putSiegeNameAndID(Siege siege)
	{
		siegeNameMap.put(siege.getSiegeName(), siege.getSiegeID());
	}
	
	public static void renameSiege(Siege siege, String oldName)
	{
		siegeNameMap.remove(oldName);
		putSiegeNameAndID(siege);
	}
	
	public static void deleteSiege(Siege siege)
	{
		siege.deleteSiege();
		saveSiegeToFile(siege);
		siegeMap.remove(siege.getSiegeID());
		siegeNameMap.remove(siege.getSiegeName());
	}
	
	public static void updateActiveSieges(World world)
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.isActive() && siege.isSiegeWorld(world))
			{
				siege.updateSiege(world);
			}
		}
	}
	
	// TODO : Vinyarion's Addon start
	public static List<Siege> getSiegesForPlayer(PlayerEntity player) {
		List<Siege> sieges = new ArrayList<>();
		for (Siege siege : siegeMap.values()) {
			for (SiegeTeam team : siege.teams()) {
				if (team.shadowTeamPlayers.contains(player.getUniqueID())) {
					sieges.add(siege);
				}
			}
		}
		return sieges;
	}
	public static List<SiegePlayerData> removeStale(PlayerEntity player) {
		List<SiegePlayerData> datas = new ArrayList<>();
		for (Siege siege : siegeMap.values()) {
			for (SiegeTeam team : siege.teams()) {
				if (team.shadowTeamPlayers.contains(player.getUniqueID())) {
					team.shadowTeamPlayers.remove(player.getUniqueID());
				}
			}
		}
		return datas;
	}
	public static Collection<Siege> getAllSieges() {
		return ImmutableList.copyOf(siegeMap.values());
	}
	// Addon end
	
	public static Siege getActiveSiegeForPlayer(PlayerEntity entityplayer)
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.isActive() && siege.hasPlayer(entityplayer))
			{
				return siege;
			}
		}
		return null;
	}
	
	public static List<Siege> getActiveSiegesAtPosition(Vec3d vec)
	{
		List<Siege> siegesHere = new ArrayList<>();
		for (Siege siege : siegeMap.values())
		{
			if (siege.isActive() && siege.isLocationInSiege(vec))
			{
				siegesHere.add(siege);
			}
		}
		return siegesHere;
	}
	
	public static List<Siege> getInactiveSiegesAtPosition(Vec3d vec)
	{
		List<Siege> siegesHere = new ArrayList<>();
		for (Siege siege : siegeMap.values())
		{
			if (!siege.isActive() && !siege.isDeleted() && siege.isLocationInSiege(vec))
			{
				siegesHere.add(siege);
			}
		}
		return siegesHere;
	}
	
	private static File getOrCreateSiegeDirectory()
	{
		File dir = new File(SiegeModeMain.instance.getSiegeRootDirectory(), "sieges");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	private static File getSiegeFile(Siege siege)
	{
		File siegeDir = getOrCreateSiegeDirectory();
		return new File(siegeDir, siege.getSiegeID().toString() + ".dat");
	}
	
	public static boolean anyNeedSave()
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.needsSave())
			{
				return true;
			}
		}
		return false;
	}
	
	public static void save()
	{
		try
		{
			int i = 0;
			for (Siege siege : siegeMap.values())
			{
				if (siege.needsSave())
				{
					saveSiegeToFile(siege);
					siege.markSaved();
					i++;
				}
			}
			SiegeModeMain.MODLOG.info(String.format("SiegeMode: Saved %s sieges", i));
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error("Error saving siege data");
			e.printStackTrace();
		}
	}
	
	public static void reloadAll()
	{
		siegeMap.clear();
		siegeNameMap.clear();
		try
		{
			File siegeDir = getOrCreateSiegeDirectory();
			File[] siegeFiles = siegeDir.listFiles();
			int i = 0;
			for (File dat : siegeFiles)
			{
				Siege siege = loadSiegeFromFile(dat);
				if (siege != null)
				{
					siegeMap.put(siege.getSiegeID(), siege);
					putSiegeNameAndID(siege);
					i++;
				}
			}
			SiegeModeMain.MODLOG.info(String.format("SiegeMode: Loaded %s sieges", i));
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error("Error loading siege data");
			e.printStackTrace();
		}
	}
	
	private static Siege loadSiegeFromFile(File siegeFile)
	{
		try
		{
			CompoundNBT nbt = SiegeModeMain.instance.loadNBTFromFile(siegeFile);
			Siege siege = new Siege("");
			siege.readFromNBT(nbt);
			if (!siege.isDeleted())
			{
				return siege;
			}
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error(String.format("SiegeMode: Error loading siege %s", siegeFile.getName()));
			e.printStackTrace();
		}
		return null;
	}

	public static void saveSiegeToFile(Siege siege)
	{
		try
		{
			CompoundNBT nbt = new CompoundNBT();
			siege.writeToNBT(nbt);
			SiegeModeMain.instance.saveNBTToFile(getSiegeFile(siege), nbt);
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error(String.format("SiegeMode: Error saving siege %s", siege.getSiegeName()));
			e.printStackTrace();
		}
	}
}
