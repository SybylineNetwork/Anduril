package sybyline.anduril.scripting.client;

import sybyline.anduril.scripting.api.client.IScriptWindow;

public final class ScriptWindow implements IScriptWindow {

	private final ScriptGuiWrapper<?> parentScreen;

	ScriptWindow(ScriptGuiWrapper<?> scriptGuiScreen) {
		parentScreen = scriptGuiScreen;
	}

	public int window_width() {
		return parentScreen.width;
	}

	public int window_height() {
		return parentScreen.height;
	}

	public int x() {
		return parentScreen.posX;
	}

	public int y() {
		return parentScreen.posY;
	}

	public int w() {
		return parentScreen.widthGui;
	}

	public int h() {
		return parentScreen.heightGui;
	}

	public void x(int x) {
		parentScreen.posX = x;
	}

	public void y(int y) {
		parentScreen.posY = y;
	}

	public void w(int w) {
		parentScreen.widthGui = w;
	}

	public void h(int h) {
		parentScreen.heightGui = h;
	}
	
}