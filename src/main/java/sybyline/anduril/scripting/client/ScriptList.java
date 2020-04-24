package sybyline.anduril.scripting.client;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;
import sybyline.anduril.scripting.api.client.IScriptList;
import sybyline.anduril.scripting.api.client.IScriptListEntry;
import sybyline.anduril.util.rtc.FieldWrapper;

public final class ScriptList extends ScrollPanel implements IScriptList {

	final ScriptGuiWrapper<?> parentScreen;
	int piece_w, piece_h, _maxX, _baseY;
	final List<ScriptListEntry> elements = Lists.newArrayList();
	
	ScriptListEntry lastClicked = null;
	
	public ScriptList(ScriptGuiWrapper<?> parentScreen, int x, int y, int w, int h, int x_offset, int y_offset, int piece_w, int piece_h) {
		super(parentScreen.getMinecraft(), w, h, y, x);
		this.parentScreen = parentScreen;
		this.piece_w = piece_w;
		this.piece_h = piece_h;
	}

	protected int getContentHeight() {
		return elements.size() * piece_h;
	}

	protected boolean clickPanel(double mouseX, double mouseY, int button) {
		if (button != 0) return false;
		if (!this.contains((int)mouseX + left, (int)mouseY + _baseY)) return false;
		for (ScriptListEntry elem : elements) {
			int xx = 0;
			int yy = elem.idx * piece_h - 2;
			if ((mouseX > xx && mouseX < xx + piece_w - 6) && (mouseY > yy && mouseY < yy + piece_h)) {
				try {
					if (elem.click.test(elem)) {
						lastClicked = elem;
						return true;
					} else {
						return false;
					}
				} catch(Exception e) {
					e.printStackTrace();
					parentScreen.gui.gui_close();
					return false;
				}
			}
		}
		return false;
	}

	protected void drawPanel(int maxX, int baseY, Tessellator tess, int mouseX, int mouseY) {
		_maxX = maxX;
		_baseY = baseY;
		try {
			for (ScriptListEntry element : elements) {
				element.render.accept(element);
				if (element.contains(mouseX, mouseY)) {
					element.hover.accept(element);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			parentScreen.gui.gui_close();
		}
	}

	public IScriptListEntry new_entry(Consumer<IScriptListEntry> render, Consumer<IScriptListEntry> hover, Predicate<IScriptListEntry> click) {
		ScriptListEntry entry = new ScriptListEntry(this, elements.size(), render, hover, click);
		elements.add(entry);
		if (lastClicked == null) {
			lastClicked = entry;
		}
		return entry;
	}

	public IScriptListEntry last_selected() {
		return lastClicked;
	}

	public int x() {
		return left;
	}

	public int y() {
		return top;
	}

	public int w() {
		return width;
	}

	public int h() {
		return height;
	}

	private static final FieldWrapper<Integer>
		TOP = FieldWrapper.of(ScrollPanel.class, "top"),
		LEFT = FieldWrapper.of(ScrollPanel.class, "left"),
		RIGHT = FieldWrapper.of(ScrollPanel.class, "right"),
		BOTTOM = FieldWrapper.of(ScrollPanel.class, "bottom"),
		WIDTH = FieldWrapper.of(ScrollPanel.class, "width"),
		HEIGHT = FieldWrapper.of(ScrollPanel.class, "height"),
		BAR_LEFT = FieldWrapper.of(ScrollPanel.class, "barLeft");

	public void x(int x) {
		LEFT.set(this, x);
		RIGHT.set(this, this.width + x);
		BAR_LEFT.set(this, this.right - 6);
	}

	public void y(int y) {
		TOP.set(this, y);
		BOTTOM.set(this, this.height + y);
	}

	public void w(int w) {
		WIDTH.set(this, w);
		RIGHT.set(this, w + this.left);
		BAR_LEFT.set(this, this.right - 6);
	}

	public void h(int h) {
		HEIGHT.set(this, h);
		BOTTOM.set(this, h + this.top);
	}

}