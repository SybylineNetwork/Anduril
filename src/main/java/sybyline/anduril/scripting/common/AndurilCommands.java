package sybyline.anduril.scripting.common;

import java.util.Collection;

import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import sybyline.anduril.scripting.server.ServerScripting;

public class AndurilCommands {

	public static void setup(FMLServerStartingEvent event) {

		// Reload command
		event.getCommandDispatcher().register(
		Commands.literal("anduril_reload")
		.executes(context -> {
			ServerScripting.INSTANCE.server.reload();
			return 0;
		}));
		
		// Gui command
		event.getCommandDispatcher().register(
		Commands.literal("anduril_gui")
		.then(Commands.argument("players", EntityArgument.players())
		.then(Commands.argument("loc", ResourceLocationArgument.resourceLocation())
		.executes(context -> command_sybyline_gui(
			EntityArgument.getPlayers(context, "players"),
			ResourceLocationArgument.getResourceLocation(context, "loc"),
			true,
			new CompoundNBT()))
		.then(Commands.argument("redisplay", BoolArgumentType.bool())
		.executes(context -> command_sybyline_gui(
			EntityArgument.getPlayers(context, "players"),
			ResourceLocationArgument.getResourceLocation(context, "loc"),
			BoolArgumentType.getBool(context, "redisplay"),
			new CompoundNBT()))
		.then(Commands.argument("data", NBTCompoundTagArgument.nbt())
		.executes(context -> command_sybyline_gui(
			EntityArgument.getPlayers(context, "players"),
			ResourceLocationArgument.getResourceLocation(context, "loc"),
			BoolArgumentType.getBool(context, "redisplay"),
			NBTCompoundTagArgument.getNbt(context, "data"))
		))))));
		
	}

	private static int command_sybyline_gui(Collection<ServerPlayerEntity> players, ResourceLocation loc, boolean redisplay, CompoundNBT data) {
		new S2CSybylineGui().with(loc, redisplay, data).sendTo(CommonScripting.INSTANCE.network, players);
		return 0;
	}

}
