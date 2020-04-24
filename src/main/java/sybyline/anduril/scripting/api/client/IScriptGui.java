package sybyline.anduril.scripting.api.client;

import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import sybyline.anduril.scripting.api.common.IMCResource;

public interface IScriptGui<SubScreen extends FocusableGui & IRenderable> {

	public void gui_exit();

	public void gui_close();

	public default void gui_display(IMCResource location) {
		gui_display(location, null);
	}

	public void gui_display(IMCResource location, Object data);

	public void gui_clickSound();

	// Drawing

	// Background

	public void draw_gradientBackground();

	public void draw_gradientBackground(int color);

	// Shapes

	public void draw_lineHorizontal(int p_hLine_1_, int p_hLine_2_, int p_hLine_3_, int p_hLine_4_);

	public void draw_lineVertical(int p_vLine_1_, int p_vLine_2_, int p_vLine_3_, int p_vLine_4_);

	public void draw_pointSmall(int x, int y, int color);

	public void draw_point(int x, int y, int color);

	public void draw_pointLarge(int x, int y, int color);

	public void draw_circleHollow(int x, int y, int color);

	public void draw_circle(int x, int y, int color);

	// Quads

	public void draw_gradientFill(int p_fillGradient_1_, int p_fillGradient_2_, int p_fillGradient_3_, int p_fillGradient_4_, int p_fillGradient_5_, int p_fillGradient_6_);

	public void draw_blitStretch(int x, int y, int w, int h, float u, float v, float s, float t);

	public void draw_blitStretch(int x, int y, int w, int h, float u, float v, float s, float t, float texW, float texH);

	public void draw_blit(int p_blit_1_, int p_blit_2_, int p_blit_3_, int p_blit_4_, int p_blit_5_, int p_blit_6_);

	public void draw_blankBackground();

	public void draw_blankBackground(int x, int y, int w, int h);

	// Text

	public default void draw_textLeft(String msg, int x, int y, int color) {
		this.draw_text(msg, x, y, color);
	}

	public void draw_textCenter(String msg, int x, int y, int color);

	public void draw_textRight(String msg, int x, int y, int color);

	public void draw_text(String msg, int x, int y, int color);

	public FontRenderer font();

	// Item

	public default void draw_item(ItemStack stack, int x, int y) {
		this.draw_item(stack, x, y, null, true);
	}

	public default void draw_item(ItemStack stack, int x, int y, boolean overlay) {
		this.draw_item(stack, x, y, null, overlay);
	}

	public default void draw_item(ItemStack stack, int x, int y, String text) {
		this.draw_item(stack, x, y, text, true);
	}

	public void draw_item(ItemStack stack, int x, int y, String text, boolean overlay);

	// Tooltip

	public void draw_tooltip(String msg, int x, int y);

	public void draw_tooltip(List<String> msg, int x, int y);

	// Rendering

	// Depth (z)

	public int depth_get();

	public int depth_set(int newBlitOffset);

	public default void depth_push() {
		this.depth_push(1);
	}

	public void depth_push(int i);

	public void depth_pop();

	public void depth_pushGL();

	public void depth_popGL();

	// Screen objects

	public void screen_add(Widget button);

	public void screen_add(SubScreen child);

	public void screen_listen(IGuiEventListener listener);

	// Resources

	public void bind_resource(IMCResource resource);

	// Simple textures

	public IScriptWidgetTexture new_textureSimple(IMCResource location);

	// Texture sets

	public IScriptWidgetTextureSet new_textureSet(IMCResource location, int width, int height);

	// Buttons

	public default IScriptButton new_button(int x, int y, int w, int h, String msg, Runnable clickAction) {
		return new_button(x, y, w, h, 1, msg, clickAction);
	}

	public default IScriptButton new_button(int x, int y, int w, int h, String msg, Runnable clickAction, Runnable hoverAction) {
		return new_button(x, y, w, h, 1, msg, clickAction, hoverAction);
	}

	public default IScriptButton new_button(int x, int y, int w, int h, int z, String msg, Runnable clickAction) {
		return new_button(x, y, w, h, z, msg, clickAction, () -> {});
	}

	public IScriptButton new_button(int x, int y, int w, int h, int z, String msg, Runnable clickAction, Runnable hoverAction);

	// List

	public IScriptPane new_list(int x, int y, int w, int h, int x_offset, int y_offset, int piece_w, int piece_h);

	// Text field

	public default IScriptInput<String> new_textfield(int x, int y, int w, int h) {
		return this.new_textfield(x, y, w, h, "");
	}
	
	public IScriptInput<String> new_textfield(int x, int y, int w, int h, String text);

	// Slider

	public default IScriptInput<Double> new_slider(int x, int y, int w, int h, String message, double initial) {
		return this.new_slider(x, y, w, h, message, initial, null);
	}

	public default IScriptInput<Double> new_slider(int x, int y, int w, int h, String message, double initial, Consumer<IScriptInput<Double>> update) {
		return this.new_slider(x, y, w, h, message, initial, update, null);
	}

	public IScriptInput<Double> new_slider(int x, int y, int w, int h, String text, double initial, Consumer<IScriptInput<Double>> update, Consumer<IScriptInput<Double>> apply);

	// Checkbox

	public default IScriptInput<Boolean> new_checkbox(int x, int y, int w, int h, String message) {
		return this.new_checkbox(x, y, w, h, message, false);
	}

	public default IScriptInput<Boolean> new_checkbox(int x, int y, int w, int h, String message, boolean startsChecked) {
		return this.new_checkbox(x, y, w, h, message, startsChecked, null);
	}

	public IScriptInput<Boolean> new_checkbox(int x, int y, int w, int h, String text, boolean startsChecked, Consumer<IScriptInput<Boolean>> update);

	// Messaging

	public default void message_print(String message) {
		this.message_print(message, true);
	}

	public void message_print(String message, boolean type);

	public default void message_send(String message) {
		this.message_send(message, false);
	}

	public void message_send(String message, boolean record);

	public void message_openFile(String url);

	public void message_openLink(String url);

	public void message_suggest(String message);

}
