package sybyline.anduril.scripting.data;

import java.util.HashMap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import sybyline.anduril.scripting.api.data.IScriptDataObject;
import sybyline.satiafenris.ene.Convert;

public class ScriptDataObject extends HashMap<String, Object> implements IScriptDataObject {

	public ScriptDataObject() {}

	public ScriptDataObject(INBT map) {
		if (map instanceof CompoundNBT) {
			CompoundNBT nbt = (CompoundNBT)map;
			nbt.keySet().forEach(id -> {
				this.put(id, Convert.js_of(nbt.get(id)));
			});
		}
	}

	CompoundNBT toCompound() {
		CompoundNBT nbt = new CompoundNBT();
		this.forEach((id, object) -> {
			nbt.put(id, Convert.nbt_of(object));
		});
		return nbt;
	}

	private static final long serialVersionUID = -6363329163420937511L;

}
