package sybyline.anduril.scripting.client;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.button.CheckboxButton;
import sybyline.anduril.scripting.api.client.IScriptInput;

public class ScriptInputCheckbox extends CheckboxButton implements IScriptInput<Boolean> {

	public ScriptInputCheckbox(int x, int y, int w, int h, String label, boolean beginsChecked, Consumer<IScriptInput<Boolean>> update) {
		super(x, y, w, h, label, beginsChecked);
		this.update = update != null ? update : __ -> {};
	}

	private final Consumer<IScriptInput<Boolean>> update;

	public void onPress() {
		super.onPress();
		try {
			update.accept(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Boolean value() {
		return this.isChecked();
	}

	public void value(Boolean value) {
		if (value() ^ value) this.onPress();
	}

}
