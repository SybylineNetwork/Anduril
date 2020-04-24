package sybyline.anduril.extensions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import sybyline.anduril.Anduril;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubmodMarker {

	/**
	 * The modid of the mod this submod requires.
	 */
	public abstract String value() default Anduril.MODID;

	public abstract Bus[] busses() default { Bus.MOD, Bus.FORGE };

}