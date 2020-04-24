package sybyline.anduril.scripting.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import sybyline.anduril.scripting.api.client.*;

public class ScriptButton extends ExtendedButton implements IScriptButton {

	public ScriptButton(int x, int y, int w, int h, int z, String msg, Runnable clickAction, Runnable hoverAction) {
		super(x, y, w, h, msg, button -> clickAction.run());
		this.z = z;
		this.hover = hoverAction;
	}

	final int z;
	final Runnable hover;
	IScriptGui<?> _internal_screen = null;
	IScriptWidgetTexture texture = null;

	public IScriptButton withTexture(IScriptWidgetTexture texture) {
		this.texture = texture;
		return this;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.translatef(0, 0, z);
		if (texture == null) {
			super.renderButton(mouseX, mouseY, partialTicks);
		} else {
			texture.draw(_internal_screen, x, y, width, height);
		}
		if (this.isHovered) hover.run();
		GlStateManager.translatef(0, 0,-z);
	}

	@Override
	public void onPress() {
		try {
			super.onPress();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String value() {
		return text();
	}

	@Override
	public void value(String value) {
		text(value);
	}

}
