package sybyline.anduril.scripting.api.client;

import net.minecraft.client.gui.widget.Widget;

public interface IScriptInput<Value> extends IScriptPane {

	public Value value();

	public void value(Value value);

	public default String text() {
		return ((Widget)this).getMessage();
	}

	public default void text(String text) {
		((Widget)this).setMessage(text);
	}

	public default int x() {
		return ((Widget)this).x;
	}

	public default int y() {
		return ((Widget)this).y;
	}

	public default int w() {
		return ((Widget)this).getWidth();
	}

	public default int h() {
		return ((Widget)this).getHeight();
	}

	public default void x(int x) {
		((Widget)this).x = x;
	}

	public default void y(int y) {
		((Widget)this).y = y;
	}

	public default void w(int w) {
		((Widget)this).setWidth(w);
	}

	public default void h(int h) {
		((Widget)this).setHeight(h);
	}

}
