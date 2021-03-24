package siege.common.addon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import siege.common.kit.Kit;
import siege.common.mode.ModeCTF;
import siege.common.siege.BackupSpawnPoint;
import siege.common.siege.Siege;
import siege.common.siege.SiegePlayerData;
import siege.common.siege.SiegeTeam;
import siege.common.zone.ZoneFlag;

public class AddonHooks {

	public static ThreadLocal<SiegePlayerData> lastLeft = ThreadLocal.withInitial(()->null);

	public static void playerJoinsSiege(PlayerEntity player, Siege siege, SiegeTeam team, Kit kit) {
		if(siege == null) return;
		AddonPlayerData pd = siege.getPlayerData(player).addonData;
		pd.isSiegeActive = true;
		pd.joinedSiegePos = player.getPositionVec();
		pd.joinedSiegeDim = player.dimension;
		siege.mode.ruleHandler.playerJoin(siege, player);
	}

	public static void playerLeavesSiege(ServerPlayerEntity player, Siege siege, SiegeTeam team) {
		if(siege == null) return;
		AddonPlayerData pd = siege.getPlayerData(player).addonData;
		lastLeft.set(siege.getPlayerData(player));
		siege.mode.ruleHandler.playerLeave(siege, player);
		pd.isSiegeActive = false;
	}

	public static void playerLogsInActive(PlayerEntity player, Siege siege) {
		if(siege == null) return;
		siege.mode.ruleHandler.playerLogin(siege, player);
	}

	public static void playerLogsInInactive(PlayerEntity player, Siege siege, SiegePlayerData old) {
		if(siege == null) return;
		lastLeft.set(old);
		if(!siege.isActive() || siege.isDeleted()) {
			old.updateSiegeScoreboard((ServerPlayerEntity)player, false);
			BackupSpawnPoint bsp = old.getBackupSpawnPoint();
			if (bsp != null) {
				player.setSpawnPoint(bsp.spawnCoords, bsp.spawnForced, false, bsp.dimension);
			}
			Kit.clearPlayerInvAndKit(player);
			if (siege.getDispelEnd()) {
				Siege.dispel(player);
			}
		}
	}

	public static void playerLogsOut(PlayerEntity player, Siege siege) {
		if(siege == null) return;
		siege.mode.ruleHandler.playerLogout(siege, player);
	}

	public static void playerDies(PlayerEntity player, Siege siege, SiegeTeam team) {
		if(siege == null) return;
		if(siege.mode instanceof ModeCTF) {
			ZoneFlag.dropFlag((ModeCTF)siege.mode, siege, player);
		}
		siege.mode.ruleHandler.playerDie(siege, player);
	}

	public static void playerRespawns(PlayerEntity player, Siege siege, SiegeTeam team) {
		if(siege == null) return;
		siege.mode.ruleHandler.playerRespawn(siege, player);
	}

}
