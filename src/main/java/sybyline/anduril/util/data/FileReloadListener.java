package sybyline.anduril.util.data;

import com.google.common.collect.Maps;
import java.io.*;
import java.util.Map;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.common.Anduril;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.function.TriConsumer;

import org.apache.logging.log4j.*;

public class FileReloadListener<T> extends ReloadListener<Map<ResourceLocation, T>> {

	private static final Logger LOGGER = LogManager.getLogger();
	private final IFormat<T> format;
	private final String folder;
	private final String folder_slash;
	private final String extension;
	private final int extension_length;
	private final TriConsumer<Map<ResourceLocation, T>, IResourceManager, IProfiler> triConsumer;

	public FileReloadListener(IFormat<T> format, String folder, TriConsumer<Map<ResourceLocation, T>, IResourceManager, IProfiler> triConsumer) {
		this.format = format;
		this.folder = folder;
		this.folder_slash = folder + "/";
		this.extension = format.filename("");
		this.extension_length = extension.length();
		this.triConsumer = triConsumer;
		Anduril.LOGGER.info("New FileReloadListener:"+folder+"/*"+extension);
	}

	@Override
	protected Map<ResourceLocation, T> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		Map<ResourceLocation, T> map = Maps.newHashMap();
		int pathBegin = this.folder.length() + 1;
		for (ResourceLocation resourcelocation : resourceManagerIn.getAllResourceLocations(this.folder, str -> str.endsWith(extension))) {
			String s = resourcelocation.getPath();
			ResourceLocation singleLoc = new ResourceLocation(resourcelocation.getNamespace(), s.substring(pathBegin, s.length() - extension_length));
			try (
				IResource resource = resourceManagerIn.getResource(resourcelocation);
				InputStream stream = resource.getInputStream();
			) {
				T thing = format.read(singleLoc, stream);
				if (thing != null) {
					T prev = map.put(singleLoc, thing);
					if (prev != null) {
						throw new IllegalStateException("Duplicate data file ignored with ID " + singleLoc);
					}
				} else {
					LOGGER.error("Couldn't load data file {} from {} as it's null or empty", singleLoc, resourcelocation);
				}
			} catch (Exception e) {
				LOGGER.error("Couldn't parse data file {} from {}", singleLoc, resourcelocation, e);
			}
		}
		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, T> data, IResourceManager resources, IProfiler profiler) {
		this.triConsumer.accept(data, resources, profiler);
	}

	public FileReloadListener<T> register(IResourceManager resources) {
		if (resources instanceof IReloadableResourceManager) {
			((IReloadableResourceManager)resources).addReloadListener(this);
		} else {
			// maybe log, but there doesn't seem to be a good reason yet
		}
		return this;
	}

	protected ResourceLocation getPreparedPath(ResourceLocation location) {
		return Util.Structs.sandwich(location, this.folder_slash, this.extension);
	}

}
