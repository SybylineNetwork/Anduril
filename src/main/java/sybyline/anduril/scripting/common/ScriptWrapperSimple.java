package sybyline.anduril.scripting.common;

import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.scripting.common.ScriptWrapper;
import sybyline.anduril.util.data.IFormat;

public class ScriptWrapperSimple extends ScriptWrapper<Void> {

	public ScriptWrapperSimple(String name, String source) {
		super(name, source);
	}

	@Override
	public Void setupWithContext(Void literal) {
		this.setupInternal();
		return literal;
	}

	@Override
	protected void bindVariables() {
	}

	public static final IFormat<ScriptWrapperSimple> FORMAT = ScriptWrapper.formatOf(ScriptWrapperSimple::new);

	@Override
	protected LogicalSide side() {
		return LogicalSide.SERVER;
	}

}
