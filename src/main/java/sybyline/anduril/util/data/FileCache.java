package sybyline.anduril.util.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import sybyline.anduril.util.Util;

@EventBusSubscriber
public class FileCache<Identifier, Data, Type extends ICachable<Data>> implements ICache<Identifier, Data, Type> {

	private static final List<ICache<?, ?, ?>> caches = Collections.synchronizedList(new ArrayList<>());

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppedEvent event) {
		caches.forEach(ICache::saveAllEntries);
	}

	FileCache(ResourceLocation location, IFormat<Data> format, Function<Identifier, Type> factory, Function<Identifier, String> filenameFactory) {
		this.file = null;
		this.resourceLocation = location;
		this.format = format;
		this.factory = factory;
		this.filenameFactory = filenameFactory;
		caches.add(this);
		this.checkFile();
	}

	public void setServer(MinecraftServer server) {
		if (server == null) {
			this.saveAllEntries();
			this.file = null;
		} else {
			this.file = new File(server.getActiveAnvilConverter().getFile(server.getFolderName(), resourceLocation.getNamespace()), resourceLocation.getPath());
		}
		this.checkFile();
	}

	private final void checkFile() {
		if (file != null) {
			if (!file.exists()) {
				file.mkdirs();
			} else if(!file.isDirectory()) {
				throw new RuntimeException("Specified file for " + resourceLocation + " already exists as a non-directory file");
			}
		}
	}

	private File file;
	private final ResourceLocation resourceLocation;
	private final IFormat<Data> format;
	private final Function<Identifier, Type> factory;
	private final Function<Identifier, String> filenameFactory;

	private final Map<Identifier, CacheEntry> cache = Maps.newHashMap();
	private final List<CacheEntry> savable = Lists.newArrayList();

	private boolean verbose = false;

	public FileCache<Identifier, Data, Type> setVerbosity(boolean verbosity) {
		verbose = verbosity;
		return this;
	}

	@Nullable
	public Type getOrCreate(Identifier id) {
		return get(id, true);
	}

	@Nullable
	public Type get(Identifier id, boolean shouldCreate) {
		if (id == null) {
			return null;
		}
		CacheEntry ret = getEntry(id, shouldCreate);
		if (ret == null) {
			return null;
		}
		return ret.datum;
	}

	public void deleteEntry(Identifier id) {
		if (id == null) return;
		CacheEntry ret = getEntry(id, false);
		if (ret == null) return;
		if (verbose) Util.LOG.info("Deleting data entry " + ret.identifier + "@" + resourceLocation + " from " + ret.location.getAbsolutePath());
		synchronized (cache) {
			cache.remove(id);
		}
		synchronized (savable) {
			savable.remove(id);
		}
		ret.location.delete();
	}

	private CacheEntry getEntry(Identifier id, boolean shouldCreate) {
		CacheEntry ret;
		synchronized (filenameFactory.apply(id).intern()) {
			ret = cache.get(id);
			if (ret == null) {
				CacheEntry maybe = null;
				synchronized (savable) {
					for (CacheEntry toCheck : savable) {
						if (toCheck.identifier.equals(id)) {
							synchronized (cache) {
								cache.put(toCheck.identifier, toCheck);
							}
							maybe = toCheck;
							break;
						}
					}
					if (maybe != null) {
						savable.remove(maybe);
					}
				}
				ret = maybe;
			}
			if (ret == null) {
				ret = readEntry(id, shouldCreate);
				if (ret != null) {
					synchronized (cache) {
						cache.put(ret.identifier, ret);
					}
				}
			}
		}
		return ret;
	}

	public void findStaleEntries() {
		List<CacheEntry> old = Lists.newArrayList();
		synchronized (cache) {
			Collection<CacheEntry> set = cache.values();
			if (!set.isEmpty()) {
				for (CacheEntry entry : set) {
					if (entry.datum.shouldKeep()) continue;
					old.add(entry);
				}
				set.removeAll(old);
			}
		}
		synchronized (savable) {
			savable.addAll(old);
		}
	}

	public void saveStaleEntries() {
		synchronized (savable) {
			savable.forEach(this::writeEntry);
			savable.clear();
		}
	}

	public void saveAllEntries() {
		synchronized (cache) {
			cache.values().forEach(this::writeEntry);
			cache.clear();
		}
		synchronized (savable) {
			savable.forEach(this::writeEntry);
			savable.clear();
		}
	}

	@Nullable
	private CacheEntry readEntry(Identifier id, boolean shouldCreate) {
		Type datum = factory.apply(id);
		datum.construct();
		File loc = new File(file, format.filename(filenameFactory.apply(id)));
		if (verbose) Util.LOG.info("Reading data entry " + id + "@" + resourceLocation + " from " + loc.getAbsolutePath());
		CacheEntry entry = new CacheEntry(id, loc, datum);
		if (loc.exists() && loc.isFile()) {
			Data d = format.readOrNull(loc);
			if (d != null) {
				datum.readFrom(d);
			} else {
				Util.LOG.error("Entry did not exist, initializing! This is a serious error.");
				datum.firstLoad();
			}
		} else if (shouldCreate) {
			if (verbose) Util.LOG.info("Entry did not exist, initializing and saving! This is normal.");
			datum.firstLoad();
			this.writeEntry(entry);
		} else {
			return null;
		}
		return entry;
	}

	private void writeEntry(CacheEntry value) {
		if (verbose) Util.LOG.info("Saving data entry " + value.identifier + "@" + resourceLocation + " to " + value.location.getAbsolutePath());
		Data d = format.create();
		synchronized (value.datum) {
			value.datum.writeTo(d);
		}
		format.writeOrNull(value.location, d);
	}

	private class CacheEntry {
		private CacheEntry(Identifier identifier, File location, Type datum) {
			this.identifier = identifier;
			this.location = location;
			this.datum = datum;
		}
		private final Identifier identifier;
		private final File location;
		private final Type datum;
	}

}
