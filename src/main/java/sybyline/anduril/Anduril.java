package sybyline.anduril;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import sybyline.anduril.extensions.Submod;
import sybyline.anduril.scripting.common.CommonScripting;

import org.apache.logging.log4j.*;
import org.apache.maven.artifact.versioning.*;

@Mod(Anduril.MODID)
public class Anduril {

	public static final String MODID = "anduril";
	public static final String NAME = "Andúril";
	public static final ArtifactVersion VERSION = new DefaultArtifactVersion("0.0.0-unknown");
    public static final Logger LOGGER = LogManager.getLogger();

    private static Anduril instance;

    public static Anduril instance() {
    	return instance;
    }

    public Anduril() {
    	instance = this;
        IEventBus mods = FMLJavaModLoadingContext.get().getModEventBus();
	        mods.addListener(this::setup);
	        mods.addListener(this::gameStart);
	        mods.addListener(this::enqueueIMC);
	        mods.addListener(this::processIMC);
        IEventBus forge = MinecraftForge.EVENT_BUS;
        	forge.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        Submod.loadSubmods();
        ModList.get().getModContainerById(MODID).map(ModContainer::getModInfo).map(IModInfo::getVersion).map(ArtifactVersion::toString).ifPresent(VERSION::parseVersion);
    }

    private void gameStart(FMLClientSetupEvent event) {
    	CommonScripting.INSTANCE.gameStart(event);
    	Submod.gameStart(event);
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
    	CommonScripting.INSTANCE.serverStart(event);
    }

    @SubscribeEvent
    public void serverStartPost(FMLServerStartedEvent event) {
    	event.getServer().reload();
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
    	CommonScripting.INSTANCE.serverTick(event);
    }

    @SubscribeEvent
    public void serverClose(FMLServerStoppingEvent event) {
    	CommonScripting.INSTANCE.serverStop(event);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
    }

    private void processIMC(InterModProcessEvent event) {
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        }
    }

}
