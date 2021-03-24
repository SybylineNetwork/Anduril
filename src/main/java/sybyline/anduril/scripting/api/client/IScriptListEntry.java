package sybyline.anduril.scripting.api.client;

public interface IScriptListEntry {

	public IScriptList parent();

	public int index();

	public int entry_width();

	public int entry_height();

	public default int w() {
		return this.entry_width();
	}

	public default int h() {
		return this.entry_height();
	}

	public int pos_getX();

	public int pos_getY();

	public default int x() {
		return this.pos_getX();
	}

	public default int y() {
		return this.pos_getY();
	}

	public default int xMax() {
		return this.pos_getX() + this.entry_width();
	}

	public default int yMax() {
		return this.pos_getY() + this.entry_height();
	}

	public default boolean contains(int mouseX, int mouseY) {
		if (parent().contains(mouseX, mouseY)) {
			mouseX -= pos_getX();
			mouseY -= pos_getY();
			return (0 <= mouseX && mouseX < entry_width()) && (0 <= mouseY && mouseY < entry_height());
		} else {
			return false;
		}
	}

}
