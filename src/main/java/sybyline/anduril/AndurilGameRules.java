package sybyline.anduril;

import java.util.function.BiConsumer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.GameRules.RuleType;
import net.minecraftforge.coremod.api.ASMAPI;
import sybyline.anduril.network.S2CSyncGameRules;
import sybyline.anduril.util.rtc.ReflectionTricks;

public final class AndurilGameRules {

	private AndurilGameRules() {}

	public static final RuleKey<BooleanValue> DUAL_WIELDING = GameRules.register("anduril:dualWielding", booleanvalue_create(true));

	public static void init() {
		Anduril.LOGGER.info("Registered 1 game rule!");
	}

	public static void updateOne(ServerPlayerEntity player) {
		new S2CSyncGameRules().with(player.server).sendTo(Anduril.instance().network, player);
	}

	public static void updateAll(MinecraftServer server) {
		new S2CSyncGameRules().with(server).sendTo(Anduril.instance().network, server.getPlayerList().getPlayers());
	}

	@SuppressWarnings("unchecked")
	public static RuleType<BooleanValue> booleanvalue_create(boolean defaultValue) {
		try {
			return (RuleType<BooleanValue>)
				ReflectionTricks.findMethod(BooleanValue.class, new Class<?>[] {
					boolean.class,
					BiConsumer.class,
				}, ASMAPI.mapMethod("func_223567_b"))
				.invoke(null, defaultValue, new BiConsumer<MinecraftServer, BooleanValue>() {
				@Override public void accept(MinecraftServer server, BooleanValue value) {
					updateAll(server);
				}});
		} catch(Exception e) {
			ReflectionTricks.debugDeclaredMethods(BooleanValue.class);
			throw new RuntimeException(e);
		}
	}

	public static final class Client {

		private Client() {}

		public static boolean client_dualWielding = true;

	}

}
