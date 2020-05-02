package sybyline.anduril.coremods;

import java.util.UUID;
import java.util.function.Supplier;
import com.mojang.blaze3d.platform.GlStateManager.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.coremod.api.ASMAPI;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import sybyline.anduril.Anduril;
import sybyline.anduril.AndurilGameRules;
import sybyline.anduril.extensions.client.SubmodClient;
import sybyline.anduril.network.C2SAttackEntity;
import sybyline.anduril.util.math.Interpolation;
import sybyline.anduril.util.rtc.*;

@EventBusSubscriber
public final class DualWield {

	private static final FieldWrapper<DualWield> anduril_dual_wield = FieldWrapper.of(LivingEntity.class, "anduril_dual_wield");
	private static final FieldWrapper<Integer> ticksSinceLastSwing = FieldWrapper.of(LivingEntity.class, ASMAPI.mapField("field_184617_aD"));

	public static final UUID STAMINA_UUID = UUID.fromString("9ee034b4-3f19-430f-8742-5e27cfac591d");
	public static final UUID WEIGHT_UUID = UUID.fromString("fe7779a3-5e8c-426d-bda1-a050ee2e42df");

	public static DualWield of(LivingEntity player) {
		DualWield ret = anduril_dual_wield.get(player);
		if (ret == null) {
			anduril_dual_wield.set(player, ret = new DualWield(player));
		}
		return ret;
	}

	private DualWield(LivingEntity player) {
		this.living = player;
	}

	private final LivingEntity living;
	private int ticksSinceLastSwingOffhand = 0;
	private ItemStack itemStackOffHand = ItemStack.EMPTY;

	public final int staminaMax = 50;
	public int staminaCurrent = 50;

	public void tick() {
		if (staminaCurrent < staminaMax) {
			staminaCurrent++;
		}
		++this.ticksSinceLastSwingOffhand;
		ItemStack mainHand = living.getHeldItemMainhand();
		ItemStack offHand = living.getHeldItemOffhand();
		if (!ItemStack.areItemStacksEqual(this.itemStackOffHand, offHand)) {
			if (!ItemStack.areItemsEqualIgnoreDurability(this.itemStackOffHand, offHand)) {
				resetCooldownOffhand();
			}
			itemStackOffHand = offHand.copy();
		}
		int mainWeight = getItemWeight(mainHand);
		int offWeight = getItemWeight(offHand);
		float overWeight = Float.NaN;
		if (mainWeight != 1 && offWeight != 1) {
			int totalweight = mainWeight + offWeight;
			if (totalweight > 95) {
				overWeight = -0.55F;
			} else if (totalweight > 75) {
				overWeight = -0.40F;
			} else if (totalweight > 55) {
				overWeight = -0.25F;
			} else if (totalweight > 35) {
				overWeight = -0.12F;
			}
		}
		IAttributeInstance attackSpeed = living.getAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		attackSpeed.removeModifier(WEIGHT_UUID);
		if (!Float.isNaN(overWeight)) {
			attackSpeed.applyModifier(new AttributeModifier(WEIGHT_UUID, "anduril.dualweight", overWeight, Operation.MULTIPLY_TOTAL));
		}
	}

	public float getCooledAttackStrengthOffhand(float adjustTicks) {
		return MathHelper.clamp(((float)ticksSinceLastSwingOffhand + adjustTicks) / getCooldownPeriodOffhand(), 0.0F, 1.0F);
	}

	public void resetCooldownOffhand() {
		ticksSinceLastSwingOffhand = 0;
	}

	public int getItemWeight(ItemStack stack) {
		if (stack.isEmpty()) {
			return 1;
		}
		Item item = stack.getItem();
		if (item.isShield(stack, living)) {
			return 15;
		} else if (item instanceof AxeItem) {
			return 50;
		} else if (item instanceof PickaxeItem) {
			return 40;
		} else if (item instanceof ShovelItem) {
			return 30;
		} else if (item instanceof SwordItem) {
			return 20;
		} else if (item instanceof HoeItem) {
			return 10;
		} else {
			return 5;
		}
	}

	public float tryConsumeStamina(int amount) {
		if (amount <= staminaCurrent) {
			staminaCurrent -= amount;
			return -0.00F;
		} else {
			int result = staminaCurrent - amount;
			if (result > -staminaMax/2) {
				staminaCurrent = result;
				return -0.33F;
			} else if (result > -staminaMax) {
				staminaCurrent = result;
				return -0.67F;
			} else {
				return -1.10F; // Ensure that nothing actually happens
			}
		}
	}

