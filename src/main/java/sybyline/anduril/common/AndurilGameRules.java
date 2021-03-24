package sybyline.anduril.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import sybyline.anduril.util.data.Syncable;
import sybyline.anduril.util.function.TriConsumer;

public final class AndurilGameRules implements Syncable {

	private AndurilGameRules() {}

	public static final AndurilGameRules CLIENT = new AndurilGameRules(), SERVER = new AndurilGameRules();

	public static final Rule<Boolean> DUAL_WIELD = Rule.create("anduril:dual_wield", true);
	public static final Rule<Boolean> LOCALIZED_DAMAGE = Rule.create("anduril:localized_damage", true);
	public static final Rule<Boolean> SPELL_CHUNKLOADING = Rule.create("anduril:spell_chunkloading", true);
	public static final Rule<Integer> SCRIPT_EDIT_TIMEOUT = Rule.create("anduril:script_edit_timeout", 4000, IntegerArgumentType.integer(1000, 1000_000));

	@Override
	public void read(CompoundNBT nbt) {
		clear();
		data.merge(nbt.getCompound("data"));
	}

	@Override
	public void write(CompoundNBT nbt) {
		nbt.merge(data);
	}

	private final CompoundNBT data = new CompoundNBT();
	private final List<Rule<?>> rules = new ArrayList<>();
	private File file = null;

	public List<Rule<?>> rules() {
		return rules;
	}

	public void clear() {
		data.keySet().clear();
	}

	public void load(MinecraftServer server) {
		file = Proxy.PROXY.findSaveSpecificFile("anduril_game_rules.dat");
		if (file == null ? false : file.isFile())
		try {
			CompoundNBT nbt = CompressedStreamTools.read(file);
			read(nbt);
		} catch (IOException e) {
			Anduril.LOGGER.error("Failed to load game rules:", e);
		}
	}

	public void save(MinecraftServer server) {
		if (file == null) {
			Anduril.LOGGER.error("Game rule file null! " + data);
			return;
		}
		CompoundNBT nbt = new CompoundNBT();
		try {
			write(nbt);
			CompressedStreamTools.safeWrite(nbt, file);
		} catch (IOException e) {
			Anduril.LOGGER.error("Failed to save game rules:", e);
			Anduril.LOGGER.error(nbt);
		}
	}

	public <T> T get(Rule<T> rule) {
		if (data.contains(rule.id, rule.nbtType))
			return rule.getter.apply(data, rule.id);
		rule.setter.accept(data, rule.id, rule.defaultValue);
		return rule.defaultValue;
	}

	public <T> void set(Rule<T> rule, T value) {
		rule.setter.accept(data, rule.id, value);
	}

	public static final class Rule<T> {
		public static Rule<Boolean> create(String id, boolean defaultValue) {
			return new Rule<>(defaultValue, id, NBT.TAG_INT, CompoundNBT::getBoolean, CompoundNBT::putBoolean, BoolArgumentType::getBool, BoolArgumentType.bool());
		}
		public static Rule<Integer> create(String id, int defaultValue, IntegerArgumentType arg) {
			return new Rule<>(defaultValue, id, NBT.TAG_INT, CompoundNBT::getInt, CompoundNBT::putInt, IntegerArgumentType::getInteger, arg);
		}
		public static Rule<Long> create(String id, long defaultValue, LongArgumentType arg) {
			return new Rule<>(defaultValue, id, NBT.TAG_LONG, CompoundNBT::getLong, CompoundNBT::putLong, LongArgumentType::getLong, arg);
		}
		public static Rule<Float> create(String id, float defaultValue, FloatArgumentType arg) {
			return new Rule<>(defaultValue, id, NBT.TAG_FLOAT, CompoundNBT::getFloat, CompoundNBT::putFloat, FloatArgumentType::getFloat, arg);
		}
		public static Rule<Double> create(String id, double defaultValue, DoubleArgumentType arg) {
			return new Rule<>(defaultValue, id, NBT.TAG_DOUBLE, CompoundNBT::getDouble, CompoundNBT::putDouble, DoubleArgumentType::getDouble, arg);
		}
		protected Rule(T defaultValue, String id, int nbtType, BiFunction<CompoundNBT, String, T> getter, TriConsumer<CompoundNBT, String, T> setter, BiFunction<CommandContext<CommandSource>, String, T> fromContext, ArgumentType<T> arg) {
			this.defaultValue = defaultValue;
			this.id = id;
			this.nbtType = nbtType;
			this.getter = getter;
			this.setter = setter;
			this.fromContext = fromContext;
			this.arg = arg;
			SERVER.rules.add(this);
		}
		private final T defaultValue;
		private final String id;
		private final int nbtType;
		private final BiFunction<CompoundNBT, String, T> getter;
		private final TriConsumer<CompoundNBT, String, T> setter;
		private final BiFunction<CommandContext<CommandSource>, String, T> fromContext;
		private final ArgumentType<T> arg;
		public LiteralArgumentBuilder<CommandSource> addTo(LiteralArgumentBuilder<CommandSource> command) {
			return command.then(
				Commands.literal(id)
				.executes(AndurilCommands.wrapError(context -> {
					T value = SERVER.get(this);
					context.getSource().sendFeedback(new TranslationTextComponent("anduril.gamerules.info", id, String.valueOf(value)), true);
					return Command.SINGLE_SUCCESS;
				}))
				.then(
					Commands.argument("value", arg)
					.executes(AndurilCommands.wrapError(context -> {
						T value = fromContext.apply(context, "value");
						SERVER.set(this, value);
						AndurilDatas.GAME_RULES.sync(Proxy.PROXY.getCurrentServer().getPlayerList().getPlayers());
						context.getSource().sendFeedback(new TranslationTextComponent("anduril.gamerules.update", id, String.valueOf(value)), true);
						return Command.SINGLE_SUCCESS;
					}))
				)
			);
		}
	}

}
