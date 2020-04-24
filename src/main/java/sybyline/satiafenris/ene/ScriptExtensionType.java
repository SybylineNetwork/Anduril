package sybyline.satiafenris.ene;

import net.minecraft.nbt.INBT;

public interface ScriptExtensionType<Thing extends ScriptExtension<Thing>> {

	@SuppressWarnings("unchecked")
	public default INBT convertDefault(Object thing) {
		return this.convert((Thing)thing);
	}

	public INBT convert(Thing thing);

	public Thing tryConvert(INBT nbt);

	public static <Thing extends ScriptExtension<Thing>> ScriptExtensionType<Thing> register(ScriptExtensionType<Thing> adapter) {
		ScriptExtensions.register(adapter);
		return adapter;
	}

}