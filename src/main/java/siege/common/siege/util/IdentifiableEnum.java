package siege.common.siege.util;

public interface IdentifiableEnum<E extends Enum<E>&IdentifiableEnum<E>> extends Identifiable {

	@SuppressWarnings("unchecked")
	@Override
	public default String identifier() {
		return sanitize(((E)this).name());
	}

	public static String sanitize(String string) {
		return string.toLowerCase().replace('_', '-');
	}

}
