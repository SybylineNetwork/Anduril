package sybyline.anduril.extensions.event;

import sybyline.anduril.common.network.SybylineNetwork;

public class SybylineNetworkInitEvent extends SybylineEvent {

	public SybylineNetworkInitEvent(SybylineNetwork network) {
		this.network = network;
	}

	public final SybylineNetwork network;

}
