package siege.common.siege.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import static net.minecraft.command.Commands.*;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.ColorArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;
import siege.common.mode.SiegeMode;
import siege.common.rule.SiegeRule;
import siege.common.siege.Siege;
import siege.common.siege.SiegeDatabase;
import siege.common.siege.SiegeTeam;
import siege.common.siege.util.Enums;
import siege.common.siege.util.ThrowingBiFunction;

public interface SiegeCommands {

	String SIEGE = "siege";
	String SIEGE_TEAM = "siege_team";
	String SIEGE_KIT = "kit";

	public static SimpleCommandExceptionType simple(String name) {
		return new SimpleCommandExceptionType(() -> name);
	}

	public static CommandException runtime(String string, Object... objects) {
		return new CommandException(new StringTextComponent(String.format(string, objects)));
	}

	public static void feedback(CommandSource source, String string, Object... objects) {
		source.sendFeedback(new StringTextComponent(String.format(string, objects)), true);
	}

	public static Predicate<CommandSource> opPerms() {
		return source -> source.hasPermissionLevel(2);
	}

	public static <T> T defaulting(CommandContext<CommandSource> context, ThrowingBiFunction<CommandContext<CommandSource>, String, T> func, String name, T def) {
		try {
			return func.apply(context, name);
		} catch(Exception e) {
			return def;
		}
	}

	public static <T> T requireLazy(T value, String message) {
		if (value == null)
			throw SiegeCommands.runtime(message);
		return value;
	}

	public static String joinNiceStringFromCollection(Collection<String> strings) {
		String[] array = strings.stream().toArray(String[]::new);
		int length = array.length;
		switch(length) {
		case 0:
			return "";
		case 1:
			return array[0];
		case 2:
			return array[0] + " and " + array[1];
		default:
			StringBuilder ret = new StringBuilder();
			for (int i = 0; i < length - 2; i++) {
				ret = ret.append(array[i]).append(", ");
			}
			return ret.append(array[length - 2]).append(", and ").append(array[length - 1]).toString();
		}
	}

	public static void register(CommandDispatcher<CommandSource> cmds) {
		// actual
		cmds.register(siege_kit());
		cmds.register(siege_setup());
		cmds.register(siege_play());
		// TODO : Vinyarion's Addon start
		cmds.register(siege_setmode());
		cmds.register(siege_zone());
		cmds.register(siege_list());
		cmds.register(siege_rule());
		cmds.register(siege_teamcolor());
		// Addon end
	}

	public static void registerArgumentTypes() {
		ArgumentTypes.register("anduril:seige", SiegeArgumentType.class, new ArgumentSerializer<>(SiegeArgumentType::siege));
		ArgumentTypes.register("anduril:seige_team", SiegeTeamArgumentType.class, new ArgumentSerializer<>(SiegeTeamArgumentType::siegeTeam));
		ArgumentTypes.register("anduril:seige_kit", SiegeKitArgumentType.class, new ArgumentSerializer<>(SiegeKitArgumentType::siegeKit));
	}

	/* Command setup */

