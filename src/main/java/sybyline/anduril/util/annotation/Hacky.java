package sybyline.anduril.util.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({ TYPE, FIELD, METHOD, CONSTRUCTOR, LOCAL_VARIABLE })
public @interface Hacky {

	public String value() default "";

}
