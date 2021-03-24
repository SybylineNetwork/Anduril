package siege.common.mode;

import java.util.Map;
import java.util.function.Supplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import siege.common.siege.Siege;
import siege.common.siege.SiegeTeam;
import siege.common.siege.command.ArgumentEnum;
import siege.common.siege.command.SiegeArgumentType;
import siege.common.siege.command.SiegeCommands;
import siege.common.siege.util.Enums;
import siege.common.siege.util.IdentifiableEnum;
import siege.common.siege.util.Wire;
import siege.common.zone.ZoneControl;
import siege.common.zone.ZoneFlag;

public enum SiegeMode implements ArgumentEnum<SiegeMode>, IdentifiableEnum<SiegeMode> {
	DEATHMATCH
		(false, ModeDefault::new), 
	CTF
		(true, ModeCTF::new) {
		public void addZone(CommandSource sender, Siege siege, String name, BlockPos pos, int radius, Integer order) {
			if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
			if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
			ModeCTF ctf = (ModeCTF)siege.mode;
			SiegeTeam team = siege.getTeam(name);
			if (team == null) throw SiegeCommands.runtime("No such team '" + name + "'!");
			ctf.zones.removeIf(zf -> {
				if (zf.owner == team) {
					ctf.owners.remove(zf.owner);
					return true;
				}
				return false;
			});
			ZoneFlag zf = new ZoneFlag(team, pos.getX(), pos.getY(), pos.getZ(), radius);
			zf.siege = siege;
			ctf.owners.put(zf.owner, zf);
			ctf.zones.add(zf);
			team.setRespawnPoint(pos.getX(), pos.getY(), pos.getZ());
			SiegeCommands.feedback(sender, "Set the home zone and respawn point for team %s.", name);
		}
		public void removeZone(CommandSource sender, Siege siege, String name) {
			if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
			if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
			ModeCTF ctf = (ModeCTF)siege.mode;
			SiegeTeam team = siege.getTeam(name);
			boolean removedAny = ctf.zones.removeIf(zf -> {
				if (zf.owner == team) {
					ctf.owners.remove(zf.owner);
					return true;
				}
				return false;
			});
			if (removedAny)
				throw SiegeCommands.runtime("That team does not have a home zone set!");
			SiegeCommands.feedback(sender, "Removed the home zone for team %s.", name);
		}
	}, 
	DOMINATION
		(true, ModeDomination::new) {
		public void addZone(CommandSource sender, Siege siege, String name, BlockPos position, int radius, Integer order) {
			if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
			if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
			ModeDomination dom = (ModeDomination)siege.mode;
			int ordering = SiegeCommands.requireLazy(order, "Missing ordering!");
			ZoneControl zc = new ZoneControl(name, position.getX(), position.getY(), position.getZ(), radius, ordering);
			zc.siege = siege;
			dom.zones.add(zc);
			SiegeCommands.feedback(sender, "Added the zone %s.", name);
		}
		public void setValueZone(CommandSource sender, Siege siege, String name, int value) {
			if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
			if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
			ModeDomination dom = (ModeDomination)siege.mode;
			Wire did = Wire.create();
			dom.zones.forEach(c -> {
				if (c.name.equals(name)) {
					c.setValue(sender, value);
					did.trip();
				}
			});
			if (!did.checkTripped())
				throw SiegeCommands.runtime("That zone does not exist!");
			SiegeCommands.feedback(sender, "Set zone's value to '" + value + "'.");
		}
		public void removeZone(CommandSource sender, Siege siege, String name) {
			if (siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
			if (siege.isDeleted()) throw SiegeCommands.runtime("Siege is already finished!");
			ModeDomination dom = (ModeDomination)siege.mode;
			boolean removedAny = dom.zones.removeIf(c -> c.name.equals(name));
			if (removedAny)
				throw SiegeCommands.runtime("That zone does not exist!");
			SiegeCommands.feedback(sender, "Removed the zone %s.", name);
		}
	}, 
	;

	private SiegeMode(boolean requiresPoints, Supplier<? extends Mode> factory) {
		this.requiresPoints = requiresPoints;
		this.factory = factory;
	}

	private final boolean requiresPoints;
	private final Supplier<? extends Mode> factory;

	@Override
	public <T extends ArgumentBuilder<CommandSource, T>, O extends Enum<O>> T addCommand(T in, O operation) {
		return requiresPoints
		? in.then(Commands.literal(identifier())
			.then(Commands.argument("points", IntegerArgumentType.integer(1))
				.executes(context -> {
					Siege siege = SiegeArgumentType.getSiege(context);
					int points = IntegerArgumentType.getInteger(context, "points");
					if(siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
					if(siege.isDeleted()) throw SiegeCommands.runtime("Siege is deleted!");
					Mode mode = factory.get();
					mode.pointsNeededToWin = points;
					mode.setSiege(siege);
					SiegeCommands.feedback(context.getSource(), "Set the mode of siege '" + siege.getSiegeName() + "' to '" + identifier() + "'.");
					return Command.SINGLE_SUCCESS;
				})
			)
		)
		: in.then(Commands.literal(identifier())
			.executes(context -> {
				Siege siege = SiegeArgumentType.getSiege(context);
				if(siege.isActive()) throw SiegeCommands.runtime("Siege is already running!");
				if(siege.isDeleted()) throw SiegeCommands.runtime("Siege is deleted!");
				Mode mode = factory.get();
				mode.setSiege(siege);
				SiegeCommands.feedback(context.getSource(), "Set the mode of siege '" + siege.getSiegeName() + "' to '" + identifier() + "'.");
				return Command.SINGLE_SUCCESS;
			})
		);
	}

	public void addZone(CommandSource sender, Siege siege, String name, BlockPos position, int radius, Integer order) {
		throw SiegeCommands.runtime("This siege's mode does not use zones!");
	}

	public void setValueZone(CommandSource sender, Siege siege, String name, int value) {
		throw SiegeCommands.runtime("This mode's zones do not have values to set!");
	}

	public void removeZone(CommandSource sender, Siege siege, String name) {
		throw SiegeCommands.runtime("This siege's mode does not use zones!");
	}

	private static final Map<String, SiegeMode> modes = Enums.map(values());

	public static Mode newMode(String string) {
		SiegeMode m = modes.get(IdentifiableEnum.sanitize(string));
		return m == null ? null : m.factory.get();
	}

}
