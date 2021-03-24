package siege.common.rule;

import java.util.Map;
import java.util.function.Supplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import siege.common.siege.Siege;
import siege.common.siege.command.ArgumentEnum;
import siege.common.siege.command.SiegeArgumentType;
import siege.common.siege.command.SiegeCommands;
import siege.common.siege.util.Enums;
import siege.common.siege.util.IdentifiableEnum;

public enum SiegeRule implements ArgumentEnum<SiegeRule>, IdentifiableEnum<SiegeRule> {
	LIVES_PLAYER
		(true, RuleLivesPlayer::new), 
	LIVES_TEAM
		(true, RuleLivesTeam::new), 
	SEQUENTIAL_DOMINATION
		(false, RuleSequentialDomination::new), 
	;

	private SiegeRule(boolean requiresPoints, Supplier<? extends Rule> factory) {
		this.requiresPoints = requiresPoints;
		this.factory = factory;
	}

	private final boolean requiresPoints;
	private final Supplier<? extends Rule> factory;

	@Override
	public <T extends ArgumentBuilder<CommandSource, T>, O extends Enum<O>> T addCommand(T in, O operation) {
		return operation == Op.REMOVE
		? in.then(Commands.literal(identifier())
			.executes(context -> {
				Siege siege = SiegeArgumentType.getSiege(context);
				if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
				if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
				Rule prev = prev(siege);
				if (prev != null) {
					siege.mode.rules.remove(prev);
					SiegeCommands.feedback(context.getSource(), "Removed siege '" + siege.getSiegeName() + "' rule '" + identifier() + "'.");
				} else {
					SiegeCommands.feedback(context.getSource(), "Siege '" + siege.getSiegeName() + "' has no rule '" + identifier() + "'.");
				}
				return Command.SINGLE_SUCCESS;
			})
		)
		: requiresPoints
			? in.then(Commands.literal(identifier())
				.executes(context -> {
					Siege siege = SiegeArgumentType.getSiege(context);
					if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
					if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
					Rule prev = prev(siege);
					Rule rule = factory.get();
					siege.mode.rules.add(rule);
					if (prev != null) {
						siege.mode.rules.remove(prev);
						SiegeCommands.feedback(context.getSource(), "Overwrote siege '" + siege.getSiegeName() + "' rule '" + identifier() + "'.");
					} else {
						SiegeCommands.feedback(context.getSource(), "Set siege '" + siege.getSiegeName() + "' rule '" + identifier() + "'.");
					}
					return Command.SINGLE_SUCCESS;
				})
			)
			: in.then(Commands.literal(identifier())
				.then(Commands.argument("value", IntegerArgumentType.integer(1))
					.executes(context -> {
						Siege siege = SiegeArgumentType.getSiege(context);
						int value = IntegerArgumentType.getInteger(context, "value");
						if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
						if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
						Rule prev = prev(siege);
						Rule rule = factory.get();
						rule.setValue(context.getSource(), value);
						siege.mode.rules.add(rule);
						if (prev != null) {
							siege.mode.rules.remove(prev);
							SiegeCommands.feedback(context.getSource(), "Overwrote siege '" + siege.getSiegeName() + "' rule '" + identifier() + "'.");
						} else {
							SiegeCommands.feedback(context.getSource(), "Set siege '" + siege.getSiegeName() + "' rule '" + identifier() + "'.");
						}
						return Command.SINGLE_SUCCESS;
					})
				)
			);
	}

	private final Rule prev(Siege siege) {
		for (Rule r : siege.mode.rules) {
			if (r.rule() == this) {
				return r;
			}
		}
		return null;
	}

	private static final Map<String, SiegeRule> modes = Enums.map(values());

	public static Rule newRule(String string) {
		SiegeRule m = modes.get(IdentifiableEnum.sanitize(string));
		return m == null ? null : m.factory.get();
	}

	public static enum Op {
		ADD,
		REMOVE,
		;
	}

}
