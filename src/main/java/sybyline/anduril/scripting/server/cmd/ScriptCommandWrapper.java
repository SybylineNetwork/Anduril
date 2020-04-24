package sybyline.anduril.scripting.server.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.scripting.common.ScriptWrapper;
import sybyline.anduril.util.data.IFormat;

public class ScriptCommandWrapper extends ScriptWrapper<LiteralArgumentBuilder<CommandSource>> {

	public ScriptCommandWrapper(String commandname, String source) {
		super(commandname, source);
	}

	public final ScriptCommand cmd = new ScriptCommand(this);

	@Override
	public LiteralArgumentBuilder<CommandSource> setupWithContext(LiteralArgumentBuilder<CommandSource> literal) {
		this.cmd.literal = literal;
		this.setupInternal();
		return this.cmd.literal;
	}

	@Override
	protected void bindVariables() {
		this.script.bind("cmd", this.cmd);
	}

	public static final IFormat<ScriptCommandWrapper> FORMAT = ScriptWrapper.formatOf(ScriptCommandWrapper::new);

	@Override
	protected LogicalSide side() {
		return LogicalSide.SERVER;
	}

}
