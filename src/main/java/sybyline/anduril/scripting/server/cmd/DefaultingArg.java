package sybyline.anduril.scripting.server.cmd;

import java.util.function.BiFunction;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.nbt.CompoundNBT;

public class DefaultingArg<T> extends NamedArg<T> {

	protected DefaultingArg(String name, T def, BiFunction<CommandContext<CommandSource>, String, T> getter) {
		super(name);
		this.def = def;
		this.getter = getter;
	}

	private final T def;
	private final BiFunction<CommandContext<CommandSource>, String, T> getter;

	@Override
	public void addInstance(CommandContext<CommandSource> context, ArgMap argMap) throws CommandSyntaxException {
		try {
			T val = getter.apply(context, name);
			argMap.present(name, val);
		} catch(IllegalArgumentException ifDoesNotExist) {
			argMap.absent(name, def);
		}
	}

	public static IDefaultingArg<Boolean> ofBoolean(String name, Boolean def) {
		return new DefaultingArg<Boolean>(name, def, BoolArgumentType::getBool);
	}

	public static IDefaultingArg<Integer> ofInteger(String name, Integer def) {
		return new DefaultingArg<Integer>(name, def, IntegerArgumentType::getInteger);
	}

	public static IDefaultingArg<Double> ofDouble(String name, Double def) {
		return new DefaultingArg<Double>(name, def, DoubleArgumentType::getDouble);
	}

	public static IDefaultingArg<String> ofString(String name, String def) {
		return new DefaultingArg<String>(name, def, StringArgumentType::getString);
	}

	public static IDefaultingArg<CompoundNBT> ofCompound(String name, CompoundNBT def) {
		return new DefaultingArg<CompoundNBT>(name, def, NBTCompoundTagArgument::getNbt);
	}

}
