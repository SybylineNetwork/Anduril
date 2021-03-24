package sybyline.anduril.scripting.server.cmd;

import java.util.HashMap;
import java.util.Map;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import jdk.internal.dynalink.beans.StaticClass;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.*;
import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.scripting.common.ScriptWrapper;
import sybyline.anduril.util.data.IFormat;
import sybyline.satiafenris.ene.JSObjectBase;

@SuppressWarnings("restriction")
public class ScriptCommandWrapper extends ScriptWrapper<LiteralArgumentBuilder<CommandSource>> {

	public static final Object args = new JSObjectBase() {
		private final Map<String, Object> argumentClasses = new HashMap<>(); {
			put("bools", BoolArgumentType.class);
			put("ints", IntegerArgumentType.class);
			put("floats", FloatArgumentType.class);
			put("longs", LongArgumentType.class);
			put("doubles", DoubleArgumentType.class);
			put("strings", StringArgumentType.class);
			
			put("blockPos", BlockPosArgument.class);
			put("blockPredicate", BlockPredicateArgument.class);
			put("blockState", BlockStateArgument.class);
			put("color", ColorArgument.class);
			put("columnPos", ColumnPosArgument.class);
			put("component", ComponentArgument.class);
			put("dimension", DimensionArgument.class);
			put("enchantment", EnchantmentArgument.class);
			put("entityAnchor", EntityAnchorArgument.class);
			put("entity", EntityArgument.class);
			put("entitySummon", EntitySummonArgument.class);
			put("func", FunctionArgument.class);
			put("gameProfile", GameProfileArgument.class);
			put("item", ItemArgument.class);
			put("itemPredicate", ItemPredicateArgument.class);
			put("localLocation", LocalLocationArgument.class);
			put("message", MessageArgument.class);
			put("nbtCompoundTag", NBTCompoundTagArgument.class);
			put("nbtPath", NBTPathArgument.class);
			put("nbtTag", NBTTagArgument.class);
			put("objective", ObjectiveArgument.class);
			put("objectiveCriteria", ObjectiveCriteriaArgument.class);
			put("operation", OperationArgument.class);
			put("particle", ParticleArgument.class);
			put("potion", PotionArgument.class);
			put("resourceLocation", ResourceLocationArgument.class);
			put("rotation", RotationArgument.class);
			put("scoreboardSlotARg", ScoreboardSlotArgument.class);
			put("scoreHolder", ScoreHolderArgument.class);
			put("slot", SlotArgument.class);
			put("swizzle", SwizzleArgument.class);
			put("team", TeamArgument.class);
			put("time", TimeArgument.class);
			put("vec2", Vec2Argument.class);
			put("vec3", Vec3Argument.class);
		}
		private void put(String string, Class<?> clazz) {
			argumentClasses.put(string, StaticClass.forClass(clazz));
		}
		@Override
		public Object getMember(String var) {
			return argumentClasses.get(var);
		}
	};

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
		this.script.bind("SimpleCommand", this.cmd);
		this.script.bind("Commands", StaticClass.forClass(Commands.class));
		this.script.bind("Args", args);
	}

	public static final IFormat<ScriptCommandWrapper> FORMAT = ScriptWrapper.formatOf(ScriptCommandWrapper::new);

	@Override
	protected LogicalSide side() {
		return LogicalSide.SERVER;
	}

}
