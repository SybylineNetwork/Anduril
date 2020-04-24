package sybyline.anduril.scripting.api.client;

import java.util.function.*;

public interface IScriptList extends IScriptPane {

	public IScriptListEntry new_entry(Consumer<IScriptListEntry> render, Consumer<IScriptListEntry> hover, Predicate<IScriptListEntry> click);

	public IScriptListEntry last_selected();

	public default boolean contains(int mouseX, int mouseY) {
		mouseX -= x();
		mouseY -= y();
		return (0 <= mouseX && mouseX < w() - 6) && (0 <= mouseY && mouseY < h());
		// -6 is for slider border
	}

}
