package sybyline.anduril.extensions;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import sybyline.anduril.extensions.event.SybylineExtensions;
import sybyline.anduril.extensions.event.SybylineNetworkInitEvent;
import sybyline.anduril.util.Util;

public class SybylineNetwork {

	public final ResourceLocation id;
	public final SimpleChannel network;
	private final AtomicInteger index;

	public SybylineNetwork(ResourceLocation id, String version) {
		this(id, version, null);
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init) {
		this(id, version, init, version::equals);
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init, Predicate<String> acceptor) {
		this(id, version, init, acceptor, acceptor);
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init, Predicate<String> acceptorClient, Predicate<String> acceptorServer) {
		Util.LOG.info("New network: " + id);
		this.id = id;
		this.network = NetworkRegistry.newSimpleChannel(id, () -> version, acceptorClient, acceptorServer);
		this.index = new AtomicInteger(0);
		if (init != null) {
			init.accept(this);
		}
		this.init();
		SybylineExtensions.post(new SybylineNetworkInitEvent(this));
	}

	public void init() {}

	public <Pkt extends PacketSpec<Pkt>> void register(Class<Pkt> packet, Supplier<Pkt> factory) {
		network.registerMessage(
			index.getAndIncrement(),
			packet,
			(Pkt pkt, PacketBuffer buf) -> {
				pkt.write(buf);
			},
			(PacketBuffer buf) -> {
				Pkt pkt = factory.get();
				pkt.read(buf);
				return pkt;
			},
			(Pkt pkt, Supplier<Context> ctx) -> {
				pkt.handle(ctx);
				ctx.get().setPacketHandled(true);
			}
		);
	}

	public interface PacketSpec<T extends PacketSpec<T>> {

		public void read(PacketBuffer buffer);

		public void write(PacketBuffer buffer);

		public void handle(Supplier<Context> context);

		public default void sendTo(SybylineNetwork net, PacketTarget target) {
			net.network.send(target, this);
		}

		public default void sendTo(SybylineNetwork net, ServerPlayerEntity target) {
			net.network.send(PacketDistributor.PLAYER.with(() -> target), this);
		}

		public default void sendTo(SybylineNetwork net, Collection<ServerPlayerEntity> targets) {
			net.network.send(PacketDistributor.NMLIST.with(() -> targets.stream().map(p -> p.connection.netManager).collect(Collectors.toList())), this);
		}

		public default IPacket<?> vanilla(SybylineNetwork net, NetworkDirection direction) {
			return net.network.toVanillaPacket(this, direction);
		}

	}

}
