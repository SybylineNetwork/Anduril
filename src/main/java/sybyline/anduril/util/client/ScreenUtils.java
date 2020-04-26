package sybyline.anduril.util.client;

import net.minecraft.client.gui.AbstractGui;

public class ScreenUtils {

	public static void box(int x, int y, int w, int h, int color) {
		hLine(x, x + w - 1, y, color);
		hLine(x, x + w - 1, y + h - 1, color);
		vLine(x, y, y + h, color);
		vLine(x + w - 1, y, y + h, color);
	}

	public static void hLine(int x1, int x2, int y, int color) {
		if (x2 < x1) {
			int tmp = x1;
			x1 = x2;
			x2 = tmp;
		}
		AbstractGui.fill(x1, y, x2 + 1, y + 1, color);
	}

	public static void vLine(int x, int y1, int y2, int color) {
		if (y2 < y1) {
			int tmp = y1;
			y1 = y2;
			y2 = tmp;
		}
		AbstractGui.fill(x, y1 + 1, x + 1, y2, color);
	}
}
