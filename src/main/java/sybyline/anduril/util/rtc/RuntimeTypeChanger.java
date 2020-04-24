package sybyline.anduril.util.rtc;

public interface RuntimeTypeChanger<T> {

	public T changeType(Object object);

	@SuppressWarnings("unchecked")
	public static <T> RuntimeTypeChanger<T> casting() {
		return object -> (T) object;
	}

}
