package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SiegeTerrainProtection
{
	private static final int MESSAGE_INTERVAL_SECONDS = 2;
	private static final Map<UUID, Long> lastPlayerMsgTimes = new HashMap<>();
	
	public static boolean isProtected(PlayerEntity entityplayer, World world, BlockPos pos)
	{
		if (!entityplayer.isCreative())
		{
			Vec3d vec = new Vec3d(pos).add(0.5, 0.5, 0.5);
			
			List<Siege> activeSieges = SiegeDatabase.getActiveSiegesAtPosition(vec);
			for (Siege siege : activeSieges)
			{
				if (siege.getTerrainProtect())
				{
					return true;
				}
			}
			
			List<Siege> inactiveSieges = SiegeDatabase.getInactiveSiegesAtPosition(vec);
			for (Siege siege : inactiveSieges)
			{
				if (siege.getTerrainProtectInactive())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static void warnPlayer(PlayerEntity entityplayer, String message)
	{
		UUID playerID = entityplayer.getUniqueID();
		long currentTimeMs = System.currentTimeMillis();
		boolean send = true;
		
		if (lastPlayerMsgTimes.containsKey(playerID))
		{
			long lastMsgTimeMs = lastPlayerMsgTimes.get(playerID);
			if (currentTimeMs - lastMsgTimeMs < MESSAGE_INTERVAL_SECONDS * 1000)
			{
				send = false;
			}
		}
		
		if (send)
		{
			Siege.warnPlayer(entityplayer, message);
			lastPlayerMsgTimes.put(playerID, currentTimeMs);
		}
	}
}
