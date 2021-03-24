package sybyline.anduril.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DynamicRegistry;
import sybyline.anduril.boot.Boot;
import sybyline.anduril.common.advancements.AndurilStats;
import sybyline.anduril.common.effect.PotionThings;
import sybyline.anduril.common.inventory.BetterInvHelp;
import sybyline.anduril.common.item.HandItem;
import sybyline.anduril.common.item.recipe.Recipes;
import sybyline.anduril.common.network.C2SAttackEntity;
import sybyline.anduril.common.network.C2SAttackVector;
import sybyline.anduril.common.network.C2SCastSpell;
import sybyline.anduril.common.network.C2SPostEditScript;
import sybyline.anduril.common.network.C2SPostIdentifiedAction;
import sybyline.anduril.common.network.C2SPostIdentifiedData;
import sybyline.anduril.common.network.PacketSpecDynamic;
import sybyline.anduril.common.network.S2CDisplayGui;
import sybyline.anduril.common.network.S2CEntityBreakItem;
import sybyline.anduril.common.network.S2CQueueAction;
import sybyline.anduril.common.network.S2CQueueEditScript;
import sybyline.anduril.common.network.S2CSyncEntityData;
import sybyline.anduril.common.network.S2CSyncEntitySpawnData;
import sybyline.anduril.common.network.S2CSyncExtraRewardsData;
import sybyline.anduril.common.network.S2CSyncGameData;
import sybyline.anduril.common.network.S2CSyncInv;
import sybyline.anduril.common.network.S2CSyncPlayerData;
import sybyline.anduril.common.network.SybylineNetwork;
import sybyline.anduril.common.skill.Experience;
import sybyline.anduril.common.skill.Skill;
import sybyline.anduril.common.world.gen.AndurilOres;
import static sybyline.anduril.util.TextComponents.*;
import sybyline.anduril.coremods.EntityExtra;
import sybyline.anduril.coremods.ExtraRewards;
import sybyline.anduril.extensions.Submod;
import sybyline.anduril.scripting.api.ScriptType;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.server.ServerManagement;
import sybyline.anduril.scripting.server.ServerScripting;

@Mod(Anduril.MODID)
public final class Anduril {

	static {
		Boot.register();
	}

	public static final String MODID = "anduril";
	public static final String NAME = "Andúril";
	public static final ArtifactVersion VERSION = new DefaultArtifactVersion("0.0.0-unknown");
    public static final Logger LOGGER = LogManager.getLogger();

    private static Anduril instance;

    public static Anduril instance() {
    	return instance;
    }

	public SybylineNetwork network;
	public final siege.common.SiegeModeMain siegeMode = new siege.common.SiegeModeMain();
	public final ProceduralContent procedural_content = new ProceduralContent();
	public final Compatibility compatibility = new Compatibility();

    public Anduril() {
    	synchronized(Object.class) { // make sure custom classloaders can't accidentally
    		if (instance != null)
    			throw new IllegalArgumentException("Can't create multiple mod instances!");
    		instance = this;
    	}
        IEventBus mods = FMLJavaModLoadingContext.get().getModEventBus();
	        mods.addListener(this::setup);
	        mods.addListener(this::gameStart);
	        mods.addListener(this::enqueueIMC);
	        mods.addListener(this::processIMC);
        IEventBus forge = MinecraftForge.EVENT_BUS;
        	forge.register(this);
    	Registries.register(mods, forge);
    }

    public static boolean isDataRemote() {
    	return ServerLifecycleHooks.getCurrentServer() == null;
    }

	public static boolean isClientObject(Object object) {
		if (object == null)
			return isDataRemote();
		if (object instanceof LogicalSide)
			return ((LogicalSide)object).isClient();
		if (object instanceof World)
			return ((World)object).isRemote;
		if (object instanceof Entity)
			return ((Entity)object).world.isRemote;
		if (object instanceof Dist)
			return ((Dist)object).isClient();
		return isClientObject(null);
	}

