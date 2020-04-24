package sybyline.satiafenris.ene;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import net.minecraft.nbt.*;

public final class ScriptExtensions {

	private ScriptExtensions() {}

	public static ScriptExtension<?> fromNBT(INBT nbt) {
		for (ScriptExtensionType<?> extension : extensions) {
			ScriptExtension<?> bean = extension.tryConvert(nbt);
			if (bean != null)
				return bean;
		}
		return null;
	}

	private static final List<ScriptExtensionType<?>> extensions = Lists.newArrayList();

	public static <Type extends ScriptExtension<Type>> void register(ScriptExtensionType<Type> nbtType) {
		extensions.add(nbtType);
	}

	public static <E extends Enum<E>&ScriptExtension<E>> void registerEnum(String id, Class<E> clazz) {
		registerEnum(id, clazz, null);
	}

	public static <E extends Enum<E>&ScriptExtension<E>> void registerEnum(String id, Class<E> clazz, Map<E, String> map) {
		register(new ScriptExtensionType<E>() {
			private final String prefix = "__enum:" + id + ";";
			private final int length = prefix.length();
			private final Map<E, String> names = Collections.unmodifiableMap(map);
			private final Map<String, E> enums = Collections.unmodifiableMap(map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey)));
			public E tryConvert(INBT nbt) {
				if (nbt instanceof StringNBT) {
					String string = ((StringNBT)nbt).getString();
					if (string.startsWith(prefix)) {
						return enums.get(string.substring(length));
					}
				}
				return null;
			}
			public INBT convert(E thing) {
				return StringNBT.valueOf(prefix + names.get(thing));
			}
		});
	}

	public static <Type extends ScriptExtensionSerial<Type, StringNBT>> void registerString(String id, Function<String, Type> constructor) {
		registerString(id, constructor, Type::toNBT);
	}

	public static <Type extends ScriptExtension<Type>> void registerString(String id, Function<String, Type> constructor, Function<Type, INBT> writer) {
		register(new ScriptExtensionType<Type>() {
			private final String prefix = "__bean:" + id + ";";
			private final int length = prefix.length();
			public Type tryConvert(INBT nbt) {
				if (nbt instanceof StringNBT) {
					String string = ((StringNBT)nbt).getString();
					if (string.startsWith(prefix)) {
						return constructor.apply(string.substring(length));
					}
				}
				return null;
			}
			public INBT convert(Type thing) {
				return writer.apply(thing);
			}
		});
	}

	public static <Type extends ScriptExtensionSerial<Type, CompoundNBT>> void registerCompound(String id, Function<CompoundNBT, Type> constructor) {
		registerCompound(id, constructor, Type::toNBT);
	}

	public static <Type extends ScriptExtension<Type>> void registerCompound(String id, Function<CompoundNBT, Type> constructor, Function<Type, CompoundNBT> writer) {
		register(new ScriptExtensionType<Type>() {
			private static final String key = "__ext";
			public Type tryConvert(INBT nbt) {
				if (nbt instanceof CompoundNBT) {
					CompoundNBT compound = ((CompoundNBT)nbt);
					if (compound.getString(key).contentEquals(id)) {
						return constructor.apply(compound);
					}
				}
				return null;
			}
			public CompoundNBT convert(Type thing) {
				CompoundNBT ret = writer.apply(thing);
				ret.putString(key, id);
				return ret;
			}
		});
	}

}
