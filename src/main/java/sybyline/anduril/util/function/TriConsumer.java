package sybyline.anduril.util.function;

@FunctionalInterface
public interface TriConsumer<S1, S2, S3> {

	public void accept(S1 s1, S2 s2, S3 s3);

}
