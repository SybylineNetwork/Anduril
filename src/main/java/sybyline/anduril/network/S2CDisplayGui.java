package sybyline.anduril.network;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import sybyline.anduril.extensions.SybylineNetwork.PacketSpec;
import sybyline.anduril.scripting.client.ClientScripting;
import sybyline.anduril.util.Util;

public final class S2CDisplayGui implements PacketSpec<S2CDisplayGui> {

	public S2CDisplayGui with(ResourceLocation loc, boolean redisplay, CompoundNBT nbt) {
		this.loc = loc;
		this.redisplay = redisplay;
		this.nbt = nbt;
		return this;
	}

	private ResourceLocation loc = Util.NULL_RESOURCE;
	private boolean redisplay;
	private CompoundNBT nbt = new CompoundNBT();

	public void read(PacketBuffer buffer) {
		loc = buffer.readResourceLocation();
		redisplay = buffer.readBoolean();
		nbt = buffer.readCompoundTag();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeResourceLocation(loc);
		buffer.writeBoolean(redisplay);
		buffer.writeCompoundTag(nbt);
	}

	public void handle(Supplier<Context> context) {
		S2CDisplayGui packet = this;
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			context.get().enqueueWork(() -> {
				ClientScripting.INSTANCE.serverSendsGui(packet.loc, packet.redisplay, packet.nbt);
			});
		});
	}
	
}