package siege.common;

import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import siege.common.siege.command.SiegeCommands;

public class SiegeModeMain
{
	
	public static final String MODID = "siegemode";
	public static final Logger MODLOG = LogManager.getLogger(MODID);
	public static SiegeModeMain instance;
	
	public SiegeModeMain() {
		instance = this;
		eventHandler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLJavaModLoadingContext.get().getModEventBus().register(eventHandler);
		SiegeCommands.registerArgumentTypes();
	}
	
	private EventHandler eventHandler;
	MinecraftServer server;

	public MinecraftServer getServer() {
		return server;
	}
	
	public File getSiegeRootDirectory()
	{
		if (getServer() == null)
			return new File(".");
		return getServer().getActiveAnvilConverter().getFile(getServer().getFolderName(), MODID);
	}
	
	public CompoundNBT loadNBTFromFile(File file) throws FileNotFoundException, IOException
	{
		return file.exists()
			? CompressedStreamTools.readCompressed(new FileInputStream(file))
			: new CompoundNBT();
	}
	
	public void saveNBTToFile(File file, CompoundNBT nbt) throws FileNotFoundException, IOException
	{
		CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
	}
}
