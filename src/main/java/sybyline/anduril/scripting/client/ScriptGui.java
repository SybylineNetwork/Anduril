package sybyline.anduril.scripting.client;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL11;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import sybyline.anduril.scripting.api.client.*;
import sybyline.anduril.scripting.common.*;
import sybyline.anduril.util.Util;

@SuppressWarnings("resource")
public final class ScriptGui<SubScreen extends FocusableGui & IRenderable> implements IScriptGui<SubScreen> {

	// Rendering utilities

	private final ScriptGuiWrapper<SubScreen> scriptParent;

	ScriptGui(ScriptGuiWrapper<SubScreen> scriptParent) {
		this.scriptParent = scriptParent;
	}

	public void gui_exit() {
		this.scriptParent.onExit();
	}

	public void gui_close() {
		this.scriptParent.onClose();
	}

	public void gui_display(ResourceLocation location, Object data) {
		ScriptGuiWrapper<?> newScreen = ClientScripting.INSTANCE.tryOpen(location, data);
		if (newScreen != null) {
			newScreen.parent = this.scriptParent;
		}
	}
	
	public void gui_clickSound() {
		this.scriptParent.getMinecraft().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public void bind_resource(ResourceLocation resource) {
		this.scriptParent.getMinecraft().getTextureManager().bindTexture(resource);
	}

	public void draw_item(ItemStack stack, int x, int y, String text, boolean overlay) {
		if (overlay) {
			this.scriptParent.getItemRenderer().renderItemOverlayIntoGUI(this.font(), stack, x, y, text);
		}
		RenderHelper.enableStandardItemLighting();
		this.scriptParent.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
		RenderHelper.disableStandardItemLighting();
	}

	public int depth_get() {
		return this.scriptParent.getBlitOffset();
	}

	public int depth_set(int newBlitOffset) {
		int ret = this.scriptParent.getBlitOffset();
		this.scriptParent.setBlitOffset(newBlitOffset);
		return ret;
	}

	public void depth_push(int i) {
		int bO = this.scriptParent.getBlitOffset();
		bO += i;
		this.scriptParent.setBlitOffset(bO);
		this.scriptParent.frames.push(i);
	}

	public void depth_pop() {
		int bO = this.scriptParent.getBlitOffset();
		bO -= this.scriptParent.frames.popInt();
		this.scriptParent.setBlitOffset(bO);
	}

	public void draw_tooltip(String msg, int x, int y) {
		this.scriptParent.renderTooltip(msg, x, y);
        RenderSystem.disableLighting();
	}

	public void draw_tooltip(List<String> msg, int x, int y) {
		this.scriptParent.renderTooltip(msg, x, y, this.font());
        RenderSystem.disableLighting();
	}

	public void message_print(String message, boolean chat) {
		this.scriptParent.getMinecraft().ingameGUI.addChatMessage(chat ? ChatType.CHAT : ChatType.GAME_INFO, new StringTextComponent(message));
	}

	public void message_send(String message, boolean record) {
		this.scriptParent.sendMessage(message, record);
	}

	public void message_openFile(String path) {
		this.open_uri(new File(path).toURI());
	}

	public void message_openLink(String url) {
		if (!this.scriptParent.getMinecraft().gameSettings.chatLinks) {
			this.message_print("Links are turned off. <"+url+">");
			return;
		}
		try {
			URI uri = new URI(url);
			String s = uri.getScheme();
			if (s == null) throw new URISyntaxException(url, "Missing protocol");
			if (!_protocols.contains(s.toLowerCase())) throw new URISyntaxException(url, "Unsupported protocol: " + s.toLowerCase());
			if (this.scriptParent.getMinecraft().gameSettings.chatLinksPrompt) this.scriptParent.getMinecraft().displayGuiScreen(new ConfirmOpenLinkScreen(bool -> {
				if (bool) this.open_uri(uri);
				this.scriptParent.getMinecraft().displayGuiScreen(this.scriptParent);
			}, url, false));
			else this.open_uri(uri);
		} catch (URISyntaxException e) {
			CommonScripting.LOGGER.error("Can't open url for {}", url, e);
		}
	}

	//Internal
	private final Set<String> _protocols = Sets.newHashSet("http", "https");
	private final void open_uri(URI uri) {
		net.minecraft.util.Util.getOSType().openURI(uri);
	}

	public void message_suggest(String message) {
		this.gui_close();
		this.scriptParent.getMinecraft().displayGuiScreen(new ChatScreen(message));
	}

	public void draw_gradientBackground() {
		this.scriptParent.renderBackground();
	}

	public void draw_gradientBackground(int color) {
		this.scriptParent.renderBackground(color);
	}

	public void draw_blit(int p_blit_1_, int p_blit_2_, int p_blit_3_, int p_blit_4_, int p_blit_5_, int p_blit_6_) {
		this.scriptParent.blit(p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_);
	}

	public void draw_textCenter(String msg, int x, int y, int color) {
		this.scriptParent.drawCenteredString(this.font(), msg, x, y, color);
	}

	public void draw_textRight(String msg, int x, int y, int color) {
		this.scriptParent.drawRightAlignedString(this.font(), msg, x, y, color);
	}

	public void draw_text(String msg, int x, int y, int color) {
		this.scriptParent.drawString(this.font(), msg, x, y, color);
	}

	public FontRenderer font() {
		return this.scriptParent.getFont();
	}

	public void draw_gradientFill(int p_fillGradient_1_, int p_fillGradient_2_, int p_fillGradient_3_, int p_fillGradient_4_, int p_fillGradient_5_, int p_fillGradient_6_) {
		this.scriptParent._fillGradient(p_fillGradient_1_, p_fillGradient_2_, p_fillGradient_3_, p_fillGradient_4_, p_fillGradient_5_, p_fillGradient_6_);
	}

	public void draw_lineHorizontal(int p_hLine_1_, int p_hLine_2_, int p_hLine_3_, int p_hLine_4_) {
		this.scriptParent._hLine(p_hLine_1_, p_hLine_2_, p_hLine_3_, p_hLine_4_);
	}

	public void draw_lineVertical(int p_vLine_1_, int p_vLine_2_, int p_vLine_3_, int p_vLine_4_) {
		this.scriptParent._vLine(p_vLine_1_, p_vLine_2_, p_vLine_3_, p_vLine_4_);
	}

	// For rendering text, which doesn't respect the current blitOffset
	public void depth_pushGL() {
        RenderSystem.translatef(0, 0, this.scriptParent.getBlitOffset());
	}

	// For rendering text, which doesn't respect the current blitOffset
	public void depth_popGL() {
		RenderSystem.translatef(0, 0, -this.scriptParent.getBlitOffset());
	}

	public void draw_pointSmall(int x, int y, int color) {
		this.font().drawString(".", x, y - 6, color);
	}

	public void draw_point(int x, int y, int color) {
		this.font().drawString("\u00b0", x - 1, y - 1, 0xFF000000);
		this.font().drawString(".", x, y - 6, color);
	}

	public void draw_pointLarge(int x, int y, int color) {
		this.font().drawString("*", x - 1, y - 1, color);
		this.font().drawString("\u00b0", x - 1, y - 1, color);
		this.font().drawString(".", x, y - 6, color);
	}

	public void draw_circleHollow(int x, int y, int color) {
		this.font().drawString("o", x - 2, y - 4, color);
	}

	public void draw_circle(int x, int y, int color) {
		this.font().drawString("o", x - 2, y - 4, 0xFF000000);
		this.font().drawString("*", x - 1, y - 1, color);
		this.font().drawString("\u00b0", x - 1, y - 1, color);
		this.font().drawString(".", x, y - 6, color);
	}

	public IScriptWidgetTexture new_textureSimple(ResourceLocation location) {
		return IScriptWidgetTexture.of(location);
	}

	public IScriptWidgetTextureSet new_textureSet(ResourceLocation location, int width, int height) {
		return IScriptWidgetTextureSet.of(location, height, height);
	}

	public IScriptPane new_list(int x, int y, int w, int h, int x_offset, int y_offset, int piece_w, int piece_h) {
		return new ScriptList(this.scriptParent, x, y, w, h, x_offset, y_offset, piece_w, piece_h);
	}

	public IScriptButton new_button(int x, int y, int w, int h, int z, String msg, Runnable clickAction, Runnable hoverAction) {
		ScriptButton ret = new ScriptButton(x, y, w, h, z, msg, clickAction, hoverAction);
		ret._internal_screen = this;
		return ret;
	}
	
	public IScriptInput<String> new_textfield(int x, int y, int w, int h, String text) {
		return new ScriptInputTextfield(font(), x, y, w, h, text);
	}

	public IScriptInput<Double> new_slider(int x, int y, int w, int h, String text, double initial, Consumer<IScriptInput<Double>> update, Consumer<IScriptInput<Double>> apply) {
		return new ScriptInputSlider(x, y, w, h, text, initial, update, apply);
	}

	public IScriptInput<Boolean> new_checkbox(int x, int y, int w, int h, String text, boolean startsChecked, Consumer<IScriptInput<Boolean>> update) {
		return new ScriptInputCheckbox(x, y, w, h, text, startsChecked, update);
	}

	public void screen_add(Widget button) {
		this.scriptParent._addButton(button);
	}

	public void screen_add(SubScreen child) {
		this.scriptParent.kiddos.add(child);
		this.scriptParent._children().add(child);
	}

	public void screen_listen(IGuiEventListener listener) {
		this.scriptParent._children().add(listener);
	}

	public void draw_blankBackground() {
		draw_blankBackground(this.scriptParent.posX, this.scriptParent.posY, this.scriptParent.widthGui, this.scriptParent.heightGui);
	}

	private static final ResourceLocation CUSTOM_MENU = new ResourceLocation(Util.ANDURIL, "textures/custom_menu.png");

	public void draw_blankBackground(int x, int y, int w, int h) {
		this.scriptParent.getMinecraft().getTextureManager().bindTexture(CUSTOM_MENU);
		this.draw_blit(x - 4, y - 4, 0, 0, 4, 4);
		this.draw_blit(x + w, y - 4, 4, 0, 4, 4);
		this.draw_blit(x - 4, y + h, 0, 4, 4, 4);
		this.draw_blit(x + w, y + h, 4, 4, 4, 4);
		this.draw_blitStretch(x, y - 4, w, 4, 8, 0, 1, 4);
		this.draw_blitStretch(x, y + h, w, 4, 8, 4, 1, 4);
		this.draw_blitStretch(x - 4, y, 4, h, 0, 8, 4, 1);
		this.draw_blitStretch(x + w, y, 4, h, 4, 8, 4, 1);
		this.draw_gradientFill(x, y, x + w, y + h, 0xFFC6C6C6, 0xFFC6C6C6);
	}

	public void draw_blitStretch(int x, int y, int w, int h, float u, float v, float s, float t) {
		draw_blitStretch(x, y, w, h, u, v, s, t, this.scriptParent.texW, this.scriptParent.texH);
	}

	public void draw_blitStretch(int x, int y, int w, int h, float u, float v, float s, float t, float texW, float texH) {
		u /= texW; s /= texW;
		v /= texH; t /= texH;
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buff = tess.getBuffer();
		buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			int blitOffset = this.scriptParent.getBlitOffset();
			buff.pos(x    , y    , blitOffset).tex(u    , v    ).endVertex();
			buff.pos(x    , y + h, blitOffset).tex(u    , v + t).endVertex();
			buff.pos(x + w, y + h, blitOffset).tex(u + s, v + t).endVertex();
			buff.pos(x + w, y    , blitOffset).tex(u + s, v    ).endVertex();
		tess.draw();
	}

}