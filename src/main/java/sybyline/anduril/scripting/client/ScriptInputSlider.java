package sybyline.anduril.scripting.client;

import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.AbstractSlider;
import sybyline.anduril.scripting.api.client.IScriptInput;

public class ScriptInputSlider extends AbstractSlider implements IScriptInput<Double> {

	@SuppressWarnings("resource")
	public ScriptInputSlider(int x, int y, int w, int h, String text, double initial, Consumer<IScriptInput<Double>> update, Consumer<IScriptInput<Double>> apply) {
		super(Minecraft.getInstance().gameSettings, x, y, w, h, initial);
		this.update = update != null ? update : d -> {};
		this.apply = apply != null ? apply : d -> {};
		this.setMessage(text);
	}

	public final Consumer<IScriptInput<Double>> update;
	public final Consumer<IScriptInput<Double>> apply;

	protected void updateMessage() {
		try {
			update.accept(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected void applyValue() {
		try {
			apply.accept(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String text() {
		return this.getMessage();
	}

	public void text(String text) {
		this.setMessage(text);
	}

	public Double value() {
		return this.value;
	}

	public void value(Double value) {
		this.value = value;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int w() {
		return width;
	}

	public int h() {
		return height;
	}

	public void x(int x) {
		this.x = x;
	}

	public void y(int y) {
		this.y = y;
	}

	public void w(int w) {
		this.width = w;
	}

	public void h(int h) {
		this.height = h;
	}

}
