package sybyline.anduril.extensions.event;

import sybyline.anduril.extensions.SybylineNetwork;

public class SybylineNetworkInitEvent extends SybylineEvent {

	public SybylineNetworkInitEvent(SybylineNetwork network) {
		this.network = network;
	}

	public final SybylineNetwork network;

}
