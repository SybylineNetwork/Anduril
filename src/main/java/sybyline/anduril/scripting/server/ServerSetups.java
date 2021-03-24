package sybyline.anduril.scripting.server;

import java.util.Map;
import com.google.common.collect.Maps;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.common.ScriptWrapperSimple;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.FileReloadListener;

public class ServerSetups {
	
	public ServerSetups(ServerScripting serverScripting) {
		this.serverScripting = serverScripting;
		serverScripting.resources.addReloadListener(jsScripts);
	}
	
	private final ServerScripting serverScripting;
	public final Map<ResourceLocation, ScriptWrapperSimple> wrappers = Maps.newHashMap();
	
	public final FileReloadListener<ScriptWrapperSimple> jsScripts = new FileReloadListener<ScriptWrapperSimple>(ScriptWrapperSimple.FORMAT, Util.ANDURIL + "/setup", this::reloadJS);
	
	private void reloadJS(Map<ResourceLocation, ScriptWrapperSimple> data, IResourceManager resources, IProfiler profiler) {
		profiler.startSection("sybyline_scripts_register");
		wrappers.clear();
		serverScripting.queueSetups(() -> data.forEach(this::registerJS));
		profiler.endSection();
	}
	
	private void registerJS(ResourceLocation location, ScriptWrapperSimple simple) {
		try {
			simple.domain = location.getNamespace();
			simple.setupWithContext(null);
			wrappers.put(location, simple);
		} catch(Exception e) {
			CommonScripting.LOGGER.error("A script ("+location+") errored during initialization: ", e);
		}
	}
	
}