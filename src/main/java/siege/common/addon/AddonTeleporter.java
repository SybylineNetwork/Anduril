package siege.common.addon;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.chunk.Chunk;

public class AddonTeleporter extends Teleporter {

	public AddonTeleporter(ServerWorld worldserver) {
		super(worldserver);
	}

	public boolean placeInPortal(Entity entity, float f) {
		int y = getTrueTopBlock(entity.world, 0, 0);
		entity.setLocationAndAngles(0.5, y + 1.0, 0.5, entity.rotationYaw, 0.0f);
		return true;
	}

	public static int getTrueTopBlock(World world, int i, int k) {
		final Chunk chunk = world.getChunkProvider().getChunk(i >> 4, k >> 4, true);
		for(int j = chunk.getTopFilledSegment() + 15; j > 0; --j) {
			BlockPos pos = new BlockPos(i, j, k);
			final BlockState block = world.getBlockState(pos);
			if(block.getMaterial().blocksMovement() && block.getMaterial() != Material.LEAVES && !block.isFoliage(world, pos)) {
				return j + 1;
			}
		}
		return -1;
	}

}