package siege.common.zone;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import static net.minecraft.util.text.TextFormatting.*;
import siege.common.mode.ModeCTF;
import siege.common.siege.Siege;
import siege.common.siege.SiegeTeam;

public class ZoneFlag extends Zone {

	public ZoneFlag() {}

	public ZoneFlag(SiegeTeam team, int x, int y, int z, int size) {
		super(x, y, z, size);
		owner = team;
	}

	public SiegeTeam owner;
	public boolean hasFlag = true;

	public boolean tick() {
		if(siege.getTicksRemaining() % 5 != 0) return false;
		for(Object o : siege.world().getEntitiesWithinAABB(PlayerEntity.class, box)) {
			PlayerEntity player = (PlayerEntity)o;
			if(siege.hasPlayer(player)) {
				if(siege.getPlayerTeam(player) == owner) {
					SiegeTeam carried = getCarriedFlag(siege, player);
					if(carried != null) {
						captureFlag((ModeCTF)siege.mode, player, owner, carried);
					}
				} else if(hasFlag) {
					pickupFlag((ModeCTF)siege.mode, player);
				}
			}
		}
		return false;
	}

	private void captureFlag(ModeCTF mode, PlayerEntity player, SiegeTeam winner, SiegeTeam loser) {
		siege.announceToAllPlayers(winner.color + winner.getTeamName() + GOLD + " has captured the flag of " + loser.color + loser.getTeamName() + GOLD + "!");
		int left = mode.pointsNeededToWin - winner.score - 1;
		if(left > 0) siege.announceToAllPlayers(winner.color + winner.getTeamName() + GOLD + " needs " + left + " more flags!");
		player.getPersistentData().remove("VinyarionAddon_Flag");
		mode.owners.get(loser).hasFlag = true;
		winner.score++;
		loser.antiscore++;
		siege.getPlayerData(player).addonData.personalscore++;
		siege.markDirty();
	}

	private void pickupFlag(ModeCTF mode, PlayerEntity player) {
		SiegeTeam team = siege.getTeam(player.getPersistentData().getString("VinyarionAddon_Flag"));
		player.getPersistentData().putString("VinyarionAddon_Flag", owner.getTeamName());
		if(team != null) {
			mode.owners.get(team).hasFlag = true;
			siege.announceToAllPlayers(YELLOW + player.getScoreboardName() + GOLD + " has dropped the flag of " + team.color + team.getTeamName() + GOLD + "!");
		}
		hasFlag = false;
		siege.announceToAllPlayers(YELLOW + player.getScoreboardName() + GOLD + " has picked up the flag of " + owner.color + owner.getTeamName() + GOLD + "!");
		siege.markDirty();
	}

	public static void dropFlag(ModeCTF mode, Siege siege, PlayerEntity player) {
		SiegeTeam team = siege.getTeam(player.getPersistentData().getString("VinyarionAddon_Flag"));
		player.getPersistentData().remove("VinyarionAddon_Flag");
		if(team != null) {
			mode.owners.get(team).hasFlag = true;
			siege.announceToAllPlayers(YELLOW + player.getScoreboardName() + GOLD + " has dropped the flag of " + team.color + team.getTeamName() + GOLD + "!");
		}
		siege.markDirty();
	}

	public static SiegeTeam getCarriedFlag(Siege siege, PlayerEntity player) {
		return siege.getTeam(player.getPersistentData().getString("VinyarionAddon_Flag"));
	}

	protected void fromNBT0(CompoundNBT nbt) {
		this.owner = this.siege.getTeam(nbt.getString("VinyarionAddon_TeamName"));
		this.hasFlag = nbt.getBoolean("VinyarionAddon_HasFlag");
	}

	protected void toNBT0(CompoundNBT nbt) {
		nbt.putString("VinyarionAddon_TeamName", this.owner.getTeamName());
		nbt.putBoolean("VinyarionAddon_HasFlag", this.hasFlag);
	}

}
