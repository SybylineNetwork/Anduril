package sybyline.anduril.scripting.api.common;

public interface IMCResource extends IScriptCommandFormattable {

	public String domain();

	public String path();

	public String string();

	public default void toCommandString(StringBuilder string) {
		string.append(domain()).append(':').append(path());
	}

}