	public static LiteralArgumentBuilder<CommandSource> siege_kit() {
		Command<CommandSource> commandNew = context -> {
			String kitName = StringArgumentType.getString(context, "name");
			if (KitDatabase.kitExists(kitName)) {
				throw SiegeCommands.runtime("A kit named %s already exists!", kitName);
			} else if (!KitDatabase.validKitName(kitName)) {
				throw SiegeCommands.runtime("Invalid kit name %s", kitName);
			}
			ServerPlayerEntity entityplayer = defaulting(context, EntityArgument::getPlayer, "player", null);
			if (entityplayer == null) entityplayer = context.getSource().asPlayer();
			if (entityplayer == null)
				throw SiegeCommands.runtime("Player not found");
			Kit kit = Kit.createNewKit(entityplayer, kitName);
			KitDatabase.addAndSaveKit(kit);
			feedback(context.getSource(), "Created a new kit %s from the inventory of %s", kitName, entityplayer.getScoreboardName());
			return Command.SINGLE_SUCCESS;
		};
		Command<CommandSource> commandEditRecreate = context -> {
			Kit kit = SiegeKitArgumentType.getSiegeKit(context);
			ServerPlayerEntity entityplayer = defaulting(context, EntityArgument::getPlayer, "player", null);
			if (entityplayer == null) entityplayer = context.getSource().asPlayer();
			if (entityplayer == null)
				throw SiegeCommands.runtime("Player not found");
			kit.createFrom(entityplayer);
			feedback(context.getSource(), "Recreated kit %s from the inventory of %s", kit.getKitName(), entityplayer.getScoreboardName());
			return Command.SINGLE_SUCCESS;
		};
		return literal("siege_kit")
		.requires(opPerms())
		.then(literal("new")
			.then(argument("name", StringArgumentType.word())
				.executes(commandNew)
				.then(argument("player", EntityArgument.player())
					.executes(commandNew)
				)
			)
		)
		.then(literal("apply")
			.then(SiegeKitArgumentType.siegeKitArgument()
				.executes(context -> {
					Kit kit = SiegeKitArgumentType.getSiegeKit(context);
					ServerPlayerEntity entityplayer = defaulting(context, EntityArgument::getPlayer, "player", null);
					if (entityplayer == null) entityplayer = context.getSource().asPlayer();
					if (entityplayer == null)
						throw SiegeCommands.runtime("Player not found");
					kit.applyTo(entityplayer);
					feedback(context.getSource(), "Applied kit %s to %s", kit.getKitName(), entityplayer.getScoreboardName());
					return Command.SINGLE_SUCCESS;
				})
			)
		)
		.then(literal("edit")
			.then(SiegeKitArgumentType.siegeKitArgument()
				.then(literal("rename")
					.then(argument("newName", StringArgumentType.word())
						.executes(context -> {
							Kit kit = SiegeKitArgumentType.getSiegeKit(context);
							String oldName = kit.getKitName();
							String newName = StringArgumentType.getString(context, "newName");
							if (!KitDatabase.validKitName(newName))
								throw SiegeCommands.runtime("Invalid kit rename %s", newName);
							if (KitDatabase.kitExists(newName))
								throw SiegeCommands.runtime("A kit named %s already exists!", newName);
							kit.rename(newName);
							feedback(context.getSource(), "Renamed kit %s to %s", oldName, newName);
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(literal("recreate")
					.executes(commandEditRecreate)
					.then(argument("player", EntityArgument.player())
						.executes(commandEditRecreate)
					)
				)
			)
		)
		.then(literal("delete")
			.then(SiegeKitArgumentType.siegeKitArgument()
				.executes(context -> {
					Kit kit = SiegeKitArgumentType.getSiegeKit(context);
					KitDatabase.deleteKit(kit);
					feedback(context.getSource(), "Deleted kit %s", kit.getKitName());
					return Command.SINGLE_SUCCESS;
				})
			)
		);
	}

	public static LiteralArgumentBuilder<CommandSource> siege_setup() {
		BiFunction<String, BiConsumer<Siege, Integer>, ArgumentBuilder<CommandSource, ?>> integerConfig = (name, setter) -> {
			return literal(name)
			.then(argument("value", IntegerArgumentType.integer(1))
				.executes(context -> {
					Siege siege = SiegeArgumentType.getSiege(context);
					int integer = IntegerArgumentType.getInteger(context, "value");
					setter.accept(siege, integer);
					feedback(context.getSource(), "Set siege %s %s to %s", siege.getSiegeName(), name, integer);
					return Command.SINGLE_SUCCESS;
				})
			);
		};
		BiFunction<String, BiConsumer<Siege, Boolean>, ArgumentBuilder<CommandSource, ?>> boolConfig = (name, setter) -> {
			return literal(name)
			.then(argument("value", BoolArgumentType.bool())
				.executes(context -> {
					Siege siege = SiegeArgumentType.getSiege(context);
					boolean bool = BoolArgumentType.getBool(context, "value");
					setter.accept(siege, bool);
					feedback(context.getSource(), (
						bool ? "Enabled %s in siege %s" : "Disabled %s in siege %s"
					), name, siege.getSiegeName());
					return Command.SINGLE_SUCCESS;
				})
			);
		};
		return literal("siege_setup")
		.requires(opPerms())
		.then(literal("new")
			.then(argument("name", StringArgumentType.word())
				.executes(context -> {
					String name = StringArgumentType.getString(context, "name");
					if (!SiegeDatabase.validSiegeName(name)) {
						throw SiegeCommands.runtime("Invalid siege name %s", name);
					} else if (SiegeDatabase.siegeExists(name)) {
						throw SiegeCommands.runtime("A siege named %s already exists!", name);
					}
					Siege siege = new Siege(name);
					SiegeDatabase.addAndSaveSiege(siege);
					feedback(context.getSource(), "Created a new siege %s", name);
					return Command.SINGLE_SUCCESS;
				})
			)
		)
		.then(literal("edit")
			.then(SiegeArgumentType.siegeArgument()
				.then(literal("rename")
					.then(argument("newName", StringArgumentType.word())
						.executes(context -> {
							Siege siege = SiegeArgumentType.getSiege(context);
							if (siege.isActive())
								throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
							String newName = StringArgumentType.getString(context, "newName");
							if (!SiegeDatabase.validSiegeName(newName))
								throw SiegeCommands.runtime("Invalid siege rename %s", newName);
							if (SiegeDatabase.siegeExists(newName))
								throw SiegeCommands.runtime("A siege named %s already exists!", newName);
							siege.rename(newName);
							feedback(context.getSource(), "Renamed siege %s to %s", siege.getSiegeName(), newName);
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(literal("setcoords")
					.then(argument("pos", Vec2Argument.vec2())
						.then(argument("radius", FloatArgumentType.floatArg(10))
							.executes(context -> {
								Siege siege = SiegeArgumentType.getSiege(context);
								if (siege.isActive())
									throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
								Vec2f pos = Vec2Argument.getVec2f(context, "pos");
								float radius = FloatArgumentType.getFloat(context, "radius");
								DimensionType dim = context.getSource().getWorld().getDimension().getType();
								siege.setCoords(dim, MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(radius));
								feedback(context.getSource(), "Set location of siege %s to [x=%s, z=%s, r=%s] in dim-%s", siege.getSiegeName(), pos.x, pos.y, radius, dim);
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
				.then(literal("teams")
					.then(literal("new")
						.then(argument("teamName", StringArgumentType.word())
							.executes(context -> {
								Siege siege = SiegeArgumentType.getSiege(context);
								if (siege.isActive())
									throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
								String teamName = StringArgumentType.getString(context, "teamName");
								if (!SiegeDatabase.validTeamName(teamName))
									throw SiegeCommands.runtime("Invalid team name %s", teamName);
								if (siege.getTeam(teamName) != null)
									throw SiegeCommands.runtime("Siege %s already has a team named %s!", siege.getSiegeName(), teamName);
								siege.createNewTeam(teamName);
								feedback(context.getSource(), "Created new team %s for siege %s", teamName, siege.getSiegeName());
								return Command.SINGLE_SUCCESS;
							})
						)
					)
					.then(literal("edit")
						.then(SiegeTeamArgumentType.siegeTeamArgument()
							.then(literal("rename")
								.then(argument("newName", StringArgumentType.word())
									.executes(context -> {
										Siege siege = SiegeArgumentType.getSiege(context);
										if (siege.isActive())
											throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
										String teamRename = StringArgumentType.getString(context, "newName");
										SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
										String teamName = team.getTeamName();
										if (!SiegeDatabase.validTeamName(teamRename))
											throw SiegeCommands.runtime("Invalid team rename %s", teamRename);
										if (siege.getTeam(teamRename) != null)
											throw SiegeCommands.runtime("A team named %s already exists in siege %s!", teamRename, siege.getSiegeName());
										team.rename(teamRename);
										feedback(context.getSource(), "Renamed team %s to %s in siege %s", teamName, teamRename, siege.getSiegeName());
										return Command.SINGLE_SUCCESS;
									})
								)
							)
							.then(literal("kit-add")
								.then(SiegeKitArgumentType.siegeKitArgument()
									.executes(context -> {
										Siege siege = SiegeArgumentType.getSiege(context);
										SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
										String kitName = SiegeKitArgumentType.getSiegeKitName(context);
										if (!KitDatabase.kitExists(kitName))
											throw SiegeCommands.runtime("Kit %s does not exist", kitName);
										Kit kit = KitDatabase.getKit(kitName);
										if (team.containsKit(kit))
											throw SiegeCommands.runtime("Siege %s team %s already includes kit %s!", siege.getSiegeName(), team.getTeamName(), kitName);
										team.addKit(kit);
										feedback(context.getSource(), "Added kit %s for team %s in siege %s", kitName, team.getTeamName(), siege.getSiegeName());
										return Command.SINGLE_SUCCESS;
									})
								)
							)
							.then(literal("kit-remove")
								.then(SiegeKitArgumentType.siegeKitArgument()
									.executes(context -> {
										Siege siege = SiegeArgumentType.getSiege(context);
										SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
										String kitName = SiegeKitArgumentType.getSiegeKitName(context);
										if (!KitDatabase.kitExists(kitName))
											throw SiegeCommands.runtime("Kit %s does not exist", kitName);
										Kit kit = KitDatabase.getKit(kitName);
										if (!team.containsKit(kit))
											throw SiegeCommands.runtime("Siege %s team %s does not include kit %s!", siege.getSiegeName(), team.getTeamName(), kitName);
										team.removeKit(kit);
										feedback(context.getSource(), "Removed kit %s from team %s in siege %s", kitName, team.getTeamName(), siege.getSiegeName());
										return Command.SINGLE_SUCCESS;
									})
								)
							)
							.then(literal("kit-limit")
								.then(SiegeKitArgumentType.siegeKitArgument()
									.then(argument("limit", IntegerArgumentType.integer(1))
										.executes(context -> {
											Siege siege = SiegeArgumentType.getSiege(context);
											SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
											String kitName = SiegeKitArgumentType.getSiegeKitName(context);
											int limit = IntegerArgumentType.getInteger(context, "limit");
											if (!KitDatabase.kitExists(kitName))
												throw SiegeCommands.runtime("Kit %s does not exist", kitName);
											Kit kit = KitDatabase.getKit(kitName);
											if (!team.containsKit(kit))
												throw SiegeCommands.runtime("Siege %s team %s does not include kit %s!", siege.getSiegeName(), team.getTeamName(), kitName);
											team.limitKit(kit, limit);
											feedback(context.getSource(), "Limited kit %s to %s players for team %s in siege %s", kitName, String.valueOf(limit), team.getTeamName(), siege.getSiegeName());
											return Command.SINGLE_SUCCESS;
										})
									)
								)
							)
							.then(literal("kit-unlimit")
								.then(SiegeKitArgumentType.siegeKitArgument()
									.executes(context -> {
										Siege siege = SiegeArgumentType.getSiege(context);
										SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
										String kitName = SiegeKitArgumentType.getSiegeKitName(context);
										if (!KitDatabase.kitExists(kitName))
											throw SiegeCommands.runtime("Kit %s does not exist", kitName);
										Kit kit = KitDatabase.getKit(kitName);
										if (!team.containsKit(kit))
											throw SiegeCommands.runtime("Siege %s team %s does not include kit %s!", siege.getSiegeName(), team.getTeamName(), kitName);
										if (!team.isKitLimited(kit))
											throw SiegeCommands.runtime("Kit %s is not limited for team %s in siege %s", kitName, team.getTeamName(), siege.getSiegeName());
										team.unlimitKit(kit);
										feedback(context.getSource(), "Unlimited kit %s for team %s in siege %s", kitName, team.getTeamName(), siege.getSiegeName());
										return Command.SINGLE_SUCCESS;
									})
								)
							)
							.then(literal("setspawn")
								.then(argument("position", BlockPosArgument.blockPos())
									.executes(context -> {
										Siege siege = SiegeArgumentType.getSiege(context);
										SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
										BlockPos pos = BlockPosArgument.getBlockPos(context, "position");
										team.setRespawnPoint(pos.getX(), pos.getY(), pos.getZ());
										feedback(context.getSource(), "Set siege %s team %s respawn point to [%s, %s, %s]", siege.getSiegeName(), team.getTeamName(), pos.getX(), pos.getY(), pos.getZ());
										return Command.SINGLE_SUCCESS;
									})
								)
							)
						)
					)
					.then(literal("remove")
						.then(SiegeTeamArgumentType.siegeTeamArgument()
							.executes(context -> {
								Siege siege = SiegeArgumentType.getSiege(context);
								if (siege.isActive())
									throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
								SiegeTeam team = SiegeTeamArgumentType.getSiegeTeam(context);
								String teamName = team.getTeamName();
								if (!siege.removeTeam(teamName))
									throw SiegeCommands.runtime("Could not remove team %s from siege %s", teamName, siege.getSiegeName());
								feedback(context.getSource(), "Removed team %s from siege %s", teamName, siege.getSiegeName());
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
				.then(integerConfig.apply("max-team-diff", Siege::setMaxTeamDifference))
				.then(integerConfig.apply("respawn-immunity", Siege::setRespawnImmunity))
				.then(boolConfig.apply("friendly-fire", Siege::setFriendlyFire))
				.then(boolConfig.apply("mob-spawning", Siege::setMobSpawning))
				.then(boolConfig.apply("terrain-protect", Siege::setTerrainProtect))
				.then(boolConfig.apply("terrain-protect-inactive", Siege::setTerrainProtectInactive))
				.then(boolConfig.apply("dispel", Siege::setDispelOnEnd))
			)
		)
		.then(literal("start")
			.then(SiegeArgumentType.siegeArgument()
				.then(argument("seconds", IntegerArgumentType.integer(0))
					.executes(context -> {
						Siege siege = SiegeArgumentType.getSiege(context);
						if (siege.isActive())
							throw SiegeCommands.runtime("Siege %s is already active!", siege.getSiegeName());
						if (!siege.canBeStarted())
							throw siege.mode.generateException();
						int seconds = IntegerArgumentType.getInteger(context, "seconds");
						int durationTicks = seconds * 20;
						siege.startSiege(durationTicks);
				        String timeDisplay = Siege.ticksToTimeString(durationTicks);
						feedback(context.getSource(), "Started a new siege %s lasting for %s", siege.getSiegeName(), timeDisplay);
						return Command.SINGLE_SUCCESS;
					})
				)
			)
		)
		.then(literal("active")
			.then(SiegeArgumentType.siegeArgument()
				.then(literal("extend")
					.then(argument("seconds", IntegerArgumentType.integer(0))
						.executes(context -> {
							Siege siege = SiegeArgumentType.getSiege(context);
							if (!siege.isActive())
								throw SiegeCommands.runtime("Siege %s is not active!", siege.getSiegeName());
							int seconds = IntegerArgumentType.getInteger(context, "seconds");
							int durationTicks = seconds * 20;
							siege.extendSiege(durationTicks);
							String timeDisplay = Siege.ticksToTimeString(durationTicks);
					        int fullDuration = siege.getTicksRemaining();
					        String fullTimeDisplay = Siege.ticksToTimeString(fullDuration);
					        feedback(context.getSource(), "Extended siege %s for %s - now lasting for %s", siege.getSiegeName(), timeDisplay, fullTimeDisplay);
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(literal("end")
					.executes(context -> {
						Siege siege = SiegeArgumentType.getSiege(context);
						if (!siege.isActive())
							throw SiegeCommands.runtime("Siege %s is not active!", siege.getSiegeName());
						siege.endSiege();
						feedback(context.getSource(), "Ended siege %s", siege.getSiegeName());
						return Command.SINGLE_SUCCESS;
					})
				)
			)
		)
		.then(literal("delete")
			.then(SiegeArgumentType.siegeArgument()
				.executes(context -> {
					Siege siege = SiegeArgumentType.getSiege(context);
					if (siege != null) {
						SiegeDatabase.deleteSiege(siege);
						feedback(context.getSource(), "Deleted siege %s", siege.getSiegeName());
					} else {
						throw SiegeCommands.runtime("No siege for name %s", context.getArgument(SiegeCommands.SIEGE, String.class));
					}
					return Command.SINGLE_SUCCESS;
				})
			)
		);
	}

	public static LiteralArgumentBuilder<CommandSource> siege_play() {
		Command<CommandSource> commandJoin = context -> {
			ServerPlayerEntity operator = context.getSource().asPlayer();
			if (SiegeDatabase.getActiveSiegeForPlayer(operator) != null)
				throw SiegeCommands.runtime("You are already taking part in a siege!");
			Siege siege = SiegeArgumentType.getSiege(context);
			if (siege == null ? true : !siege.isActive())
				throw SiegeCommands.runtime("Cannot join siege %s: no such active siege exists!", "?");
			if (siege.isPlayerInDimension(operator))
				throw SiegeCommands.runtime("Cannot join siege %s: you are in the wrong dimension!");
			SiegeTeam team = SiegeTeamArgumentType.getSiegeTeamOrNull(context);
			if (team == null)
				throw SiegeCommands.runtime("Cannot join siege %s on team %s: no such team exists!", siege.getSiegeName(), "?");
			if (!team.canPlayerJoin(operator))
				throw SiegeCommands.runtime("Cannot join siege %s on team %s: too many players! Try another team", siege.getSiegeName(), team.getTeamName());
			String kitName = defaulting(context, StringArgumentType::getString, "kit", null);
			Kit kit = KitDatabase.getKit(kitName);
			if (kitName != null && kit == null)
				throw SiegeCommands.runtime("Kit %s does not exist!", kitName);
			if (kit != null && team.containsKit(kit)) {
				if (team.isKitAvailable(kit)) {
				} else {
					int limit = team.getKitLimit(kit);
					throw SiegeCommands.runtime("Kit %s is limited to %s players in team %s! Try another kit", kit.getKitName(), String.valueOf(limit), team.getTeamName());
				}
			}
			if (siege.joinPlayer(operator, team, kit)) {
				if (kit == null) {
					feedback(context.getSource(), "Joined siege %s on team %s", siege.getSiegeName(), team.getTeamName());
				} else {
					feedback(context.getSource(), "Joined siege %s on team %s as kit %s", siege.getSiegeName(), team.getTeamName(), kit.getKitName());
				}
			}
			return Command.SINGLE_SUCCESS;
		};
		Command<CommandSource> commandTeam = context -> {
			ServerPlayerEntity operator = context.getSource().asPlayer();
			Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
			if (siege == null ? true : siege.isActive())
				throw SiegeCommands.runtime("You are not currently taking part in a siege!");
			String teamName = defaulting(context, StringArgumentType::getString, "team", "?");
			SiegeTeam team = SiegeTeamArgumentType.getSiegeTeamOrNull(context);
			if (team == null)
				throw SiegeCommands.runtime("Cannot switch to team %s: no such team exists!", teamName);
			if (!team.canPlayerJoin(operator))
				throw SiegeCommands.runtime("Cannot switch to team %s: too many players!", teamName);
			String kitName = defaulting(context, StringArgumentType::getString, "kit", null);
			Kit kit;
			if (kitName == null) {
				kit = null;
			} else if (KitDatabase.isRandomKitID(kitName)) {
				kit = null;
			} else {
				Kit kitArg = KitDatabase.getKit(kitName);
				if (kitArg != null && team.containsKit(kitArg)) {
					if (team.isKitAvailable(kitArg) || kitArg.getKitID().equals(siege.getPlayerData(operator).getChosenKit())) {
						kit = kitArg;
					} else {
						int limit = team.getKitLimit(kitArg);
						throw SiegeCommands.runtime("Kit %s is limited to %s players in team %s! Try another kit", kitArg.getKitName(), String.valueOf(limit), teamName);
					}
				} else {
					kit = null;
				}
			}
			
			siege.getPlayerData(operator).setNextTeam(teamName);
			siege.getPlayerData(operator).setChosenKit(kit);

			if (kit == null) {
				feedback(context.getSource(), "Switching to team %s after death", teamName);
			} else {
				feedback(context.getSource(), "Switching to team %s with kit %s after death", teamName, kit.getKitName());
			}
			return Command.SINGLE_SUCCESS;
		};
		Command<CommandSource> commandKit = context -> {
			ServerPlayerEntity operator = context.getSource().asPlayer();
			Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
			if (siege == null ? true : !siege.isActive())
				throw SiegeCommands.runtime("You are not currently taking part in a siege!");
			SiegeTeam team = siege.getPlayerTeam(operator);
			String teamName = team.getTeamName();
			String kitName = defaulting(context, StringArgumentType::getString, "kit", null);
			
			if (KitDatabase.isRandomKitID(kitName)) {
				siege.getPlayerData(operator).setRandomChosenKit();
				feedback(context.getSource(), "Switching to random kit selection after death", kitName);
				return Command.SINGLE_SUCCESS;
			} else {
				Kit kit = KitDatabase.getKit(kitName);
				if (kit == null ? true : !team.containsKit(kit))
					throw SiegeCommands.runtime("Cannot switch to kit %s: no such kit exists on team %s!", kitName, team.color + teamName);
				if (team.isKitAvailable(kit) || kit.getKitID().equals(siege.getPlayerData(operator).getChosenKit())) {
					siege.getPlayerData(operator).setChosenKit(kit);
					feedback(context.getSource(), "Switching to kit %s after death", kitName);
					return Command.SINGLE_SUCCESS;
				} else {
					int limit = team.getKitLimit(kit);
					throw SiegeCommands.runtime("Kit %s is limited to %s players in team %s! Try another kit", kitName, String.valueOf(limit), team.color + teamName);
				}
			}
		};
		return literal("siege_play")
		.then(literal("join")
			.then(SiegeArgumentType.siegeArgument()
				.executes(commandJoin)
				.then(SiegeTeamArgumentType.siegeTeamArgument()
					.executes(commandJoin)
					.then(SiegeKitArgumentType.siegeKitArgument()
						.executes(commandJoin)
					)
				)
			)
		)
		.then(literal("team")
			.then(SiegeTeamArgumentType.siegeTeamArgument()
				.executes(commandTeam)
				.then(SiegeKitArgumentType.siegeKitArgument()
					.executes(commandTeam)
				)
			)
		)
		.then(literal("kit")
			.executes(commandKit)
			.then(SiegeKitArgumentType.siegeKitArgument()
				.executes(commandKit)
			)
		)
		.then(literal("leave")
			.executes(context -> {
				ServerPlayerEntity operator = context.getSource().asPlayer();
				Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
				if (siege == null ? true : !siege.isActive())
					throw SiegeCommands.runtime("You are not currently taking part in a siege!");
				siege.leavePlayer(operator, true);
				feedback(context.getSource(), "Left siege %s", siege.getSiegeName());
				return Command.SINGLE_SUCCESS;
			})
		);
	}

	// Addon start

	public static LiteralArgumentBuilder<CommandSource> siege_setmode() {
		return literal("siege_setmode")
		.requires(opPerms())
		.then(ArgumentEnum.iterate(SiegeMode.class, Enums.NULL, SiegeArgumentType.siegeArgument()));
	}

	public static LiteralArgumentBuilder<CommandSource> siege_zone() {
		Command<CommandSource> commandAdd = context -> {
			Siege siege = SiegeArgumentType.getSiege(context);
			siege.mode.mode().addZone(
				context.getSource(), 
				siege, 
				StringArgumentType.getString(context, "zone"), 
				BlockPosArgument.getBlockPos(context, "position"), 
				IntegerArgumentType.getInteger(context, "radius"), 
				SiegeCommands.defaulting(context, IntegerArgumentType::getInteger, "order", null)
			);
			return Command.SINGLE_SUCCESS;
		};
		return literal("siege_zone")
		.requires(opPerms())
		.then(SiegeArgumentType.siegeArgument()
			.then(argument("zone", StringArgumentType.word())
				.then(literal("add")
					.then(argument("position", BlockPosArgument.blockPos())
						.then(argument("radius", IntegerArgumentType.integer(0))
							.executes(commandAdd)
							.then(argument("order", IntegerArgumentType.integer())
								.executes(commandAdd)
							)
						)
					)
				)
				.then(literal("set-value")
					.then(argument("value", IntegerArgumentType.integer())
						.executes(context -> {
							CommandSource source = context.getSource();
							Siege siege = SiegeArgumentType.getSiege(context);
							String zoneName = StringArgumentType.getString(context, "zone");
							Integer value = IntegerArgumentType.getInteger(context, "value");
							siege.mode.mode().setValueZone(source, siege, zoneName, value);
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(literal("remove")
					.executes(context -> {
						CommandSource source = context.getSource();
						Siege siege = SiegeArgumentType.getSiege(context);
						String zoneName = StringArgumentType.getString(context, "zone");
						siege.mode.mode().removeZone(source, siege, zoneName);
						return Command.SINGLE_SUCCESS;
					})
				)
			)
		);
	}

	public static LiteralArgumentBuilder<CommandSource> siege_list() {
		return literal("siege_list")
		.requires(opPerms())
		.then(literal("active")
			.executes(context -> {
				List<String> allOfType = SiegeDatabase.getAllSieges()
					.stream()
					.filter(Siege::isActive)
					.map(Siege::getSiegeName)
					.collect(Collectors.toList());
				feedback(context.getSource(), (allOfType.isEmpty()
					? "No active sieges."
					: allOfType.size() + " active sieges: " + joinNiceStringFromCollection(allOfType)
				));
				return Command.SINGLE_SUCCESS;
			})
		)
		.then(literal("inactive")
			.executes(context -> {
				List<String> allOfType = SiegeDatabase.getAllSieges()
					.stream()
					.filter(siege -> !siege.isActive())
					.map(Siege::getSiegeName)
					.collect(Collectors.toList());
				feedback(context.getSource(), (allOfType.isEmpty()
					? "No inactive sieges."
					: allOfType.size() + " inactive sieges: " + joinNiceStringFromCollection(allOfType)
				));
				return Command.SINGLE_SUCCESS;
			})
		)
		.then(literal("deleted")
			.executes(context -> {
				List<String> allOfType = SiegeDatabase.getAllSieges()
					.stream()
					.filter(Siege::isDeleted)
					.map(Siege::getSiegeName)
					.collect(Collectors.toList());
				feedback(context.getSource(), (allOfType.isEmpty()
					? "No deleted sieges."
					: allOfType.size() + " deleted sieges: " + joinNiceStringFromCollection(allOfType)
				));
				return Command.SINGLE_SUCCESS;
			})
		);
	}

	public static LiteralArgumentBuilder<CommandSource> siege_rule() {
		return literal("siege_rule")
		.requires(opPerms())
		.then(SiegeArgumentType.siegeArgument()
			.then(ArgumentEnum.iterate(SiegeRule.class, SiegeRule.Op.ADD, literal("add")))
			.then(ArgumentEnum.iterate(SiegeRule.class, SiegeRule.Op.REMOVE, literal("remove")))
		);
	}

	public static LiteralArgumentBuilder<CommandSource> siege_teamcolor() {
		return literal("siege_teamcolor")
		.requires(opPerms())
		.then(SiegeArgumentType.siegeArgument()
			.then(SiegeTeamArgumentType.siegeTeamArgument()
				.then(argument("color", ColorArgument.color())
					.executes(context -> {
						SiegeTeam siegeTeam = SiegeTeamArgumentType.getSiegeTeam(context);
						TextFormatting color = ColorArgument.getColor(context, "color");
						siegeTeam.color = color;
						feedback(context.getSource(), "Set team "+siegeTeam.getTeamName()+"'s color to "+color.getFriendlyName());
						return Command.SINGLE_SUCCESS;
					})
				)
			)
		);
	}

}
