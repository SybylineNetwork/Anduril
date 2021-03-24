package sybyline.anduril.util.function;

@FunctionalInterface
public interface VarFunction<R> {

	public R apply(Object... args);

}
