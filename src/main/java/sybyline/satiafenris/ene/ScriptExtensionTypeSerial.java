package sybyline.satiafenris.ene;

import net.minecraft.nbt.INBT;

public interface ScriptExtensionTypeSerial<Thing extends ScriptExtensionSerial<Thing, NBT>, NBT extends INBT> extends ScriptExtensionType<Thing> {

	public default INBT convert(Thing thing) {
		return thing.toNBT();
	}

	public Thing tryConvert(INBT nbt);

	public static <Thing extends ScriptExtensionSerial<Thing, NBT>, NBT extends INBT> ScriptExtensionTypeSerial<Thing, NBT> register(ScriptExtensionTypeSerial<Thing, NBT> adapter) {
		ScriptExtensions.register(adapter);
		return adapter;
	}

}