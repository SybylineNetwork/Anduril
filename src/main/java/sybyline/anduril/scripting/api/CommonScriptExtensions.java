package sybyline.anduril.scripting.api;

import java.util.*;
import java.util.function.*;
import com.google.common.collect.*;
import sybyline.satiafenris.ene.Script;

public final class CommonScriptExtensions {

	private static final Map<String, Function<Script, Object>> map = Maps.newHashMap();

	public static void registerExtension(String name, Function<Script, Object> extension) {
		map.put(name, extension);
	}

	public static void forEach(BiConsumer<String, Function<Script, Object>> task) {
		map.forEach(task);
	}

}
