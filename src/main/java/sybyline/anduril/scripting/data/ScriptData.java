package sybyline.anduril.scripting.data;

import java.util.*;
import net.minecraft.nbt.*;
import sybyline.anduril.scripting.api.data.*;
import sybyline.satiafenris.ene.Convert;

public class ScriptData implements IScriptData {

	public ScriptData() {}

	public ScriptData(CompoundNBT nbt_domain) {
		CompoundNBT nbt_generics = nbt_domain.getCompound("generics"); nbt_generics.keySet().forEach(id -> {
			generics.put(id, Convert.js_of(nbt_generics.get(id)));
		});
		CompoundNBT nbt_lists = nbt_domain.getCompound("lists"); nbt_lists.keySet().forEach(id -> {
			lists.put(id, new ScriptDataList(nbt_lists.get(id)));
		});
		CompoundNBT nbt_objects = nbt_domain.getCompound("objects"); nbt_objects.keySet().forEach(id -> {
			objects.put(id, new ScriptDataObject(nbt_objects.get(id)));
		});
	}

	public CompoundNBT toCompound() {
		CompoundNBT nbt_domain = new CompoundNBT();
		CompoundNBT nbt_generics = new CompoundNBT();
			generics.forEach((id, object) -> nbt_generics.put(id, Convert.nbt_of(object)));
			nbt_domain.put("generics", nbt_generics);
		CompoundNBT nbt_lists = new CompoundNBT();
			lists.forEach((id, object) -> nbt_lists.put(id, object.toListNBT()));
			nbt_domain.put("lists", nbt_lists);
		CompoundNBT nbt_objects = new CompoundNBT();
			objects.forEach((id, object) -> nbt_objects.put(id, object.toCompound()));
			nbt_domain.put("objects", nbt_objects);
		return nbt_domain;
	}

	private final Map<String, Object> generics = new HashMap<>();
	private final Map<String, ScriptDataList> lists = new HashMap<>();
	private final Map<String, ScriptDataObject> objects = new HashMap<>();

	@Override
	public boolean containsData(String pathkey) {
		return generics.containsKey(pathkey);
	}

	@Override
	public boolean containsList(String pathkey) {
		return lists.containsKey(pathkey);
	}

	@Override
	public boolean containsObject(String pathkey) {
		return objects.containsKey(pathkey);
	}

	@Override
	public Object getData(String pathkey) {
		return generics.get(pathkey);
	}

	@Override
	public void setData(String pathkey, Object value) {
		generics.put(pathkey, value);
	}

	@Override
	public IScriptDataList newList() {
		return new ScriptDataList();
	}

	@Override
	public IScriptDataList getList(String pathkey) {
		return lists.get(pathkey);
	}

	@Override
	public void setList(String pathkey, IScriptDataList value) {
		lists.put(pathkey, (ScriptDataList)value);
	}

	@Override
	public IScriptDataObject newObject() {
		return new ScriptDataObject();
	}

	@Override
	public IScriptDataObject getObject(String pathkey) {
		return objects.get(pathkey);
	}

	@Override
	public void setObject(String pathkey, IScriptDataObject value) {
		objects.put(pathkey, (ScriptDataObject)value);
	}

}