    private void setup(FMLCommonSetupEvent event) {
    	ModList.get().getModContainerById(MODID).map(ModContainer::getModInfo).map(IModInfo::getVersion).map(ArtifactVersion::toString).ifPresent(VERSION::parseVersion);
    	network = new SybylineNetwork(new ResourceLocation(MODID, "main"), MODID, network -> {
    		network.register(PacketSpecDynamic.class, PacketSpecDynamic::new);
    		// C2S
    		network.register(C2SAttackEntity.class, C2SAttackEntity::new);
    		network.register(C2SAttackVector.class, C2SAttackVector::new);
    		network.register(C2SCastSpell.class, C2SCastSpell::new);
    		network.register(C2SPostEditScript.class, C2SPostEditScript::new);
    		network.register(C2SPostIdentifiedAction.class, C2SPostIdentifiedAction::new);
    		network.register(C2SPostIdentifiedData.class, C2SPostIdentifiedData::new);
    		// S2C
    		network.register(S2CEntityBreakItem.class, S2CEntityBreakItem::new);
    		network.register(S2CDisplayGui.class, S2CDisplayGui::new);
    		network.register(S2CQueueAction.class, S2CQueueAction::new);
    		network.register(S2CQueueEditScript.class, S2CQueueEditScript::new);
    		network.register(S2CSyncEntityData.class, S2CSyncEntityData::new);
    		network.register(S2CSyncEntitySpawnData.class, S2CSyncEntitySpawnData::new);
    		network.register(S2CSyncExtraRewardsData.class, S2CSyncExtraRewardsData::new);
    		network.register(S2CSyncGameData.class, S2CSyncGameData::new);
    		network.register(S2CSyncInv.class, S2CSyncInv::new);
    		network.register(S2CSyncPlayerData.class, S2CSyncPlayerData::new);
    	});
    	Boot.boot_finalize();
    	CommonScripting.INSTANCE.commonStart(event);
        Submod.loadSubmods();
    	Recipes.init();
        HandItem.init();
        AndurilStats.init();
        AndurilDatas.init();
        EntityExtra.init();
        ExtraRewards.init();
        Skill.init();
        Experience.init();
        PotionThings.init();
        AndurilOres.init();
        BetterInvHelp.init();
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
    	Submod.interModCommsEnqueue();
    }

    private void processIMC(InterModProcessEvent event) {
    	event.getIMCStream().forEach(msg -> compatibility.consume(msg.getMethod(), msg.getSenderModId(), msg.getMessageSupplier()));
    }

	private void gameStart(FMLClientSetupEvent event) {
    	CommonScripting.INSTANCE.gameStart(event);
    	Submod.gameStart(event);
    }

    @SubscribeEvent
    public void serverStartPre(FMLServerAboutToStartEvent event) {
    	ScriptType.init(event.getServer());
    }
   
    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
    	if (!event.getWorld().isRemote() && event.getWorld().getDimension().getType() == DimensionType.OVERWORLD)
    		procedural_content.setCurrentSeed(event.getWorld().getSeed());
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
    	ServerManagement.INSTANCE.serverStart(event);
    	CommonScripting.INSTANCE.serverStart(event);
    	procedural_content.serverStart(event);
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
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!event.getPlayer().world.isRemote)
			runLoginReloadListeners(event.getPlayer().getServer().getPlayerList(), (ServerPlayerEntity)event.getPlayer());
    }

    // sybyline.anduril.boot.Boot
	public void serverReloadPost(PlayerList playerlist) {
		runLoginReloadListeners(playerlist, playerlist.getPlayers());
		playerlist.sendMessage($translate("refresh.your.resources", $noArgs, $underlined, $green), false);
		ServerScripting.INSTANCE.resolveTasks();
	}
    private final List<BiConsumer<PlayerList, Collection<ServerPlayerEntity>>> loginReloadListeners = new ArrayList<>();
    public void addLoginReloadListener(BiConsumer<PlayerList, Collection<ServerPlayerEntity>> listener) {
    	synchronized (loginReloadListeners) {
    		loginReloadListeners.add(listener);
    	}
    }
    public void runLoginReloadListeners(PlayerList server, Collection<ServerPlayerEntity> players) {
    	synchronized (loginReloadListeners) {
    		loginReloadListeners.forEach(listener -> listener.accept(server, players));
    	}
    }
    public void runLoginReloadListeners(PlayerList server, ServerPlayerEntity player) {
    	runLoginReloadListeners(server, Collections.singletonList(player));
    }

    @SubscribeEvent
    public void serverClose(FMLServerStoppingEvent event) {
    	CommonScripting.INSTANCE.serverStop(event);
    	ServerManagement.INSTANCE.serverStop(event);
    	procedural_content.serverStop(event);
    }

    @SubscribeEvent
    public void serverClosePost(FMLServerStoppedEvent event) {
    	procedural_content.setCurrentSeed(0L);
    	DynamicRegistry.getDynamicRegistries().forEach(DynamicRegistry::resetExtension);
    }

}
