package sybyline.anduril.scripting.client;

import java.util.function.Consumer;
import java.util.function.Predicate;

import sybyline.anduril.scripting.api.client.IScriptList;
import sybyline.anduril.scripting.api.client.IScriptListEntry;

public final class ScriptListEntry implements IScriptListEntry {

	private final ScriptList parent;
	final int idx;
	final Consumer<IScriptListEntry> render;
	final Consumer<IScriptListEntry> hover;
	final Predicate<IScriptListEntry> click;
	public ScriptListEntry(ScriptList parent, int idx, Consumer<IScriptListEntry> render, Consumer<IScriptListEntry> hover, Predicate<IScriptListEntry> click) {
		this.parent = parent;
		this.idx = idx;
		this.render = render != null ? render : thiz -> {};
		this.hover = hover != null ? hover : thiz -> {};
		this.click = click != null ? click : thiz -> false;
	}

	public IScriptList parent() {
		return parent;
	}

	public int index() {
		return idx;
	}

	public int entry_width() {
		return parent.piece_w;
	}

	public int entry_height() {
		return parent.piece_h;
	}

	public int pos_getX() {
		return parent.x();
	}

	public int pos_getY() {
		return parent._baseY + index() * entry_height() - 2; // -2 is for looks
	}

}