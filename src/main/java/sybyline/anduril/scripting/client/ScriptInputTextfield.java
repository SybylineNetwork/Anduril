package sybyline.anduril.scripting.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import sybyline.anduril.scripting.api.client.IScriptInput;

public class ScriptInputTextfield extends TextFieldWidget implements IScriptInput<String> {

	public ScriptInputTextfield(FontRenderer font, int x, int y, int w, int h, String text) {
		super(font, x, y, w, h, text);
		this.setText(text);
	}

	public String value() {
		return this.getText();
	}

	public void value(String value) {
		this.setText(value);
	}

}