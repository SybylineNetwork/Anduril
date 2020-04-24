package sybyline.anduril.extensions.event;

import net.minecraftforge.common.IExtensibleEnum;

public class EnumExtensionEvent<T extends Enum<T> & IExtensibleEnum> extends SybylineEvent {

	public EnumExtensionEvent(Class<T> clazz) {
		this.clazz = clazz;
	}

	public final Class<T> clazz;

}
