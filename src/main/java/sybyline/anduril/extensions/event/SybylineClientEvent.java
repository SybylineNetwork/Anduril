package sybyline.anduril.extensions.event;

import net.minecraft.client.Minecraft;

public class SybylineClientEvent extends SybylineEvent {

	protected SybylineClientEvent(Minecraft mc) {
		this.mc = mc;
	}

	public final Minecraft mc;

}
