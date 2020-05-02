package sybyline.anduril.scripting.server.cmd;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.util.function.TriFunction;

public class RequiredArg<T> extends NamedArg<T> {

	protected static final SimpleCommandExceptionType REQUIRED_ARG = new SimpleCommandExceptionType(() -> "anduril.commands.requiredarg");
	protected static final SimpleCommandExceptionType INVALID_ARG = new SimpleCommandExceptionType(() -> "anduril.commands.invalidarg");

	protected RequiredArg(String name, TriFunction<CommandContext<CommandSource>, String, String, T, CommandSyntaxException> getter) {
		super(name);
		this.getter = getter;
	}

	protected RequiredArg(String name, BiFunction<CommandContext<CommandSource>, String, T> getter) {
		super(name);
		this.getter = (ctx, str, __) -> getter.apply(ctx, str);
	}

	private final TriFunction<CommandContext<CommandSource>, String, String, T, CommandSyntaxException> getter;

	@Override
	public void addInstance(CommandContext<CommandSource> context, ArgMap argMap) throws CommandSyntaxException {
		try {
			T val = getter.apply(context, name, argMap.domain);
			if (val == null)
				throw REQUIRED_ARG.createWithContext(new StringReader(name));
			argMap.present(name, val);
		} catch(IllegalArgumentException ifDoesNotExist) {
			throw REQUIRED_ARG.createWithContext(new StringReader(name));
		}
	}

	public static RequiredArg<IScriptPlayer> player_self(String name) {
		return new RequiredArg<IScriptPlayer>(name, (context, arg, domain) -> {
			ServerPlayerEntity player = context.getSource().asPlayer();
			return CommonScripting.INSTANCE.getScriptPlayerFor(player, domain);
		});
	}

	public static RequiredArg<IScriptPlayer> player_one(String name) {
		return new RequiredArg<IScriptPlayer>(name, (context, arg, domain) -> {
			ServerPlayerEntity player = EntityArgument.getPlayer(context, arg);
			return CommonScripting.INSTANCE.getScriptPlayerFor(player, domain);
		});
	}

	public static RequiredArg<List<IScriptPlayer>> player_multi(String name) {
		return new RequiredArg<List<IScriptPlayer>>(name, (context, arg, domain) -> {
			Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(context, arg);
			return players.parallelStream().map(player -> CommonScripting.INSTANCE.getScriptPlayerFor(player, domain)).collect(Collectors.toList());
		});
	}

	public static RequiredArg<Boolean> ofBoolean(String name) {
		return new RequiredArg<Boolean>(name, BoolArgumentType::getBool);
	}

	public static RequiredArg<Integer> ofInteger(String name) {
		return new RequiredArg<Integer>(name, IntegerArgumentType::getInteger);
	}

	public static RequiredArg<Double> ofDouble(String name) {
		return new RequiredArg<Double>(name, DoubleArgumentType::getDouble);
	}

	public static RequiredArg<String> ofString(String name) {
		return new RequiredArg<String>(name, StringArgumentType::getString);
	}

	public static RequiredArg<CompoundNBT> ofCompound(String name) {
		return new RequiredArg<CompoundNBT>(name, NBTCompoundTagArgument::getNbt);
	}

	public static RequiredArg<String> ofStrings(String name, List<String> collect) {
		return new RequiredArg<String>(name, (context, arg, domain) -> {
			String word = StringArgumentType.getString(context, arg);
			if (!collect.contains(word))
				throw INVALID_ARG.createWithContext(new StringReader(arg));
			return word;
		});
	}

}
