package siege.common.kit;

import java.io.File;
import java.util.*;

import net.minecraft.nbt.CompoundNBT;

import org.apache.commons.lang3.StringUtils;

import siege.common.SiegeModeMain;

public class KitDatabase
{
	//Addon: Increased visibility
	static Map<UUID, Kit> kitMap = new HashMap<>();
	static Map<String, UUID> kitNameMap = new HashMap<>();
	
	private static final String randomKitID = "random";
	
	public static Kit getKit(UUID id)
	{
		return kitMap.get(id);
	}
	
	public static Kit getKit(String name)
	{
		return kitMap.get(getKitNameID(name));
	}
	
	public static UUID getKitNameID(String name)
	{
		return kitNameMap.get(name);
	}
	
	public static boolean kitExists(String name)
	{
		return getKit(name) != null;
	}
	
	public static List<String> getAllKitNames()
	{
		return new ArrayList<>(kitNameMap.keySet());
	}
	
	public static boolean validKitName(String name)
	{
		return StringUtils.isAlphanumeric(name.replaceAll("_", "")) && !isRandomKitID(name);
	}
	
	public static boolean isRandomKitID(String name)
	{
		return name.equalsIgnoreCase(randomKitID);
	}
	
	public static String getRandomKitID()
	{
		return randomKitID;
	}
	
	public static void addAndSaveKit(Kit kit)
	{
		kitMap.put(kit.getKitID(), kit);
		putKitNameAndID(kit);
		saveKitToFile(kit);
	}
	
	private static void putKitNameAndID(Kit kit)
	{
		kitNameMap.put(kit.getKitName(), kit.getKitID());
	}
	
	public static void renameKit(Kit kit, String oldName)
	{
		kitNameMap.remove(oldName);
		putKitNameAndID(kit);
	}
	
	public static void deleteKit(Kit kit)
	{
if (kit.isSelfSupplied()) return; // Addon
		kit.deleteKit();
		saveKitToFile(kit);
		kitMap.remove(kit.getKitID());
		kitNameMap.remove(kit.getKitName());
	}
	
	private static File getOrCreateKitDirectory()
	{
		File dir = new File(SiegeModeMain.instance.getSiegeRootDirectory(), "kits");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	private static File getKitFile(Kit kit)
	{
		File kitDir = getOrCreateKitDirectory();
		return new File(kitDir, kit.getKitID().toString() + ".dat");
	}
	
	public static boolean anyNeedSave()
	{
		for (Kit kit : kitMap.values())
		{
			if (kit.needsSave())
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
			for (Kit kit : kitMap.values())
			{
				if (kit.needsSave())
				{
					saveKitToFile(kit);
					kit.markSaved();
					i++;
				}
			}
			SiegeModeMain.MODLOG.info(String.format("SiegeMode: Saved %s kits", i));
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error("Error saving kits data");
			e.printStackTrace();
		}
	}
	
	public static void reloadAll()
	{
		kitMap.clear();
		kitNameMap.clear();
		try
		{
			File kitDir = getOrCreateKitDirectory();
			File[] kitFiles = kitDir.listFiles();
			int i = 0;
			for (File dat : kitFiles)
			{
				Kit kit = loadKitFromFile(dat);
				if (kit != null)
				{
					kitMap.put(kit.getKitID(), kit);
					putKitNameAndID(kit);
					i++;
				}
			}
			SiegeModeMain.MODLOG.info(String.format("SiegeMode: Loaded %s kits", i));
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error("Error loading kit data");
			e.printStackTrace();
		}
		Kit.setupSelfSupplied();
	}
	
	private static Kit loadKitFromFile(File kitFile)
	{
		try
		{
			CompoundNBT nbt = SiegeModeMain.instance.loadNBTFromFile(kitFile);
			Kit kit = new Kit();
			kit.readFromNBT(nbt);
			if (!kit.isDeleted())
			{
				return kit;
			}
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error(String.format("SiegeMode: Error loading kit %s", kitFile.getName()));
			e.printStackTrace();
		}
		return null;
	}

	public static void saveKitToFile(Kit kit)
	{
		try
		{
			CompoundNBT nbt = new CompoundNBT();
			kit.writeToNBT(nbt);
			SiegeModeMain.instance.saveNBTToFile(getKitFile(kit), nbt);
		}
		catch (Exception e)
		{
			SiegeModeMain.MODLOG.error(String.format("SiegeMode: Error saving kit %s", kit.getKitName()));
			e.printStackTrace();
		}
	}
}
