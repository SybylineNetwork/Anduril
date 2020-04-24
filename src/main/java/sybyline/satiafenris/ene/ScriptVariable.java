package sybyline.satiafenris.ene;

public interface ScriptVariable<T> {

	public void implementation_set(Object value) throws Exception;

	public Object implementation_get() throws Exception;

	public default void set(T object) {
		try {
			this.implementation_set(object);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public default T get() {
		try {
			@SuppressWarnings("unchecked")
			T ret = (T) this.implementation_get();
			return ret;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
