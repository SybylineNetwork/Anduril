package sybyline.anduril.extensions.client;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import sybyline.anduril.Anduril;
import sybyline.anduril.extensions.Submod;
import sybyline.anduril.util.client.ScreenUtils;

public final class SubmodClient {

	private SubmodClient() {}

	public static final SubmodClient INSTANCE = new SubmodClient();

	private Minecraft mc;

	public void init(FMLClientSetupEvent event) {
		mc = event.getMinecraftSupplier().get();
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static final int ROW = 10;

	@SubscribeEvent
	public void drawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
		Screen screen = event.getGui();
		if (screen instanceof MainMenuScreen) {
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			List<String> strings = getMainMenuStrings();
			float x = 2, y = 2;
			for (int i = 0; i < strings.size(); i++) {
				String s = strings.get(i);
				int l = mc.fontRenderer.getStringWidth(s);
				mc.fontRenderer.drawString(s, x, y + (i * ROW), -1);
				if (i == 0 && mouseX >= x && mouseX <= x + ROW && mouseY >= y && mouseY <= y + l) {
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
			List<String> strings = getMainMenuStrings();
			float x = 2, y = 2;
			if (strings.size() > 0) {
				String s = strings.get(0);
				int l = mc.fontRenderer.getStringWidth(s);
				if (mouseX >= x && mouseX <= x + ROW && mouseY >= y && mouseY <= y + l) {
					event.setCanceled(true);
					mc.displayGuiScreen(new SubmodListScreen());
				}
			}
		}
	}

	private List<String> getMainMenuStrings() {
		return Lists.newArrayList(
			(Anduril.NAME + " " + Anduril.VERSION),
			(Submod.submods().size() + " submods loaded")
		);
	}

}
