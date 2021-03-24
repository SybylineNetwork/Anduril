package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.util.ResourceLocation;
import sybyline.anduril.common.Anduril;
import sybyline.anduril.util.rtc.RuntimeTricks;
import sybyline.anduril.util.rtc.RuntimeTypeChanger;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DynamicRegistry<V extends IForgeRegistryEntry<V>> extends ForgeRegistry<V> {

	@Deprecated
	public DynamicRegistry(RegistryManager stage, ResourceLocation name, RegistryBuilder<V> builder) {
		super(stage, name, builder);
	}

	static final Map<DynamicRegistry, RegistryExtra> extras = new HashMap<>();
	static final Set<DynamicRegistry<?>> registries = new HashSet<>();
	static final RuntimeTypeChanger<DynamicRegistry> rtc = RuntimeTricks.getTypeChanger(DynamicRegistry.class).then(registries::add);

	public static <V extends IForgeRegistryEntry<V>> DynamicRegistry<V> of(IForgeRegistry<V> forge) {
		if (forge instanceof DynamicRegistry)
			return (DynamicRegistry<V>)forge;
		return rtc.<DynamicRegistry<V>>changeTypeGenericCasting(forge);
	}

	public static Set<DynamicRegistry<?>> getDynamicRegistries() {
		return Collections.unmodifiableSet(registries);
	}

	@Override
	public Set<ResourceLocation> getKeys() {
		Set<ResourceLocation> ret = new HashSet<>();
		ret.addAll(super.getKeys());
		ret.addAll(getExtension().extra_names.keySet());
		return ret;
	}

	@Override
	public Collection<V> getValues() {
		Collection<V> ret = new ArrayList<>();
		ret.addAll(super.getValues());
		ret.addAll(getExtension().extra_names.values());
		return ret;
	}

	@Override
	public Set<Entry<ResourceLocation, V>> getEntries() {
		Set<Entry<ResourceLocation, V>> ret = new HashSet<>();
		ret.addAll(super.getEntries());
		ret.addAll(getExtension().extra_names.entrySet());
		return ret;
	}

	@Override
	public V getValue(ResourceLocation key) {
		V value = getExtension().extra_names.get(key);
		if (value != null)
			return value;
		return super.getValue(key);
	}

	@Override
	public V getRaw(ResourceLocation key) {
		V value = getExtension().extra_names.get(key);
		if (value != null)
			return value;
		return super.getRaw(key);
	}

	@Override
	public V getValue(int id) {
		V value = getExtension().extra_ids.get(id);
		if (value != null)
			return value;
		return super.getValue(id);
	}

	@Override
	public ResourceLocation getKey(V value) {
		ResourceLocation key = getExtension().extra_names.inverse().get(value);
		if (key != null)
			return key;
		return super.getKey(value);
	}

	@Override
	public int getID(V value) {
		Integer id = getExtension().extra_ids.inverse().get(value);
		if (id != null)
			return id;
		return super.getID(value);
	}

	@Override
	public int getID(ResourceLocation name) {
		V value = getExtension().extra_names.get(name);
		if (value != null)
			return this.getID(value);
		return super.getID(name);
	}

	public RegistryExtra<V> getExtension() {
		return extras.computeIfAbsent(this, RegistryExtra::new);
	}

	public void resetExtension() {
		getExtension().reset();
	}

	public void resetExtensionConditional() {
		getExtension().resetConditional();
	}

	public boolean registerExt(int id, ResourceLocation key, V thing) {
		return getExtension().register(id, key, thing);
	}

	public boolean registerExt(ResourceLocation key, V thing) {
		return getExtension().register(key, thing);
	}

	public static class RegistryExtra<V extends IForgeRegistryEntry<V>> {
		RegistryExtra(DynamicRegistry<V> registry) {
			this.registry = registry;
		}
		final DynamicRegistry<V> registry;
		final AtomicInteger ider = new AtomicInteger(-1);
		public final BiMap<Integer, V> extra_ids = HashBiMap.create();
		public final BiMap<ResourceLocation, V> extra_names = HashBiMap.create();
		public synchronized void reset() {
			extra_ids.clear();
			extra_names.clear();
			ider.set(-1);
		}
		public synchronized void resetConditional() {
			if (Anduril.isDataRemote()) this.reset();
		}
		public synchronized boolean register(int id, ResourceLocation key, V thing) {
			if (extra_ids.containsKey(id)) return false;
			if (extra_names.containsKey(key)) return false;
			extra_names.put(key, thing);
			extra_ids.put(id, thing);
			return true;
		}
		public synchronized boolean register(ResourceLocation key, V thing) {
			if (extra_names.containsKey(key)) return false;
			extra_names.put(key, thing);
			extra_ids.put(ider.getAndDecrement(), thing);
			return true;
		}
	}

}
