package sybyline.anduril.common.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import sybyline.anduril.extensions.event.SybylineExtensions;
import sybyline.anduril.extensions.event.SybylineNetworkInitEvent;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.rtc.RuntimeTricks;

public class SybylineNetwork {

	private static final Map<Class<? extends PacketSpec<?>>, SybylineNetwork> registrymap = new HashMap<>();

	private RuntimeTricks.ConstructionStrategy strat = RuntimeTricks.ConstructionStrategy.CASCADE;

	public final ResourceLocation id;
	public final SimpleChannel network;
	private final AtomicInteger index;

	public SybylineNetwork(ResourceLocation id, String version) {
		this(id, version, null);
	}

	public SybylineNetwork(ResourceLocation id, Supplier<String> version) {
		this(id, version, null);
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init) {
		this(id, version, init, version::equals);
	}

	public SybylineNetwork(ResourceLocation id, Supplier<String> version, Consumer<SybylineNetwork> init) {
		this(id, version, init, querey -> version.get().equals(querey));
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init, Predicate<String> acceptor) {
		this(id, version, init, acceptor, acceptor);
	}

	public SybylineNetwork(ResourceLocation id, Supplier<String> version, Consumer<SybylineNetwork> init, Predicate<String> acceptor) {
		this(id, version, init, acceptor, acceptor);
	}

	public SybylineNetwork(ResourceLocation id, String version, Consumer<SybylineNetwork> init, Predicate<String> acceptorClient, Predicate<String> acceptorServer) {
		this(id, () -> version, init, acceptorClient, acceptorServer);
	}

	public SybylineNetwork(ResourceLocation id, Supplier<String> version, Consumer<SybylineNetwork> init, Predicate<String> acceptorClient, Predicate<String> acceptorServer) {
		Util.LOG.info("New network: " + id);
		this.id = id;
		this.network = NetworkRegistry.newSimpleChannel(id, version, acceptorClient, acceptorServer);
		this.index = new AtomicInteger(0);
		if (init != null) {
			init.accept(this);
		}
		this.init();
		SybylineExtensions.post(new SybylineNetworkInitEvent(this));
	}

	public void init() {}

	public final void setStrat(RuntimeTricks.ConstructionStrategy strat) {
		this.strat = strat;
	}

	public final <Pkt extends PacketSpec<Pkt>> void register(Class<Pkt> packet) {
		register(packet, RuntimeTricks.createConstructorSupplier(packet, strat));
	}

	@SuppressWarnings("unchecked")
	public final <Pkt extends PacketSpec<Pkt>> void register(Supplier<Pkt> factory) {
		register((Class<Pkt>)factory.get().getClass(), factory);
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <Pkt extends PacketSpec<Pkt>> void registerAll(Supplier<PacketSpec<?>>... factories) {
		for (Supplier<PacketSpec<?>> factory : factories)
			register((Supplier<Pkt>)factory);
	}

	public final <Pkt extends PacketSpec<Pkt>> void register(Class<Pkt> packet, Supplier<Pkt> factory) {
		registrymap.put(packet, this);
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
				try {
					pkt.handle(ctx);
				} finally {
					ctx.get().setPacketHandled(true);
				}
			}
		);
	}

	public interface PacketSpec<T extends PacketSpec<T>> {

		public void read(PacketBuffer buffer);

		public void write(PacketBuffer buffer);

		public void handle(Supplier<Context> context);

		public default SybylineNetwork getRegisteredNet() {
			SybylineNetwork ret = registrymap.get(this.getClass());
			if (ret == null) throw new RuntimeException("Unregistered packet "+this.getClass());
			return ret;
		}

		public default void sendToClient() {
			throw Util.exception();
		}

		public default void sendToServer() {
			getRegisteredNet().network.send(PacketDistributor.SERVER.noArg(), this);
		}

		public default void sendTo(PacketTarget target) {
			getRegisteredNet().network.send(target, this);
		}

		public default void sendToIfRemote(PlayerEntity target) {
			if (target instanceof ServerPlayerEntity) sendTo((ServerPlayerEntity)target);
		}

		public default void sendTo(ServerPlayerEntity target) {
			getRegisteredNet().network.send(PacketDistributor.PLAYER.with(() -> target), this);
		}

		public default void sendToTrackers(Entity target) {
			getRegisteredNet().network.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), this);
		}

		public default void sendTo(Collection<ServerPlayerEntity> targets) {
			getRegisteredNet().network.send(PacketDistributor.NMLIST.with(() -> targets.stream().map(p -> p.connection.netManager).collect(Collectors.toList())), this);
		}

		public default IPacket<?> vanilla(NetworkDirection direction) {
			return getRegisteredNet().network.toVanillaPacket(this, direction);
		}

		public default void handleAsyncOn(LogicalSide side, Supplier<Context> context, Consumer<Context> task) {
			Context ctx = context.get();
			if (ctx.getDirection().getReceptionSide() == side)
				task.accept(ctx);
		}

		public default void handleAsyncOn(Supplier<Context> context, Consumer<Context> clientTask, Consumer<Context> serverTask) {
			Context ctx = context.get();
			(ctx.getDirection().getReceptionSide().isClient() ? clientTask : serverTask).accept(ctx);
		}

		public default void handleAsyncOn(LogicalSide side, Supplier<Context> context, Supplier<? extends Runnable> task) {
			Context ctx = context.get();
			if (ctx.getDirection().getReceptionSide() == side)
				task.get().run();
		}

		public default void handleAsyncOn(Supplier<Context> context, Supplier<? extends Runnable> clientTask, Supplier<? extends Runnable> serverTask) {
			Context ctx = context.get();
			(ctx.getDirection().getReceptionSide().isClient() ? clientTask : serverTask).get().run();
		}

		public default void handleSyncOn(LogicalSide side, Supplier<Context> context, Consumer<Context> task) {
			Context ctx = context.get();
			if (ctx.getDirection().getReceptionSide() == side)
				ctx.enqueueWork(() -> task.accept(ctx));
		}

		public default void handleSyncOn(Supplier<Context> context, Consumer<Context> clientTask, Consumer<Context> serverTask) {
			Context ctx = context.get();
			context.get().enqueueWork(()->(ctx.getDirection().getReceptionSide().isClient() ? clientTask : serverTask).accept(ctx));
		}

		public default void handleSyncOn(LogicalSide side, Supplier<Context> context, Supplier<? extends Runnable> task) {
			Context ctx = context.get();
			if (ctx.getDirection().getReceptionSide() == side)
				ctx.enqueueWork(() -> task.get().run());
		}

		public default void handleSyncOn(Supplier<Context> context, Supplier<? extends Runnable> clientTask, Supplier<? extends Runnable> serverTask) {
			Context ctx = context.get();
			ctx.enqueueWork((ctx.getDirection().getReceptionSide().isClient() ? clientTask : serverTask).get());
		}

		public default Entity getEntityById(int targetID, PlayerEntity sender) {
			if (sender instanceof ServerPlayerEntity) {
				return ServerLifecycleHooks.getCurrentServer().getWorld(sender.dimension).getEntityByID(targetID);
			} else {
				return sender.world.getEntityByID(targetID);
			}
		}

	}

}
