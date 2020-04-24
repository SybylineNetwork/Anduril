package sybyline.anduril.scripting.api.data;

public interface IScriptData {

	public default boolean containsAny(String pathkey) {
		if (containsData(pathkey))
			return true;
		if (containsList(pathkey))
			return true;
		if (containsObject(pathkey))
			return true;
		return false;
	}

	public boolean containsData(String pathkey);

	public boolean containsList(String pathkey);

	public boolean containsObject(String pathkey);

	public Object getData(String pathkey);

	public void setData(String pathkey, Object value);

	public IScriptDataList newList();

	public IScriptDataList getList(String pathkey);

	public void setList(String pathkey, IScriptDataList value);

	public IScriptDataObject newObject();

	public IScriptDataObject getObject(String pathkey);

	public void setObject(String pathkey, IScriptDataObject value);

}
