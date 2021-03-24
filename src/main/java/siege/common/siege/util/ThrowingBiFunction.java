package siege.common.siege.util;

public interface ThrowingBiFunction<T, U, R> {

	public abstract R apply(T t, U u) throws Exception;

}
