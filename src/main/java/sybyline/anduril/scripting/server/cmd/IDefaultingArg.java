package sybyline.anduril.scripting.server.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundNBT;

public interface IDefaultingArg<T> {

	public void addInstance(CommandContext<CommandSource> context, ArgMap argMap) throws CommandSyntaxException;

	public static IDefaultingArg<Boolean> ofBoolean(String name, Boolean def) {
		return def == null ? RequiredArg.ofBoolean(name) : DefaultingArg.ofBoolean(name, def);
	}

	public static IDefaultingArg<Integer> ofInteger(String name, Integer def) {
		return def == null ? RequiredArg.ofInteger(name) : DefaultingArg.ofInteger(name, def);
	}

	public static IDefaultingArg<Double> ofDouble(String name, Double def) {
		return def == null ? RequiredArg.ofDouble(name) : DefaultingArg.ofDouble(name, def);
	}

	public static IDefaultingArg<String> ofString(String name, String def) {
		return def == null ? RequiredArg.ofString(name) : DefaultingArg.ofString(name, def);
	}

	public static IDefaultingArg<CompoundNBT> ofCompound(String name, CompoundNBT def) {
		return def == null ? RequiredArg.ofCompound(name) : DefaultingArg.ofCompound(name, def);
	}

}
