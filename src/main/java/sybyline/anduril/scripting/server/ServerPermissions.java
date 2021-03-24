package sybyline.anduril.scripting.server;

import java.util.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.*;
import net.minecraftforge.server.permission.context.IContext;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.api.server.*;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.common.ScriptServerData;

public class ServerPermissions implements IPermissionHandler {

	public ServerPermissions(MinecraftServer server) {
		this.server = server;
		this.nodes.put(NULL_NODE.node, NULL_NODE);
	}

	private final MinecraftServer server;
	private final PermissionNode NULL_NODE = new PermissionNode("", DefaultPermissionLevel.NONE, "");
	private final Map<String, PermissionNode> nodes = Maps.newHashMap();
	private final Collection<String> nodes_view = Collections.unmodifiableCollection(nodes.keySet());

	@Override
	public void registerNode(String node, DefaultPermissionLevel level, String desc) {
		nodes.computeIfAbsent(node, node_ -> new PermissionNode(node_, level, desc));
	}

	@Override
	public Collection<String> getRegisteredNodes() {
		return nodes_view;
	}

	@Override
	public boolean hasPermission(GameProfile profile, String node, IContext context) {
		PermissionNode perm = nodes.getOrDefault(node, NULL_NODE);
		switch (perm.level) {
		case ALL: return true;
		case OP:
		case NONE:
		default:
		}
		if (server.getPlayerList().canSendCommands(profile)) {
			return true;
		}
		IScriptPlayer player = CommonScripting.INSTANCE.getScriptPlayerFor(profile.getId(), perm.domain);
		if (player.hasPermission(perm)) {
			return true;
		}
		return false;
	}

	@Override
	public String getNodeDescription(String node) {
		return nodes.getOrDefault(node, NULL_NODE).desc;
	}

	// Custom

	public boolean hasPermission(GameProfile profile, IPermission permission) {
		return hasPermissionRecurse(profile, permission);
	}

	private boolean hasPermissionRecurse(GameProfile profile, IPermission permission) {
		Set<String> perms = CommonScripting.INSTANCE.getScriptDataFor(profile.getId()).permissions;
		return perms.contains(permission.key())
			? true
			: permission.children().stream().anyMatch(child -> hasPermissionRecurse(profile, child));
	}

	public class PermissionNode implements IPermission {
		PermissionNode(String node, DefaultPermissionLevel level, String desc) {
			int idx = node.indexOf('.');
			this.domain = idx < 1 ? "minecraft" : node.substring(0, idx);
			this.node = node;
			this.level = level;
			this.desc = desc;
		}
		final String domain;
		final String node;
		private final DefaultPermissionLevel level;
		private final String desc;
		private final Set<IPermission> children = Sets.newHashSet();
		@Override
		public Set<IPermission> children() {
			return children;
		}
		@Override
		public String key() {
			return node;
		}
		@Override
		public String desc() {
			return desc;
		}
	}

	public class Configure implements IPermissionConfigure {
		private Configure(ScriptServerData domain) {
			this.domain = domain;
		}
		private final ScriptServerData domain;
		@Override
		public IPermission new_node(String node, String desc) {
			return get_or_new_node_internal(domain.domain, node, desc);
		}
		@Override
		public IPermission get_node(String node) {
			return nodes.getOrDefault(node, NULL_NODE);
		}
		@Override
		public IPermission get_command(String command) {
			return get_or_new_node_internal("command", command, command);
		}
		private IPermission get_or_new_node_internal(String prefix, String node, String desc) {
			String key = prefix + '.' + node;
			PermissionNode perm = nodes.get(key);
			if (perm == null) {
				nodes.put(key, perm = new PermissionNode(key, DefaultPermissionLevel.OP, desc));
			}
			return perm;
		}
	}

	public Configure newConfigure(ScriptServerData domain) {
		return new Configure(domain);
	}

}
