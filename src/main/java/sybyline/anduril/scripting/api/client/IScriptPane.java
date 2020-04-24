package sybyline.anduril.scripting.api.client;

public interface IScriptPane {

	public int x();

	public int y();

	public int w();

	public int h();

	public default int w_h() {
		return this.w() / 2;
	}

	public default int h_h() {
		return this.h() / 2;
	}

	public void x(int x);

	public void y(int y);

	public void w(int w);

	public void h(int h);

	public default void pos(int x, int y) {
		this.x(x);
		this.y(y);
	}

	public default void size(int w, int h) {
		this.w(w);
		this.h(h);
	}

	// Shorthand

	public default int width() {
		return this.w();
	}

	public default int width_h() {
		return this.w_h();
	}

	public default int height() {
		return this.h();
	}

	public default int height_h() {
		return this.h_h();
	}

	public default boolean contains(int mouseX, int mouseY) {
		mouseX -= x();
		mouseY -= y();
		return (0 <= mouseX && mouseX < w()) && (0 <= mouseY && mouseY < h());
	}

}
