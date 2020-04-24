package sybyline.anduril.scripting.server.cmd;

public abstract class NamedArg<T> implements IDefaultingArg<T> {

	protected NamedArg(String name) {
		this.name = name;
	}

	protected final String name;

}
