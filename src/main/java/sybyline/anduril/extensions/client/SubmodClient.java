package sybyline.anduril.extensions.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import sybyline.anduril.Anduril;
import sybyline.anduril.coremods.DualWield;
import sybyline.anduril.extensions.Submod;
import sybyline.anduril.util.client.ScreenUtils;

public final class SubmodClient {

	private SubmodClient() {}

	public static final SubmodClient INSTANCE = new SubmodClient();

	private Minecraft mc;
	public ItemDisplay item_display;
	public KeyBinding toggle_attack_mode;

	public void init(FMLClientSetupEvent event) {
		mc = event.getMinecraftSupplier().get();
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(DualWield.Client.INSTANCE);
		item_display = new ItemDisplay();
		ClientRegistry.registerKeyBinding(toggle_attack_mode = new ToggleableKeyBinding("key.toggle_attack_mode", GLFW.GLFW_KEY_R, Anduril.NAME, () -> true));
	}

	private static final int ROW = 10;
	@SubscribeEvent
	public void drawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
		Screen screen = event.getGui();
		if (screen instanceof MainMenuScreen) {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			String[] strings = getMainMenuStrings();
			float x = 2, y = 2;
			for (int i = 0; i < strings.length; i++) {
				String s = strings[i];
				int l = mc.fontRenderer.getStringWidth(s);
				mc.fontRenderer.drawString(s, x, y + (i * ROW), -1);
				if (i == 0 && mouseX >= x && mouseX <= x + l && mouseY >= y && mouseY <= y + ROW) {
					ScreenUtils.box((int)x - 1, (int)y - 1, l + 1, ROW + 1, -1);
				}
			}
		}
	}

	@SubscribeEvent
	public void clickGui(GuiScreenEvent.MouseClickedEvent.Post event) {
		Screen screen = event.getGui();
		if (screen instanceof MainMenuScreen) {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			String[] strings = getMainMenuStrings();
			float x = 2, y = 2;
			if (strings.length > 0) {
				String s = strings[0];
				int l = mc.fontRenderer.getStringWidth(s);
				if (mouseX >= x && mouseX <= x + l && mouseY >= y && mouseY <= y + ROW) {
					event.setCanceled(true);
//					mc.displayGuiScreen(new SubmodListScreen());
				}
			}
		}
	}

	private String[] getMainMenuStrings() {
		return new String[] {
			(Anduril.NAME + " " + Anduril.VERSION),
			(Submod.submods().size() + " submods loaded"),
		};
	}

}
