package sybyline.satiafenris.ene;

public class ScriptVariableImpl<T> implements ScriptVariable<T> {

	ScriptVariableImpl(Script parent, String name) {
		this.script = parent;
		this.name = name;
	}

	protected final Script script;
	protected final String name;

	@Override
	public void implementation_set(Object value) throws Exception {
		script.set(name, value);
	}

	@Override
	public Object implementation_get() throws Exception {
		return script.get(name);
	}

}
