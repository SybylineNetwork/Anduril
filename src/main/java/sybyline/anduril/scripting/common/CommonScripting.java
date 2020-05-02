package sybyline.anduril.scripting.common;

import java.util.UUID;
import java.util.function.Consumer;
import org.apache.logging.log4j.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.*;
import sybyline.anduril.scripting.api.common.*;
import sybyline.anduril.scripting.client.ClientScripting;
import sybyline.anduril.scripting.data.ScriptPlayerData;
import sybyline.anduril.scripting.data.ScriptServerData;
import sybyline.anduril.scripting.server.ServerScripting;
import sybyline.anduril.scripting.server.cmd.AndurilCommands;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.*;

public final class CommonScripting {

	private CommonScripting() {}

	public static final Logger LOGGER = LogManager.getLogger();
	public static final CommonScripting INSTANCE = new CommonScripting();

	static {
		LOGGER.info("Sybyline Custom Scripting has been loaded!");
	}

	public final ICache<UUID, CompoundNBT, ScriptPlayerData> player_data = ICache.files
		(new ResourceLocation(Util.SYBYLINE, "player_data"), IFormat.NBT, ScriptPlayerData::new, UUID::toString).setVerbosity(true);

	public final ICache<String, CompoundNBT, ScriptServerData> server_data = ICache.files
		(new ResourceLocation(Util.SYBYLINE, "server_data"), IFormat.NBT, ScriptServerData::new, String::valueOf).setVerbosity(true);

	private Consumer<String> println_debug = LOGGER::debug;

	public void println_debug(String string) {
		println_debug.accept(string);
	}

	public void setPrintln_debug(Consumer<String> stringconsumer) {
		this.println_debug = stringconsumer;
	}

	private boolean clientAddons = false;
	private boolean serverAddons = false;

	public boolean areClientAddonsEnabled() {
		return clientAddons;
	}

	public boolean areServerAddonsEnabled() {
		return serverAddons;
	}

	public void setExtensionsEnabled(boolean clientAddons) {
		this.clientAddons = clientAddons;
	}

	public void setExtensionsEnabledServer(boolean serverAddons) {
		this.serverAddons = serverAddons;
	}

	public void commonStart(FMLCommonSetupEvent event) {
		
	}

	public void gameStart(FMLClientSetupEvent event) {
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()->{
			ClientScripting.INSTANCE.setup(event);
		});
	}

	public void serverStart(FMLServerStartingEvent event) {
		player_data.setServer(event.getServer());
		AndurilCommands.setup(event);
		ServerScripting.INSTANCE.setup(event);
	}

	private static final int cleanupticks = 20 * 30;
	private int ticks = 0;

	public void serverTick(TickEvent.ServerTickEvent event) {
		if ((ticks = ((++ticks) % cleanupticks)) == 0) {
			player_data.periodicCleanup();
		}
	}

	public void serverStop(FMLServerStoppingEvent event) {
		player_data.setServer(null);
	}

	public ScriptPlayerData getScriptDataFor(UUID playeruuid) {
		if (playeruuid == null) {
			return null;
		}
		return this.player_data.getOrCreate(playeruuid);
	}

	public IScriptPlayer getScriptPlayerFor(UUID playeruuid, String domain) {
		if (playeruuid == null) {
			return null;
		}
		return this.getScriptDataFor(playeruuid).scriptdata(domain);
	}

	public IScriptPlayer getScriptPlayerFor(PlayerEntity player, String domain) {
		if (player == null) {
			return null;
		}
		if (player instanceof ServerPlayerEntity) {
			return this.getScriptDataFor(PlayerEntity.getUUID(player.getGameProfile())).scriptdata(domain);
		}
		return null;
	}

	public IScriptLiving getScriptLivingFor(LivingEntity living, String domain) {
		if (living instanceof PlayerEntity) {
			return getScriptPlayerFor((PlayerEntity)living, domain);
		}
		return null;
	}

	public IScriptEntity getScriptEntityFor(Entity entity, String domain) {
		if (entity instanceof LivingEntity) {
			return getScriptLivingFor((LivingEntity)entity, domain);
		}
		return null;
	}

	public ScriptServerData getScriptServerFor(String domain) {
		return server_data.getOrCreate(domain);
	}

}
