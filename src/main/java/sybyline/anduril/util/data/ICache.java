package sybyline.anduril.util.data;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

public interface ICache<Identifier, Data, Type extends ICachable<Data>> {

	// Call this during startup, supplying a server, and during shutdown supplying null
	public void setServer(@Nullable MinecraftServer server);

	public ICache<Identifier, Data, Type> setVerbosity(boolean verbosity);

	@Nullable
	public Type getOrCreate(Identifier id);

	@Nullable
	public Type get(Identifier id, boolean shouldCreate);

	public void deleteEntry(Identifier id);
	
	// Call periodically
	public default void periodicCleanup() {
		this.saveStaleEntries();
		this.findStaleEntries();
	}

	public void findStaleEntries();

	public void saveStaleEntries();

	public void saveAllEntries();

	public static <Identifier, Data, Type extends ICachable<Data>> ICache<Identifier, Data, Type> files(ResourceLocation location, IFormat<Data> format, Function<Identifier, Type> factory, Function<Identifier, String> filenameFactory) {
		return new FileCache<Identifier, Data, Type>(location, format, factory, filenameFactory);
	}

}
