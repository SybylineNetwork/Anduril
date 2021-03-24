package sybyline.anduril.common.network;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import sybyline.anduril.common.AndurilGameRules;
import sybyline.anduril.common.AndurilPlayerData;
import sybyline.anduril.common.AndurilPlayerData.WeaponAttunement;
import sybyline.anduril.common.advancements.AndurilStats;
import sybyline.anduril.common.advancements.StatTrigger;
import sybyline.anduril.common.modelbb.ModelBB;
import sybyline.anduril.common.modelbb.ModelBB.ModelBBTraceResult;
import sybyline.anduril.common.network.SybylineNetwork.PacketSpec;
import sybyline.anduril.coremods.DualWield;
import sybyline.anduril.extensions.ItemModifier;
import sybyline.anduril.extensions.forge.IAndurilItem;
import sybyline.anduril.util.Util;

public final class C2SAttackEntity implements PacketSpec<C2SAttackEntity> {

	public static final int NULL_ENTITY = 0xFFFFFFFF;

	public C2SAttackEntity miss(Hand hand) {
		this.target = NULL_ENTITY;
		this.hand = hand;
		this.partHit = ModelBB.ModelBBPart.NONE;
		return this;
	}

	public C2SAttackEntity attack(Entity target, Hand hand, ModelBBTraceResult trace) {
		this.target = target.getEntityId();
		this.hand = hand;
		this.partHit = trace == null ? ModelBB.ModelBBPart.GENERIC : trace.part;
		return this;
	}

	public C2SAttackEntity slice(Entity target, Hand hand, ModelBBTraceResult trace, Vec3d slice) {
		this.attack(target, hand, trace);
		this.slice = slice;
		return this;
	}

	public C2SAttackEntity interact(Entity target, Hand hand, ModelBBTraceResult trace, Vec3d hit) {
		this.attack(target, hand, trace);
		this.hit = hit;
		return this;
	}

	private int target;
	private Hand hand;
	private ModelBB.ModelBBPart partHit;
	private Vec3d hit = Vec3d.ZERO;
	private Vec3d slice = Vec3d.ZERO;

