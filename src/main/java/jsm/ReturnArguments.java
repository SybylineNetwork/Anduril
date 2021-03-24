package jsm;

import java.lang.annotation.*;

@Target({
	ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface ReturnArguments {

	public abstract Class<?>[] value();

}
