package siege.common.siege.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import siege.common.siege.command.ArgumentEnum;

public enum Enums implements ArgumentEnum<Enums>, IdentifiableEnum<Enums> {

	NULL,
	;

	public static <K, V> Map<K, V> map(V[] values, Function<V, K> keyer) {
		return new HashMap<K, V>(Arrays.stream(values).collect(Collectors.toMap(keyer, Function.identity())));
	}

	public static <V extends Enum<V>&IdentifiableEnum<V>> Map<String, V> map(V[] values) {
		return map(values, (v) -> v.identifier()); // Method reference causes BootstrapMethodError
	}

	@Override
	public <T extends ArgumentBuilder<CommandSource, T>, O extends Enum<O>> T addCommand(T in, O operation) {
		return in;
	}

}