	public void read(PacketBuffer buffer) {
		this.target = buffer.readInt();
		this.hand = buffer.readEnumValue(Hand.class);
		this.partHit = buffer.readEnumValue(ModelBB.ModelBBPart.class);
		this.hit = buffer.readBoolean() ? new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()) : Vec3d.ZERO;
		this.slice = buffer.readBoolean() ? new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()) : Vec3d.ZERO;
	}

	public void write(PacketBuffer buffer) {
		buffer.writeInt(this.target);
		buffer.writeEnumValue(this.hand);
		buffer.writeEnumValue(this.partHit);
		buffer.writeBoolean(this.hit != Vec3d.ZERO);
		if (this.hit != Vec3d.ZERO) {
			buffer.writeDouble(this.hit.getX());
			buffer.writeDouble(this.hit.getY());
			buffer.writeDouble(this.hit.getZ());
		}
		buffer.writeBoolean(this.slice != Vec3d.ZERO);
		if (this.slice != Vec3d.ZERO) {
			buffer.writeDouble(this.slice.getX());
			buffer.writeDouble(this.slice.getY());
			buffer.writeDouble(this.slice.getZ());
		}
	}

	public static final ThreadLocal<C2SAttackEntity> current = new ThreadLocal<>();
	public Hand getHand() {
		return hand;
	}
	public ModelBB.ModelBBPart getPartHit() {
		return partHit;
	}
	public Vec3d getHitVec() {
		return hit;
	}
	public Vec3d getSliceVec() {
		return slice;
	}

	public void handle(Supplier<Context> context) {
		this.handleSyncOn(LogicalSide.SERVER, context, ctx -> {
			try {
				current.set(this);
				ServerPlayerEntity sender = ctx.getSender();
				if (!AndurilGameRules.SERVER.get(AndurilGameRules.DUAL_WIELD)) {
					sender.connection.disconnect(new TranslationTextComponent("anduril.disconnect.dual_wield_not_enabled"));
					sender.server.logWarning("Player " + sender.getName().getString() + " tried to dual wield on an entity");
					return;
				}
				Entity entity = this.getEntityById(target, sender);
				// Validate entity
				if (entity == null) return;
				if (hit != Vec3d.ZERO) {
					// Interact
					if (ForgeHooks.onInteractEntityAt(sender, entity, hit, hand) != null) return;
					ActionResultType actionresulttype = entity.applyPlayerInteraction(sender, hit, hand);
					if (actionresulttype.isSuccess())
						sender.func_226292_a_(hand, true);
					return;
				}
				DualWield dw = DualWield.of(sender);
				AndurilPlayerData playerdata = AndurilPlayerData.of(sender);
				ItemStack held = sender.getHeldItem(hand);
				if (target == NULL_ENTITY) {
					playerdata.skills.forAll(instance -> instance.swing(hand));
					int weight = IAndurilItem.of(held.getItem()).getItemWeight(held, sender);
					dw.tryConsumeStamina(weight);
					return;
				}
				sender.markPlayerActive();
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
				IAndurilItem itemExtra = IAndurilItem.of(held.getItem());
				int weight = itemExtra.getItemWeight(held, sender);
				float stamMod = dw.tryConsumeStamina(weight);
				
				if (DualWield.twoHandedCheck(sender, hand)) {
					// Player is holding two handed weapon with one hand
					stamMod -= 0.5F;
				}
				
				if (stamMod <= -1.0F) {
					// too weak
					sender.sendStatusMessage(new TranslationTextComponent("anduril.stamina.exhausted"), true);
					return;
				}
				
				WeaponAttunement attunement = null;
				float attuneMod = 0.0F;
				if (itemExtra.isAttuneable(held)) {
					UUID attunementID = ItemModifier.ATTUNEMENT_ID.getValueOrSet(held, Util.Structs::randomUUID);
					attunement = playerdata.attunements.getOrCreate(attunementID);
					if (attunement.needsInit())
						attunement.init();
					attuneMod = attunement.modifier();
				}
				
				float profMod = 0.0F;
				if (itemExtra.isProficienciable(held)) {
					// weapon type proficiency here
				}
				
//				sender.sendStatusMessage(new net.minecraft.util.text.StringTextComponent("prof:"+profMod+" attune:"+attuneMod+" stamina:"+stamMod), true);
				
				AttributeModifier modProficiency = new AttributeModifier(DualWield.PROFICIENCY_UUID, "anduril.proficiency", profMod, Operation.MULTIPLY_TOTAL);
				AttributeModifier modAttunement = new AttributeModifier(DualWield.ATTUNEMENT_UUID, "anduril.attunement", attuneMod, Operation.MULTIPLY_TOTAL);
				AttributeModifier modStamina = new AttributeModifier(DualWield.STAMINA_UUID, "anduril.stamina", stamMod, Operation.MULTIPLY_TOTAL);
				IAttributeInstance attacks = sender.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
				attacks.removeModifier(DualWield.PROFICIENCY_UUID);
				attacks.removeModifier(DualWield.ATTUNEMENT_UUID);
				attacks.removeModifier(DualWield.STAMINA_UUID);
				attacks.applyModifier(modProficiency);
				attacks.applyModifier(modAttunement);
				attacks.applyModifier(modStamina);
				float health = entity instanceof LivingEntity ? ((LivingEntity)entity).getHealth() : -1F;
				StatTrigger stat = null;
				if (actualDistSq >= checkDistSq) {
					// Player is too far away
				} else if (hand == Hand.MAIN_HAND) {
					stat = AndurilStats.DAMAGE_DEALT_MAINHAND_TRIGGER;
					sender.attackTargetEntityWithCurrentItem(entity);
				} else if (hand == Hand.OFF_HAND) {
					stat = AndurilStats.DAMAGE_DEALT_OFFHAND_TRIGGER;
					dw.attackTargetEntityWithCurrentItemOffhand(entity);
				}
				// Damage stats
				if (stat != null && entity instanceof LivingEntity && health > 0) {
					float damage = health - ((LivingEntity)entity).getHealth();
					if (damage > 0) {
						stat.triggerDamage(sender, damage);
						if (attunement != null) {
							attunement.hasDamage += damage;
						}
					}
				}
				playerdata.skills.forAll(instance -> instance.attack(hand));
			} finally {
				current.remove();
			}
		});
	}

}
