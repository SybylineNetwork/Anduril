package siege.common;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import siege.common.addon.AddonHooks;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;
import siege.common.siege.Siege;
import siege.common.siege.SiegeDatabase;
import siege.common.siege.SiegePlayerData;
import siege.common.siege.SiegeTeam;
import siege.common.siege.SiegeTerrainProtection;
import siege.common.siege.command.SiegeCommands;

public class EventHandler
{

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event)
	{
		SiegeModeMain.instance.server = event.getServer();
		KitDatabase.reloadAll();
		SiegeDatabase.reloadAll();
		SiegeCommands.register(event.getCommandDispatcher());
	}

	@SubscribeEvent
	public void onServerStopping(FMLServerStoppingEvent event)
	{
		KitDatabase.save();
		SiegeDatabase.save();
		SiegeModeMain.instance.server = null;
	}

	@SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		World world = event.world;
		
		if (world.isRemote)
		{
			return;
		}
		
		if (event.phase == Phase.START)
		{
			if (world.getDimension().getType() == DimensionType.OVERWORLD)
			{
				// loading?
			}
		}
			
		if (event.phase == Phase.END)
		{
			SiegeDatabase.updateActiveSieges(world);
			
			if (world.getDimension().getType() == DimensionType.OVERWORLD)
			{
				if (KitDatabase.anyNeedSave())
				{
					KitDatabase.save();
				}
				
				if (SiegeDatabase.anyNeedSave())
				{
					SiegeDatabase.save();
				}
			}
		}
	}

	@SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		PlayerEntity entityplayer = event.player;
		World world = entityplayer.world;
		
		if (world.isRemote)
		{
			return;
		}
			
		if (event.phase == Phase.END)
		{
			if (Siege.hasSiegeGivenKit(entityplayer) && SiegeDatabase.getActiveSiegeForPlayer(entityplayer) == null)
			{
				if (!entityplayer.isCreative())
				{
					Kit.clearPlayerInvAndKit(entityplayer);
				}
				Siege.setHasSiegeGivenKit(entityplayer, false);
				Siege.dispel(entityplayer);
			}
		}
	}
	
	@SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
		World world = entityplayer.world;
		
		if (!world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerLogin((ServerPlayerEntity)entityplayer);
			}
			// TODO : Vinyarion's addon start
			AddonHooks.playerLogsInActive(entityplayer, activeSiege);
			if(activeSiege == null) {
				for(SiegePlayerData data : SiegeDatabase.removeStale(entityplayer)) {
					AddonHooks.playerLogsInInactive(entityplayer, data.addonData.siege, data);
				}
			}
			// Addon end
		}
	}
	
	@SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
		World world = entityplayer.world;
		
		if (!world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerLogout((ServerPlayerEntity)entityplayer);
			}
			// TODO : Vinyarion's addon start
			siege.common.addon.AddonHooks.playerLogsOut(entityplayer, activeSiege);
			// Addon end
		}
	}
	
	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity entityplayer = (PlayerEntity)entity;
			if (!entityplayer.world.isRemote && !entityplayer.isCreative())
			{
				Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
				if (activeSiege != null)
				{
					SiegeTeam team = activeSiege.getPlayerTeam(entityplayer);
					
					Entity attacker = source.getTrueSource();
					if (attacker instanceof PlayerEntity)
					{
						PlayerEntity attackingPlayer = (PlayerEntity)attacker;
						
						if (attackingPlayer != entityplayer && team.containsPlayer(attackingPlayer) && !activeSiege.getFriendlyFire())
						{
							event.setCanceled(true);
							return;
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity entityplayer = (PlayerEntity)entity;
			if (!entityplayer.world.isRemote)
			{
				Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
				if (activeSiege != null)
				{
					activeSiege.onPlayerDeath(entityplayer, source);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
		if (!entityplayer.world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerRespawn(entityplayer);
			}
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
		if (!entityplayer.world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null && !entityplayer.isCreative())
			{
				Siege.warnPlayer(entityplayer, "You cannot drop items during a siege");
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingSpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		Vec3d pos = new Vec3d(event.getX(), event.getY(), event.getZ());
		
		List<Siege> siegesHere = SiegeDatabase.getActiveSiegesAtPosition(pos);
		for (Siege siege : siegesHere)
		{
			if (!siege.getMobSpawning())
			{
				event.setResult(Result.DENY);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event)
	{
		boolean rightClickBlock = event instanceof PlayerInteractEvent.RightClickBlock;
		boolean leftClickBlock = event instanceof PlayerInteractEvent.LeftClickBlock;
		
		if (!(rightClickBlock || leftClickBlock)) return;
		
		PlayerEntity entityplayer = event.getPlayer();
		World world = entityplayer.world;
		ItemStack itemstack = entityplayer.inventory.getCurrentItem();
		BlockPos pos = event.getPos();
//		Direction side = event.getFace();

		Item item = itemstack == null ? null : itemstack.getItem();
		boolean isPlacingItem = item != null && (item instanceof BlockItem || item instanceof BedItem);
		if ((rightClickBlock && isPlacingItem) || leftClickBlock)
		{
			BlockState block = world.getBlockState(pos);
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, pos))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
				event.setCanceled(true);
				
				if (block.getBlock() instanceof DoorBlock)
				{
					world.notifyBlockUpdate(pos, block, block, Constants.BlockFlags.DEFAULT_AND_RERENDER);
				}
				
				return;
			}
		}
		
		if (rightClickBlock)
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory)
			{
				if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, pos))
				{
					SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot interact with containers during the siege");
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
//		Block block = event.getState().getBlock();
		World world = (World) event.getWorld();
		BlockPos pos = event.getPos();
		
		if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, pos))
		{
			SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
			event.setCanceled(true);
			return;
		}
	}
	
	@SubscribeEvent
	public void onFillBucket(FillBucketEvent event)
	{
		PlayerEntity entityplayer = event.getPlayer();
		World world = event.getWorld();
		RayTraceResult target = event.getTarget();
		
		if (target.getType() == RayTraceResult.Type.BLOCK)
		{
			BlockPos pos = ((BlockRayTraceResult)target).getPos();
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, pos))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
    public void onEntityAttackedByPlayer(AttackEntityEvent event)
	{
		Entity entity = event.getTarget();
		World world = entity.world;
		PlayerEntity entityplayer = event.getPlayer();
		
		if (!(entity instanceof LivingEntity))
		{
			BlockPos pos = entity.getPosition();
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, pos))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot destroy non-living entities during the siege");
				event.setCanceled(true);
				return;
			}
		}
	}
}
