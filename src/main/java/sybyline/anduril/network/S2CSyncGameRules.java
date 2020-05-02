package sybyline.anduril.network;

import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import sybyline.anduril.AndurilGameRules;
import sybyline.anduril.extensions.SybylineNetwork.PacketSpec;

public final class S2CSyncGameRules implements PacketSpec<S2CSyncGameRules> {

	public S2CSyncGameRules with(MinecraftServer server) {
		dualWielding = server.getGameRules().getBoolean(AndurilGameRules.DUAL_WIELDING);
		return this;
	}

	private boolean dualWielding;

	public void read(PacketBuffer buffer) {
		dualWielding = buffer.readBoolean();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeBoolean(dualWielding);
	}

	public void handle(Supplier<Context> context) {
		AndurilGameRules.Client.client_dualWielding = dualWielding;
	}
	
}