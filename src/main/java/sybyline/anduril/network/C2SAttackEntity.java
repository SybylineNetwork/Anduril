package sybyline.anduril.network;

import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import sybyline.anduril.AndurilGameRules;
import sybyline.anduril.coremods.DualWield;
import sybyline.anduril.extensions.SybylineNetwork.PacketSpec;

public final class C2SAttackEntity implements PacketSpec<C2SAttackEntity> {

	public static final int NULL_ENTITY = 0xFFFFFFFF;

	public C2SAttackEntity with(Hand hand) {
		this.target = NULL_ENTITY;
		this.hand = hand;
		return this;
	}

	public C2SAttackEntity with(Entity target, Hand hand) {
		this.target = target.getEntityId();
		this.hand = hand;
		return this;
	}

	private int target;
	private Hand hand;
	// Additional fields for entity parts?

	public void read(PacketBuffer buffer) {
		this.target = buffer.readInt();
		this.hand = buffer.readEnumValue(Hand.class);
	}

	public void write(PacketBuffer buffer) {
		buffer.writeInt(this.target);
		buffer.writeEnumValue(this.hand);
	}

	public void handle(Supplier<Context> context) {
		this.handleSyncOn(NetworkDirection.PLAY_TO_SERVER, context, ctx -> {
			ServerPlayerEntity sender = ctx.getSender();
			if (!sender.server.getGameRules().getBoolean(AndurilGameRules.DUAL_WIELDING)) {
				sender.connection.disconnect(new TranslationTextComponent("anduril.disconnect.dual_wield_not_enabled"));
				sender.server.logWarning("Player " + sender.getName().getString() + " tried to dual wield attack an entity");
				return;
			}
			DualWield dw = DualWield.of(sender);
			if (target == NULL_ENTITY) {
				int weight = dw.getItemWeight(sender.getHeldItem(hand));
				dw.tryConsumeStamina(weight);
				return;
			}
			Entity entity = this.getEntityById(target, sender);
			sender.markPlayerActive();
			// Validate entity
			if (entity == null) return;
			if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof AbstractArrowEntity || entity == sender) {
				sender.connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_entity_attacked"));
				sender.server.logWarning("Player " + sender.getName().getString() + " tried to attack an invalid entity");
				return;
			}
			// Distance checking -- increased reach here
			boolean canSee = sender.canEntityBeSeen(entity);
			double reach;
			if (hand == Hand.OFF_HAND) {
				reach = 1 + dw.getAsSwapped(() -> sender.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
			} else {
				reach = 1 + sender.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
			}
			double close = 3;
			double checkDistSq = canSee ? (reach * reach) : (close * close);
			double actualDistSq = sender.getDistanceSq(entity);
			// Stamina
			int weight = dw.getItemWeight(sender.getHeldItem(hand));
			float stam = dw.tryConsumeStamina(weight);
			if (stam <= -1.0F) return; // too weak
			
			AttributeModifier mod = new AttributeModifier(DualWield.STAMINA_UUID, "anduril.stamina", stam, Operation.MULTIPLY_TOTAL);
			IAttributeInstance attacks = sender.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
			attacks.removeModifier(DualWield.STAMINA_UUID);
			attacks.applyModifier(mod);
			if (actualDistSq >= checkDistSq) {
				// Player is too far away
			} else if (hand == Hand.MAIN_HAND) {
				sender.attackTargetEntityWithCurrentItem(entity);
			} else if (hand == Hand.OFF_HAND) {
				dw.attackTargetEntityWithCurrentItemOffhand(entity);
			}
		});
	}
	
}
