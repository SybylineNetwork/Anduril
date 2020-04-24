package sybyline.anduril.scripting.common;

import net.minecraft.util.ResourceLocation;
import sybyline.anduril.scripting.api.common.IMCResource;

public class MCResource implements IMCResource {

	public final ResourceLocation location;

	MCResource(String domain, String path) {
		this(domain + ":" + path);
	}

	MCResource(String location) {
		this.location = new ResourceLocation(location);
	}

	@Override
	public String domain() {
		return location.getNamespace();
	}

	@Override
	public String path() {
		return location.getPath();
	}

	@Override
	public String string() {
		return location.toString();
	}

}
