package sybyline.anduril.scripting.server.cmd;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.command.arguments.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import sybyline.anduril.scripting.api.server.IScriptCommand;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.satiafenris.ene.Convert;

public class ScriptCommand implements IScriptCommand {

	public ScriptCommand(ScriptCommandWrapper parent) {
		this.parent = parent;
	}

	private final ScriptCommandWrapper parent;
	private final List<IDefaultingArg<?>> args = Lists.newArrayList();
	private BiConsumer<Object, Object> implementation;
	LiteralArgumentBuilder<CommandSource> literal;
	List<Pair<String, ArgumentType<?>>> vanilla_arguments = Lists.newArrayList();
	private boolean hasSenderArg = false;

	private void argument(String name, ArgumentType<?> argument) {
		vanilla_arguments.add(0, Pair.of(name, argument));
	}

	private void flagSenderArg() {
		if (hasSenderArg)
			throw new IllegalArgumentException("Already has command sender argument!");
		hasSenderArg = true;
	}

	private void cascade() {
		ArgumentBuilder<CommandSource, ?> builder = null;
		while (!vanilla_arguments.isEmpty()) {
			Pair<String, ArgumentType<?>> pair = vanilla_arguments.remove(0);
			ArgumentBuilder<CommandSource, ?> arg = Commands.argument(pair.getLeft(), pair.getRight()).executes(this::execute);
			builder = builder == null ? arg : arg.then(builder);
		}
		LiteralArgumentBuilder<CommandSource> to_register = literal.executes(this::execute);
		if (builder != null)
			to_register = to_register.then(builder);
		literal = to_register;
	}

	private int execute(CommandContext<CommandSource> context) {
		ArgMap argMap = new ArgMap(parent);
		try {
			for (IDefaultingArg<?> arg : args) arg.addInstance(context, argMap);
			implementation.accept(argMap.args(), argMap.defs());
			return 0;
		} catch(CommandSyntaxException e) {
			context.getSource().sendErrorMessage(new TranslationTextComponent(e.getRawMessage().getString()));
			return 42;
		} catch(Exception e) {
			CommonScripting.LOGGER.error("A script command has failed: ", e);
			return 69;
		}
	}

	@Override
	public void withRaw(UnaryOperator<LiteralArgumentBuilder<CommandSource>> rawCommand) {
		literal = rawCommand.apply(literal);
	}

	// Senders

	@Override
	public IScriptCommand arg_player_self(String name) {
		this.flagSenderArg();
		args.add(RequiredArg.player_self(name));
		CommonScripting.INSTANCE.println_debug("  Sender: self");
		return this;
	}

	@Override
	public IScriptCommand arg_player_one(String name) {
		this.flagSenderArg();
		argument(name, EntityArgument.player());
		args.add(RequiredArg.player_one(name));
		CommonScripting.INSTANCE.println_debug("  Sender: player");
		return this;
	}

	@Override
	public IScriptCommand arg_player_multi(String name) {
		this.flagSenderArg();
		argument(name, EntityArgument.players());
		args.add(RequiredArg.player_multi(name));
		CommonScripting.INSTANCE.println_debug("  Sender: players");
		return this;
	}

	@Override
	public IScriptCommand arg_server() {
		this.flagSenderArg();
		CommonScripting.INSTANCE.println_debug("  Sender: server");
		return this;
	}

	// Real things

	@Override
	public IScriptCommand arg_boolean(String name, Boolean def) {
		argument(name, BoolArgumentType.bool());
		args.add(IDefaultingArg.ofBoolean(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: boolean");
		return this;
	}

	@Override
	public IScriptCommand arg_integer(String name, Integer def, int min, int max) {
		argument(name, IntegerArgumentType.integer(min, max));
		args.add(IDefaultingArg.ofInteger(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: integer");
		return this;
	}

	@Override
	public IScriptCommand arg_double(String name, Double def, double min, double max) {
		argument(name, DoubleArgumentType.doubleArg(min, max));
		args.add(IDefaultingArg.ofDouble(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: double");
		return this;
	}

	@Override
	public IScriptCommand arg_string_one(String name, String def) {
		argument(name, StringArgumentType.word());
		args.add(IDefaultingArg.ofString(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: string_one");
		return this;
	}

	@Override
	public IScriptCommand arg_string_oneof(String name, Object possibilities) {
		Object optimistic_list = Convert.java_of(possibilities);
		if (!(optimistic_list instanceof List))
			throw new IllegalArgumentException("Requires an array of strings!");
		List<?> list = (List<?>)optimistic_list;
		if (list.isEmpty())
			throw new IllegalArgumentException("Requires a *not empty* array of strings!");
		argument(name, StringArgumentType.word());
		args.add(RequiredArg.ofStrings(name, list.stream().map(String::valueOf).collect(Collectors.toList())));
		CommonScripting.INSTANCE.println_debug("  Arg: string_oneof");
		return this;
	}

	@Override
	public IScriptCommand arg_string_quotable(String name, String def) {
		argument(name, StringArgumentType.string());
		args.add(IDefaultingArg.ofString(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: string_quotable");
		return this;
	}

	@Override
	public IScriptCommand arg_string_rest(String name, String def) {
		argument(name, StringArgumentType.greedyString());
		args.add(IDefaultingArg.ofString(name, def));
		CommonScripting.INSTANCE.println_debug("  Arg: string_rest");
		return this;
	}

	@Override
	public IScriptCommand arg_nbt_compound(String name, Object def) {
		Object optimistic_compound = Convert.nbt_of(def);
		if (!(optimistic_compound instanceof CompoundNBT))
			throw new IllegalArgumentException("Requires an NBT-convertable object!");
		CompoundNBT nbt = (CompoundNBT)optimistic_compound;
		argument(name, NBTCompoundTagArgument.nbt());
		args.add(IDefaultingArg.ofCompound(name, nbt));
		CommonScripting.INSTANCE.println_debug("  Arg: nbt_compound");
		return this;
	}

	@Override
	public void runs(BiConsumer<Object, Object> command) {
		if (!hasSenderArg)
			throw new IllegalArgumentException("Missing command sender argument!");
		if (command == null)
			throw new IllegalArgumentException("Null command function!");
		if (implementation != null)
			throw new IllegalArgumentException("Command function already exists!");
		CommonScripting.INSTANCE.println_debug(" Runs.");
		this.implementation = command;
		this.cascade();
	}

}
