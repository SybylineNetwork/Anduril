package sybyline.anduril.util.function;

@FunctionalInterface
public interface TriFunction<S1, S2, S3, R, E extends Throwable> {

	public R apply(S1 s1, S2 s2, S3 s3) throws E;

}
