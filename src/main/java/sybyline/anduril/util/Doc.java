package sybyline.anduril.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.*;

@Documented
@Retention(CLASS)
@Target({ TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE })
public @interface Doc {

	@Doc("An explanation for this member. Persists so that users without a javadoc can still understand what is going on.")
	public String value() default "";

}
