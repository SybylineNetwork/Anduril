package sybyline.anduril.scripting.server.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

public interface IDefaultingArg<T> {

	public void addInstance(CommandContext<CommandSource> context, ArgMap argMap) throws CommandSyntaxException;

}
