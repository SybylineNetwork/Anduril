package sybyline.anduril.scripting.api.server;

import java.util.Set;

public interface IPermission {

	public String key();

	public String desc();

	public Set<IPermission> children();

	public default void add(IPermission permission) {
		if (!posesses(permission)) children().add(permission);
	}

	public default void remove(IPermission permission) {
		children().remove(permission);
	}

	public default boolean posesses(IPermission permission) {
		if (key().equals(permission.key())) {
			return true;
		}
		return children().stream().anyMatch(child -> child.posesses(permission));
	}

	public default boolean exists() {
		return !key().isEmpty();
	}

}
