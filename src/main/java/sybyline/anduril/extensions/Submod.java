package sybyline.anduril.extensions;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.*;
import org.objectweb.asm.Type;
import com.google.common.collect.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import sybyline.anduril.client.SubmodClient;

/**
 * Submods extend from this class, have an {@code @Submod.Marker} on their type,
 * and have a {@code public static final SubmodType INSTANCE_FIELD = new SubmodType();}
 * The submod object is registered to both the FML and Forge event busses.
 */
public abstract class Submod {

	protected Submod() {
		synchronized (Submod.class) {
			final Class<? extends Submod> clazz = this.getClass();
			if (submodClasses.contains(clazz))
				throw new RuntimeException("Submods may not have multiple instances");
			marker = clazz.getAnnotation(SubmodMarker.class);
			if (marker == null)
				throw new RuntimeException("Submods must be marked");
			submod_id = new ResourceLocation(marker.value());
			submods.add(this);
		}
	}

	/**
	 * Called during the FMLCommonSetupEvent. Make calls to the Submod API during this event
	 */
	protected abstract void submodSetup();

	/**
	 * Called during the FMLCommonSetupEvent. Make calls to the Submod API during this event
	 */
	protected abstract void enqueIMCMessages();

	/**
	 * Ensure that some task gets run synchronously, by default, submods are initialized concurrently
	 */
	protected final Optional<Exception> doSync(Runnable task) {
		synchronized (Submod.class) {
			try {
				task.run();
				return Optional.empty();
			} catch(Exception e) {
				return Optional.of(e);
			}
		}
	}

	/**
	 * 
	 */
	protected final Optional<Exception> tryIfClassExists(String classname, Supplier<Runnable> task) {
		try {
			Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
			task.get().run();
			return Optional.empty();
		} catch(Exception e) {
			return Optional.of(e);
		}
	}

	private final SubmodMarker marker;
	public final ResourceLocation submod_id;

	public static final Set<Submod> submods() {
		return submodsView;
	}

	// Internal

	private static final Set<Class<? extends Submod>> submodClasses = Sets.newHashSet();
	private static final Set<Submod> submods = Sets.newHashSet();
	private static final Set<Submod> submodsView = Collections.unmodifiableSet(submods);

	private static final Type SUBMOD = Type.getType(SubmodMarker.class);
	private static final Logger LOGGER = LogManager.getLogger("Anduril Submod API");

	private static boolean hasLoaded = false;

	public static void loadSubmods() {
		synchronized (SubmodMarker.class) {
			if (hasLoaded)
				throw new RuntimeException("Submods have already been loaded");
			hasLoaded = true;
			LOGGER.info("Loading submods");
			final ModList modlist = ModList.get();
			final List<String> classes = modlist
				.getAllScanData()
				.stream()
				.map(ModFileScanData::getAnnotations)
		        .flatMap(Collection::stream)
		        .filter(a -> SUBMOD.equals(a.getAnnotationType()))
		        .filter(a -> {
		        	ResourceLocation loc = new ResourceLocation(String.valueOf(a.getAnnotationData().get("value")));
		        	boolean ret = modlist.getModObjectById(loc.getNamespace()).isPresent();
		        	if (ret) {
		        		LOGGER.info("Using submod " + loc + ", required mod exists.");
		        	} else {
		        		LOGGER.info("Ignoring submod " + loc + ", required mod is not present.");
		        	}
		        	return ret;
		        })
		        .map(AnnotationData::getClassType)
		        .map(Type::getClassName)
		        .collect(Collectors.toList());
			final ClassLoader ctx = Thread.currentThread().getContextClassLoader();
			classes.parallelStream().forEach(classname -> {
				try {
					LOGGER.info("Attempting to load submod at " + classname);
					final Class<?> pluginclass = Class.forName(classname, true, ctx);
					LOGGER.info("Loaded submod at " + pluginclass.getName());
				} catch(Exception e) {
					LOGGER.error("Failed to load submod at " + classname + ":", e);
				}
			});
			Map<Submod, Exception> exceptions = Maps.newConcurrentMap();
			LOGGER.info("Attempting to initialize submods concurrently...");
			submods.parallelStream().forEach(submod -> {
				try {
					submod.submodSetup();
				} catch(Exception e) {
					exceptions.put(submod, e);
				}
			});
			if (exceptions.size() != 0) {
				LOGGER.error("Initialized submods with " + exceptions.size() + " exceptions:");
				exceptions.forEach((submod, e) -> {
					LOGGER.error("  Submod " + submod.submod_id + " has errored, it will be removed from the list:");
					submods.remove(submod);
					e.printStackTrace();
				});
			} else {
				LOGGER.info("Initialized " + submods.size() + " submods");
			}
		}
	}

	public static void interModCommsEnqueue() {
		submods.forEach(submod -> {
    		try {
    			submod.enqueIMCMessages();
    		} catch(Exception e) {
    			LOGGER.error("Submod errored during InterModComms: " + submod.submod_id + ":", e);
    			e.printStackTrace();
    		}
    	});
	}

	public static void gameStart(FMLClientSetupEvent event) {
		try {
			SubmodClient.INSTANCE.init(event);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