	public boolean isPlayer() {
		return living instanceof PlayerEntity;
	}

	// Only call if isPlayer()!!!!!!!!!!!!!!!!
	public float getCooldownPeriodOffhand() {
		swapHandsAndAttributes();
		float ret = ((PlayerEntity)living).getCooldownPeriod();
		swapHandsAndAttributes();
		return ret;
	}

	// Only call if isPlayer()!!!!!!!!!!!!!!!!
	public void attackTargetEntityWithCurrentItemOffhand(Entity target) {
		swapHandsAndAttributes();
		int hold = ticksSinceLastSwing.get(living).intValue();
		ticksSinceLastSwing.set(living, ticksSinceLastSwingOffhand);
		((PlayerEntity)living).attackTargetEntityWithCurrentItem(target);
		ticksSinceLastSwing.set(living, hold);
		if (target.canBeAttackedWithItem() && target.hitByEntity(living)) {
			resetCooldownOffhand();
		}
		swapHandsAndAttributes();
	}

	public void runAsSwapped(Runnable runnable) {
		swapHandsAndAttributes();
		try {
			runnable.run();
		} finally {
			swapHandsAndAttributes();
		}
	}

	public <T> T getAsSwapped(Supplier<T> supplier) {
		swapHandsAndAttributes();
		T ret;
		try {
			ret = supplier.get();
		} finally {
			swapHandsAndAttributes();
		}
		return ret;
	}

