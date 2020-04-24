package sybyline.anduril.extensions;

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

public final class SubmodClient {

	private SubmodClient() {}

	public static final SubmodClient INSTANCE = new SubmodClient();

	private Minecraft mc;

	public void init(FMLClientSetupEvent event) {
		mc = event.getMinecraftSupplier().get();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void drawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
		Screen screen = event.getGui();
		if (screen instanceof MainMenuScreen) {
			List<String> strings = getMainMenuStrings();
			float x = 2, y = 2;
			for (int i = 0; i < strings.size(); i++) {
				mc.fontRenderer.drawString(strings.get(i), x, y + (i * 10), -1);
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
