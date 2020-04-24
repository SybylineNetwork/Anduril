package sybyline.anduril.scripting.data;

import java.util.ArrayList;

import net.minecraft.nbt.*;
import sybyline.anduril.scripting.api.data.IScriptDataList;
import sybyline.satiafenris.ene.Convert;

public class ScriptDataList extends ArrayList<Object> implements IScriptDataList {

	public ScriptDataList() {}

	public ScriptDataList(INBT read) {
		if (read instanceof ListNBT) {
			ListNBT nbt = (ListNBT)read;
			for (int i = 0; i < nbt.size(); i++) {
				this.add(Convert.js_of(nbt.get(i)));
			}
		}
	}

	ListNBT toListNBT() {
		ListNBT nbt = new ListNBT();
		for (int i = 0; i < this.size(); i++) {
			nbt.add(Convert.nbt_of(this.get(i)));
		}
		return nbt;
	}

	private static final long serialVersionUID = -4828619491792991985L;

}
