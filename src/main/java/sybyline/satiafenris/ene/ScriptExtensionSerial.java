package sybyline.satiafenris.ene;

import net.minecraft.nbt.INBT;

public interface ScriptExtensionSerial<Thing extends ScriptExtensionSerial<Thing, NBT>, NBT extends INBT> extends ScriptExtension<Thing> {

	public NBT toNBT();

}