package sybyline.anduril.scripting.server;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.*;
import sybyline.anduril.scripting.api.server.IPermission;
import sybyline.anduril.scripting.server.cmd.ServerCommands;
import sybyline.anduril.scripting.server.events.ServerEvents;

public final class ServerScripting {

	private ServerScripting() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static final ServerScripting INSTANCE = new ServerScripting();

	public void setup(FMLServerStartingEvent event) {
		server = event.getServer();
		resources = server.getResourceManager();
		setups = new ServerSetups(this);
		commands = new ServerCommands(this);
		events = new ServerEvents(this);
	}

	public MinecraftServer server;
	public IReloadableResourceManager resources;
	public ServerSetups setups;
	public ServerCommands commands;
	public ServerEvents events;

	public GameProfile getPlayerProfile(UUID uuid) {
		ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(uuid);
		if (player != null) return player.getGameProfile();
		GameProfile ret = server.getPlayerProfileCache().getProfileByUUID(uuid);
		if (ret != null) return ret;
		return new GameProfile(uuid, "");
	}

	public boolean hasPermission(GameProfile profile, IPermission permission) {
		return hasPermission(profile, permission, new Context());
	}

	private boolean hasPermission(GameProfile profile, IPermission permission, IContext context) {
		return PermissionAPI.hasPermission(profile, permission.key(), context)
			? true
			: permission.children().stream().anyMatch(child -> hasPermission(profile, child, context));
	}

	@SubscribeEvent
	void onCommand(CommandEvent event) {
		ParseResults<CommandSource> parse = event.getParseResults();
		try {
			ServerPlayerEntity player = parse.getContext().getSource().asPlayer();
			ImmutableStringReader immut = parse.getReader();
			StringReader reader = new StringReader(immut.getRead()+immut.getRemaining());
			if (reader.canRead()) {
				char peek = reader.peek();
				if (peek == '/') reader.skip();
				String command = reader.readUnquotedString();
				if (!PermissionAPI.hasPermission(player, "command.".concat(command))) {
					event.setException(new CommandException(new StringTextComponent("You do not have permission to use the command '/"+command+"'.")));
				}
			}
		} catch(CommandSyntaxException e) {
			// Pass if not a player
		}
	}

}
