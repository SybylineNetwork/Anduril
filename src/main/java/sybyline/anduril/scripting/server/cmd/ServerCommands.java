package sybyline.anduril.scripting.server.cmd;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.server.ServerScripting;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.FileReloadListener;

public class ServerCommands {
	
	public ServerCommands(ServerScripting serverScripting) {
		this.dispatcher = serverScripting.server.getCommandManager().getDispatcher();
		serverScripting.resources.addReloadListener(jsCommands);
	}
	
	public final CommandDispatcher<CommandSource> dispatcher;
	private final Map<String, ScriptCommandWrapper> dynamic_wrappers = Maps.newHashMap();
	
	public final FileReloadListener<ScriptCommandWrapper> jsCommands = new FileReloadListener<ScriptCommandWrapper>(ScriptCommandWrapper.FORMAT, Util.ANDURIL + "/commands", this::reloadJS);
	
	private void reloadJS(Map<ResourceLocation, ScriptCommandWrapper> data, IResourceManager resources, IProfiler profiler) {
		profiler.startSection("sybyline_commands_register");
		dynamic_wrappers.clear();
		data.forEach(this::registerJS);
		profiler.endSection();
	}
	
	private void registerJS(ResourceLocation location, ScriptCommandWrapper command) {
		try {
			if (dynamic_wrappers.get(command.name) != null)
				throw new IllegalArgumentException("A duplicate command for /" + command.name + " exists at " + location);
			command.domain = location.getNamespace();
			CommonScripting.INSTANCE.println_debug("Building script command: " + location);
			LiteralArgumentBuilder<CommandSource> literal = Commands.literal(command.name);
			command.setupWithContext(literal);
			dispatcher.register(literal);
			dynamic_wrappers.put(command.name, command);
		} catch(Exception e) {
			CommonScripting.LOGGER.error("A script command errored during initialization: ", e);
		}
	}
	
}