package siege.common.siege;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class BackupSpawnPoint
{
	public DimensionType dimension;
	public BlockPos spawnCoords;
	public boolean spawnForced;
	
	public BackupSpawnPoint(DimensionType dim, BlockPos coords, boolean forced)
	{
		dimension = dim;
		spawnCoords = coords;
		spawnForced = forced;
	}
}
