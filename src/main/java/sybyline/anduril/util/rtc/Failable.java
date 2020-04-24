package sybyline.anduril.util.rtc;

public interface Failable<T> {

	public T fail() throws Throwable;

}
