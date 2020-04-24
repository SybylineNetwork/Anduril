package sybyline.satiafenris.ene;

public interface ScriptExtension<Thing extends ScriptExtension<Thing>> extends ScriptBridge {

	public ScriptExtensionType<Thing> getTypifier();

}