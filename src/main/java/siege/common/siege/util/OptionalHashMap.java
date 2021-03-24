package siege.common.siege.util;

import java.util.*;

public class OptionalHashMap<K, V> extends AbstractMap<K, Optional<V>> implements OptionalMap<K, V> {

	private final Set<Entry<K, Optional<V>>> backer = new HashSet<>();

	@Override
	public Set<Entry<K, Optional<V>>> entrySet() {
		return backer;
	}

}
