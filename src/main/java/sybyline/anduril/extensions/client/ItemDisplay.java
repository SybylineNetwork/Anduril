package sybyline.anduril.extensions.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import sybyline.anduril.util.math.Interpolation;

@SuppressWarnings("deprecation")
public final class ItemDisplay {

	ItemDisplay() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Post event) {
		PlayerEntity player = event.getPlayer();
		Minecraft mc = Minecraft.getInstance();
		
		MatrixStack matrix = event.getMatrixStack();
		IRenderTypeBuffer buffers = event.getBuffers();
		float rot = -Interpolation.linear(event.getPartialRenderTick(), player.prevRenderYawOffset, player.renderYawOffset);
		Quaternion playerRotation = new Quaternion(0, rot, 0, true);
		boolean sneak = player.isShiftKeyDown();
		Quaternion swordRotation = sneak
				? new Quaternion(-135, 0, 0, true)
				: new Quaternion(-155, 0, 0, true);
		Quaternion shieldRotation = sneak
				? new Quaternion(20, 90, 0, true)
				: new Quaternion(0, 90, 0, true);
		int light = event.getLight();
		
		ItemStack held = player.getHeldItemMainhand();
		ItemStack shield = player.getHeldItemOffhand();
		
		if (held.isEmpty() || !(held.getItem() instanceof SwordItem)) {
			for (int i = 0; i < 9; i++) {
				ItemStack slot = player.inventory.getStackInSlot(i);
				if (!slot.isEmpty() && slot.getItem() instanceof SwordItem && player.inventory.currentItem != i) {
					matrix.push();
						matrix.rotate(playerRotation);
						if (sneak) matrix.translate(0.27D, 0.70D, 0.02D);
						else       matrix.translate(0.27D, 0.70D, 0.17D);
						matrix.rotate(swordRotation);
						matrix.scale(0.9F, 0.9F, 0.9F);
						mc.getItemRenderer().renderItem(
							slot, TransformType.THIRD_PERSON_RIGHT_HAND,
							light, OverlayTexture.NO_OVERLAY,
							matrix, buffers
						);
					matrix.pop();
				}
			}
		}
		
		if (shield.isEmpty() || !(shield.isShield(player))) {
			for (int i = 0; i < 9; i++) {
				ItemStack slot = player.inventory.getStackInSlot(i);
				if (!slot.isEmpty() && slot.isShield(player) && player.inventory.currentItem != i) {
					matrix.push();
						matrix.rotate(playerRotation);
						if (sneak) matrix.translate(-0.23D, 0.8D, -0.2D);
						else       matrix.translate(-0.23D, 0.95D, 0.0D);
						matrix.rotate(shieldRotation);
						matrix.scale(0.9F, 0.9F, 0.9F);
						mc.getItemRenderer().renderItem(
							slot, TransformType.THIRD_PERSON_RIGHT_HAND,
							light, OverlayTexture.NO_OVERLAY,
							matrix, buffers
						);
					matrix.pop();
				}
			}
		}
		
	}

}