	public void swapHandsAndAttributes() {
		ItemStack mainhand = living.getHeldItem(Hand.MAIN_HAND);
        ItemStack offhand = living.getHeldItem(Hand.OFF_HAND);
        if (isPlayer()) {
        	PlayerEntity player = (PlayerEntity)living;
        	int current = player.inventory.currentItem;
        	player.inventory.mainInventory.set(current, offhand);
        	player.inventory.offHandInventory.set(0, mainhand);
        } else {
	        living.setHeldItem(Hand.OFF_HAND, mainhand);
	        living.setHeldItem(Hand.MAIN_HAND, offhand);
        }
        living.getAttributes().removeAttributeModifiers(mainhand.getAttributeModifiers(EquipmentSlotType.MAINHAND));
        living.getAttributes().applyAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlotType.MAINHAND));
	}

	@SubscribeEvent
	public static void playerTickPost(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			DualWield.of(event.player).tick();
		}
	}

	// Client only

	@OnlyIn(Dist.CLIENT)
	public static final class Client {

		private Client() {}

		public static final Client INSTANCE = new Client();
		private final Minecraft mc = Minecraft.getInstance();

		public boolean isAttackMode() {
			return AndurilGameRules.Client.client_dualWielding && SubmodClient.INSTANCE.toggle_attack_mode.isKeyDown();
		}

		public RayTraceResult objectMouseOverOffhand = null;
		public Entity pointedEntityOffhand = null;

		// Intercept click attacks
		@SubscribeEvent
		public void playerRightClick(InputEvent.ClickInputEvent event) {
			if (!AndurilGameRules.Client.client_dualWielding) return;
			DualWield dw = DualWield.of(mc.player);
			if (event.isAttack()) { // Left clicks
				if (mc.objectMouseOver != null) {
					switch(mc.objectMouseOver.getType()) {
					case ENTITY:
						event.setSwingHand(false);
						event.setCanceled(true);
						if (-1.0F < dw.tryConsumeStamina(dw.getItemWeight(mc.player.getHeldItemMainhand()))) {
							attackEntityMainhand(mc.player, ((EntityRayTraceResult)mc.objectMouseOver).getEntity());
							mc.player.swingArm(Hand.MAIN_HAND);
						}
						break;
					case MISS:
						event.setSwingHand(false);
						event.setCanceled(true);
						if (-1.0F < dw.tryConsumeStamina(dw.getItemWeight(mc.player.getHeldItemMainhand()))) {
							attackEntityMainhand(mc.player, null);
							mc.player.swingArm(Hand.MAIN_HAND);
						}
						break;
					case BLOCK: break;
					default: break;
					}
				}
			} else if (isAttackMode() && event.isUseItem()) { // Right clicks
				if (objectMouseOverOffhand != null) {
					switch(objectMouseOverOffhand.getType()) {
					case ENTITY:
						event.setSwingHand(false);
						event.setCanceled(true);
						if (-1.0F < dw.tryConsumeStamina(dw.getItemWeight(mc.player.getHeldItemOffhand()))) {
							attackEntityOffhand(mc.player, ((EntityRayTraceResult)objectMouseOverOffhand).getEntity());
							mc.player.swingArm(Hand.OFF_HAND);
						}
						break;
					case MISS:
						event.setSwingHand(false);
						event.setCanceled(true);
						if (-1.0F < dw.tryConsumeStamina(dw.getItemWeight(mc.player.getHeldItemOffhand()))) {
							attackEntityOffhand(mc.player, null);
							mc.player.swingArm(Hand.OFF_HAND);
						}
						break;
					case BLOCK: break;
					default: break;
					}
				}
			}
		}

		// Force the player to click
		@SubscribeEvent
		public void clientTick(TickEvent.ClientTickEvent event) {
			if (mc.player == null) return;
			if (event.phase == TickEvent.Phase.START) {
				DualWield dw = DualWield.of(mc.player);
				dw.runAsSwapped(() -> {
					mc.gameRenderer.getMouseOver(1.0F);
					objectMouseOverOffhand = mc.objectMouseOver;
					pointedEntityOffhand = mc.pointedEntity;
				});
			}
			if (event.phase == TickEvent.Phase.END) {
				DualWield dw = DualWield.of(mc.player);
				if (isAttackMode()) {
					rightClickDelayTimer.set(mc, 4);
				}
				prevPct = (float)dw.staminaCurrent / (float)dw.staminaMax;
			}
		}

		private static final MethodWrapper<Void> syncCurrentPlayItem = MethodWrapper.of(PlayerController.class, ASMAPI.mapMethod("func_78750_j"));
		private static final FieldWrapper<Integer> rightClickDelayTimer = FieldWrapper.of(Minecraft.class, ASMAPI.mapField("field_71467_ac"));

		// Model from PlayerController
		private void attackEntityMainhand(ClientPlayerEntity player, Entity target) {
			syncCurrentPlayItem.calls(mc.playerController);
			if (!mc.playerController.isSpectatorMode()) {
				if (target == null) {
					new C2SAttackEntity().with(Hand.MAIN_HAND).sendToServer(Anduril.instance().network);
				} else {
					new C2SAttackEntity().with(target, Hand.MAIN_HAND).sendToServer(Anduril.instance().network);
					player.attackTargetEntityWithCurrentItem(target);
				}
				player.resetCooldown();
			}
		}
		
		private void attackEntityOffhand(ClientPlayerEntity player, Entity target) {
			syncCurrentPlayItem.calls(mc.playerController);
			DualWield dw = DualWield.of(player);
			if (!mc.playerController.isSpectatorMode()) {
				if (target == null) {
					new C2SAttackEntity().with(Hand.OFF_HAND).sendToServer(Anduril.instance().network);
				} else {
					new C2SAttackEntity().with(target, Hand.OFF_HAND).sendToServer(Anduril.instance().network);
					dw.attackTargetEntityWithCurrentItemOffhand(target);
				}
				dw.resetCooldownOffhand();
			}
		}

		// Attack cooldown render

		@SubscribeEvent
		public void renderAttackTime(RenderGameOverlayEvent.Pre event) {
			if (!AndurilGameRules.Client.client_dualWielding) return;
			if (isAttackMode() && event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
				event.setCanceled(true);
				mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
				RenderSystem.enableBlend();
		        RenderSystem.enableAlphaTest();
		        renderAttackIndicator(event.getPartialTicks());
			}
		}

		private float prevPct = 1.0F;
		private void renderAttackIndicator(float partialTicks) {
			GameSettings settings = mc.gameSettings;
			int scaledWidth = mc.getMainWindow().getScaledWidth();
			int scaledHeight = mc.getMainWindow().getScaledHeight();
			IngameGui gui = mc.ingameGUI;
			if (settings.thirdPersonView == 0) {
				if (mc.playerController.getCurrentGameType() != GameType.SPECTATOR || this.isTargetNamedMenuProvider(mc.objectMouseOver) || this.isTargetNamedMenuProvider(objectMouseOverOffhand)) {
					if (settings.showDebugInfo && !settings.hideGUI && !mc.player.hasReducedDebug() && !settings.reducedDebugInfo) {
						RenderSystem.pushMatrix();
						RenderSystem.translatef(scaledWidth/2, scaledHeight/2, gui.getBlitOffset());
						ActiveRenderInfo renderinfo = mc.gameRenderer.getActiveRenderInfo();
						RenderSystem.rotatef(renderinfo.getPitch(), -1.0F, 0.0F, 0.0F);
						RenderSystem.rotatef(renderinfo.getYaw(), 0.0F, 1.0F, 0.0F);
						RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
						RenderSystem.renderCrosshair(10);
						RenderSystem.popMatrix();
					} else {
						RenderSystem.blendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
						int i = 15;
						// Color red so it's obvious you're in attack mode
						if (isAttackMode()) RenderSystem.color3f(1.00F, 0.15F, 0.3F);
						gui.blit((scaledWidth - i) / 2, (scaledHeight - i) / 2, 0, 0, i, i);
						RenderSystem.color3f(1.00F, 1.00F, 1.00F);
						if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
							float coolMain = mc.player.getCooledAttackStrength(0.0F);
							boolean flagMain = false;
							if (mc.pointedEntity != null && mc.pointedEntity instanceof LivingEntity && coolMain >= 1.0F) {
								flagMain = mc.player.getCooldownPeriod() > 5.0F;
								flagMain = flagMain & mc.pointedEntity.isAlive();
							}
							DualWield dw = DualWield.of(mc.player);
							float coolOff = dw.getCooledAttackStrengthOffhand(0.0F);
							boolean flagOff = false;
							if (pointedEntityOffhand != null && pointedEntityOffhand instanceof LivingEntity && coolOff >= 1.0F) {
								flagOff = dw.getCooldownPeriodOffhand() > 5.0F;
								flagOff = flagOff & pointedEntityOffhand.isAlive();
							}
							int x = scaledWidth / 2 - 8;
							int y = scaledHeight / 2 - 7 + 11;
							// account for people who have main and off hands swapped
					        boolean mainIsRight = mc.gameSettings.mainHand == HandSide.RIGHT;
					        int iconOffset = 15;
					        RenderSystem.pushMatrix();
					        // render main
					        RenderSystem.translatef(mainIsRight ? iconOffset : -iconOffset, 0, 0);
					        renderAttack(gui, x, y, flagMain, coolMain);
					        // render off
					        RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
					        RenderSystem.translatef(mainIsRight ? 2 * iconOffset : -2 * iconOffset, 0, 0);
					        RenderSystem.translatef(-scaledWidth, 0.0F, 0.0F);
					        renderAttack(gui, x, y, flagOff, coolOff);
					        RenderSystem.popMatrix();
					        float pct = Interpolation.linear(partialTicks, prevPct, (float)dw.staminaCurrent / (float)dw.staminaMax);
					        RenderSystem.disableTexture();
					        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
					        // Stamina bar
					        int barWidth = 40;
					        int barX = (scaledWidth - barWidth)/2;
					        int barYFine = scaledHeight/2 - 10;
					        int barYEx = barYFine + 1;
					        int offsetFine = pct > 0 ? (int)(barWidth * pct) : 0;
					        int offsetEx = pct < 0 ? (int)(barWidth * pct) : 0;
					        RenderSystem.color3f(0.5F, 0.5F, 0.5F);
					        gui.blit(barX + offsetFine, barYFine, 0, 0, barWidth - offsetFine, 1);
				        	RenderSystem.disableBlend();
					        if (pct < -0.5F) {
					        	RenderSystem.color3f(1.0F, 0.3F, 0.3F);
					        } else {
					        	RenderSystem.color3f(1.0F, 0.6F, 0.6F);
					        }
					        gui.blit(barX, barYEx, 0, 0, barWidth + offsetEx, 1);
					        RenderSystem.enableBlend();
					        RenderSystem.enableTexture();
						}
					}
				}
			}
		}

		private boolean isTargetNamedMenuProvider(RayTraceResult rayTraceIn) {
			if (rayTraceIn == null) {
				return false;
			} else if (rayTraceIn.getType() == RayTraceResult.Type.ENTITY) {
				return ((EntityRayTraceResult) rayTraceIn).getEntity() instanceof INamedContainerProvider;
			} else if (rayTraceIn.getType() == RayTraceResult.Type.BLOCK) {
				BlockPos blockpos = ((BlockRayTraceResult) rayTraceIn).getPos();
				World world = mc.world;
				return world.getBlockState(blockpos).getContainer(world, blockpos) != null;
			} else {
				return false;
			}
		}

		private void renderAttack(IngameGui gui, int x, int y, boolean flag, float cool) {
			if (flag) {
				gui.blit(x, y, 68, 94, 16, 16);
			} else if (cool < 1.0F) {
				gui.blit(x, y, 36, 94, 16, 4);
				gui.blit(x, y, 52, 94, (int)(cool * 17.0F), 4);
			}
		}

	}

}
