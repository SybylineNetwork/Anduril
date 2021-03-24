package sybyline.anduril.scripting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.common.ScriptUtil;
import sybyline.anduril.scripting.common.ScriptWrapperSimple;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.SimpleReloadListener;

@SuppressWarnings("rawtypes")
public final class ClientScripting {

	private ClientScripting() {}

	public static final ClientScripting INSTANCE = new ClientScripting();

	public void setup(FMLClientSetupEvent event) {
		mc = event.getMinecraftSupplier().get();
		mc_resources = (IReloadableResourceManager)mc.getResourceManager();
		mc_resources.addReloadListener(SimpleReloadListener.prepare(this::clientReloadGui).call());
		mc_resources.addReloadListener(SimpleReloadListener.prepare(this::clientReloadTooltips).call());
		MinecraftForge.EVENT_BUS.register(this);
		CommonScripting.INSTANCE.setPrintln_debug(string -> {
			if (mc.gameSettings.showDebugInfo) {
				mc.ingameGUI.addChatMessage(ChatType.SYSTEM, new StringTextComponent("["+Util.ANDURIL+"]:"+string));
			}
		});
		ScriptUtil.INSTANCE.i18n = I18n::format;
	}

	public Minecraft mc;
	public IReloadableResourceManager mc_resources;

	// Custom overlay

	public static final ResourceLocation OVERLAY = new ResourceLocation(Util.ANDURIL, "special/overlay");
	public static final ResourceLocation TOOLTIPS = new ResourceLocation(Util.ANDURIL, "special/tooltips");

	public ScriptGuiWrapper<?> game_overlay = null;
	public ScriptWrapperSimple tooltips = null;

	private int prevWidth = -1, prevHeight = -1;

	@SubscribeEvent
	public void clientRenderOverlay(RenderGameOverlayEvent event) {
		if (mc.player != null && game_overlay != null) {
			float partialTicks = event.getPartialTicks();
			int width = mc.getMainWindow().getScaledWidth();
			int height = mc.getMainWindow().getScaledHeight();
			if (prevWidth != width || prevHeight != height) {
				game_overlay.init(mc, width, height);
				prevWidth = width;
				prevHeight = height;
			}
			game_overlay.render(width / 2, height / 2, partialTicks);
		}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (mc.player != null && game_overlay != null) {
				game_overlay.tick();
			}
		}
	}

	private void clientReloadGui(IResourceManager __) {
		String js = js(OVERLAY);
		if (js != null) {
			game_overlay = new ScriptGuiWrapper(OVERLAY, js, null) {
				@Override
				public void onClose() {
					game_overlay = null;
					CommonScripting.LOGGER.info("Error in overlay " + OVERLAY);
				}
			};
			int width = mc.getMainWindow().getScaledWidth();
			int height = mc.getMainWindow().getScaledHeight();
			game_overlay.init(mc, width, height);
		} else {
			CommonScripting.LOGGER.info("No overlay " + OVERLAY);
			game_overlay = null;
		}
	}

	private void clientReloadTooltips(IResourceManager __) {
		// TODO : tooltips
		String js = js(TOOLTIPS);
		if (js != null) {
			tooltips = new ScriptWrapperSimple("tooltips", js);
			tooltips.setupWithContext(null);
		} else {
			CommonScripting.LOGGER.info("No tooltips " + TOOLTIPS);
			tooltips = null;
		}
	}

	public void serverSendsGui(ResourceLocation loc, boolean redisplay, CompoundNBT nbt) {
		try {
			if (mc.currentScreen != null && mc.currentScreen instanceof ScriptGuiWrapper) {
				ScriptGuiWrapper<?> sgs = (ScriptGuiWrapper<?>)mc.currentScreen;
				if (sgs.resource_id.equals(loc)) {
					if (redisplay) {
						sgs.gui.gui_close();
					} else {
						sgs.serverData(nbt, true);
						return;
					}
				}
			}
			this.tryOpen(loc, nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ScriptGuiWrapper<?> tryOpen(ResourceLocation loc, Object data) {
		String js = js(loc);
		if (js == null) {
			CommonScripting.LOGGER.error("Failed to load script resource " + loc);
		} else {
			ScriptGuiWrapper<?> wrapper = new ScriptGuiWrapper(loc, js, data);
			mc.displayGuiScreen(wrapper);
			return wrapper;
		}
		return null;
	}

	private String js(ResourceLocation loc) {
		if (loc != null) try {
			IResource res = mc_resources.getResource(Util.Structs.sandwich(loc, Util.ANDURIL+"/", ".js"));
			return Util.IO.readString(res.getInputStream());
		} catch(Exception e) {
		}
		return null;
	}

	public void onDynamic(Context ctx, ResourceLocation id, CompoundNBT data) {
		
	};

}
