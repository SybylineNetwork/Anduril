package sybyline.anduril.scripting.client;

import java.util.List;
import org.lwjgl.opengl.*;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import sybyline.anduril.scripting.api.*;
import sybyline.anduril.scripting.common.*;
import sybyline.satiafenris.ene.*;

public class ScriptGuiWrapper<SubScreen extends FocusableGui & IRenderable> extends Screen {

	public ScriptGuiWrapper(ResourceLocation resource_id, String js, Object data) {
		super(new StringTextComponent(resource_id.toString()));
		
		this.resource_id = resource_id;
		
		this.allowClasses();
		if (CommonScripting.INSTANCE.areClientAddonsEnabled()) {
			CommonScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
			ClientScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
		}
		this.serverData(data, false);
		this.script.bind("util", ScriptUtil.INSTANCE);
		this.script.bind("screen", this.screen);
		this.script.bind("gui", this.gui);
		this.script.eval(js);
		
		this.script_tick = script.wrap("tick")::apply;
		this.script_init = script.wrap("init")::apply;
		this.script_update_data = script.wrap("update_data")::apply;
		this.script_render_pre = script.wrap("render_pre")::apply;
		this.script_render = script.wrap("render")::apply;
		this.script_render_post = script.wrap("render_post")::apply;
	}

	public ScriptGuiWrapper<?> parent = null;

	private void allowClasses() {
		script.strict().allowClasses(
			GLCapabilities.class,
			GL11.class, GL12.class, GL13.class, GL14.class, GL15.class,
			GL20.class, GL21.class,
			GL30.class, GL31.class, GL32.class, GL33.class,
			GL40.class, GL41.class, GL42.class, GL43.class, GL44.class, GL45.class, GL46.class,
			I18n.class,
			"java.math.*",
			"sybyline.anduril.util.math.*",
			Object.class
		);
	}

	private void bindVarsTick(int ticks) {
		script.bind("ticks", ticks);
	}

	private void bindVarsRender(int mouseX, int mouseY, float partialTicks) {
		script.bind("mouseX", mouseX);
		script.bind("mouseY", mouseY);
		script.bind("partialTicks", partialTicks);
	}

	public final Script script = ScriptWrapper.newScript();

	public final ResourceLocation resource_id;
	public final ScriptWindow screen = new ScriptWindow(this);
	public final ScriptGui<SubScreen> gui = new ScriptGui<SubScreen>(this);

	public final Runnable script_tick;
	public final Runnable script_init;
	public final Runnable script_update_data;
	public final Runnable script_render_pre;
	public final Runnable script_render;
	public final Runnable script_render_post;

	// Values

	int posX, posY, heightGui, widthGui;
	float texW = 256F, texH = 256F;
	final IntStack frames = new IntArrayList();
	final List<SubScreen> kiddos = Lists.newArrayList();

	public final void serverData(Object data, boolean doesUpdate) throws ScriptRuntimeException{
		if (data == null) {
			script.eval("data={};");
		} else {
			script.bind("data", Convert.js_of(data, script));
		}
		if (doesUpdate) try {
			script_update_data.run();
		} catch(Exception e) {
			e.printStackTrace();
			gui.gui_close();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
	    this.minecraft.displayGuiScreen(parent);
	}

	public void onExit() {
	    this.minecraft.displayGuiScreen(null);
	}

	@Override
	public void tick() {
		this.bindVarsTick(minecraft.player.ticksExisted);
		super.tick();
		try {
			script_tick.run();
		} catch(Exception e) {
			e.printStackTrace();
			gui.gui_close();
		}
	}

	@Override
	public void init() {
		kiddos.clear();
		super.init();
		try {
			script_init.run();
		} catch(Exception e) {
			e.printStackTrace();
			gui.gui_close();
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.bindVarsRender(mouseX, mouseY, partialTicks);
		try {
			script_render_pre.run();
			super.render(mouseX, mouseY, partialTicks);
			script_render.run();
			for (SubScreen kiddo : kiddos) kiddo.render(mouseX, mouseY, partialTicks);
			script_render_post.run();
		} catch(Exception e) {
			e.printStackTrace();
			gui.gui_close();
		}
	}

	ItemRenderer getItemRenderer() {
		return itemRenderer;
	}

	FontRenderer getFont() {
		return font;
	}

	<T extends Widget> T _addButton(T widget) {
		return this.addButton(widget);
	}

	List<IGuiEventListener> _children() {
		return children;
	}

	void _hLine(int i1, int i2, int i3, int i4) {
		this.hLine(i1, i2, i3, i4);
	}

	void _vLine(int i1, int i2, int i3, int i4) {
		this.vLine(i1, i2, i3, i4);
	}

	void _fillGradient(int i1, int i2, int i3, int i4, int i5, int i6) {
		this.fillGradient(i1, i2, i3, i4, i5, i6);
	}

}
