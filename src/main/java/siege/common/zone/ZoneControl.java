package siege.common.zone;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandSource;
import static net.minecraft.util.text.TextFormatting.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import siege.common.siege.SiegeTeam;

public class ZoneControl extends Zone implements Comparable<ZoneControl> {

	public ZoneControl() {}

	public ZoneControl(String name, int x, int y, int z, int size, int order) {
		super(x, y, z, size);
		this.name = name;
		this.order = order;
	}

	public String name;
	public int order;
	public SiegeTeam occupiers = null;
	public SiegeTeam attackers = null;
	public int ticksHeld = 0;
	public int ticksTillOccupation = 120 / 5;

	protected void fromNBT0(CompoundNBT nbt) {
		name = nbt.getString("name");
		order = nbt.getInt("order");
		occupiers = this.siege.getTeam(nbt.getString("occupiers"));
		attackers = this.siege.getTeam(nbt.getString("attackers"));
		ticksHeld = nbt.getInt("ticksHeld");
		ticksTillOccupation = nbt.getInt("ticksTillOccupation");
	}

	protected void toNBT0(CompoundNBT nbt) {
		nbt.putString("name", name);
		nbt.putInt("order", order);
		nbt.putString("occupiers", occupiers == null ? "" : (occupiers.color + occupiers.getTeamName()));
		nbt.putString("attackers", attackers == null ? "" : (attackers.color + attackers.getTeamName()));
		nbt.putInt("ticksHeld", ticksHeld);
		nbt.putInt("ticksTillOccupation", ticksTillOccupation);
	}

	public boolean tick() {
		if(siege.getTicksRemaining() % 5 != 0) return false;
		int[] teams = new int[siege.listTeamNames().size()];
		for(Object o : siege.world().getEntitiesWithinAABB(PlayerEntity.class, box)) {
			PlayerEntity player = (PlayerEntity)o;
			if(siege.hasPlayer(player)) {
				teams[siege.listTeamNames().indexOf(siege.getPlayerTeam(player).getTeamName())]++;
			}
		}
		int high = 0;
		for(int i = 1; i < teams.length; i++) {
			if(teams[high] < teams[i]) {
				high = i;
			}
		}
		List<SiegeTeam> highs = Lists.newArrayList();
		for(int i = 0; i < teams.length; i++) {
			if(teams[high] == teams[i]) {
				highs.add(siege.getTeam(siege.listTeamNames().get(i)));
			}
		}
		if(teams[high] == 0) return false;
		boolean captured = false;
		if(highs.size() == 1) {
			if(highs.get(0) == occupiers) {
				ticksHeld = 0;
				attackers = null;
			} else if(highs.get(0) == attackers) {
				ticksHeld++;
				if(ticksHeld >= ticksTillOccupation) {
					occupied(attackers);
					captured = true;
				}
				for(Object o : siege.world().getEntitiesWithinAABB(PlayerEntity.class, box)) {
					PlayerEntity player = (PlayerEntity)o;
					if(siege.getPlayerTeam(player) == attackers) {
						siege.getPlayerData(player).addonData.personalscore += (captured ? 20 : 1);
					}
				}
			} else {
				ticksHeld = 0;
				attacked(highs.get(0));
			}
		} else if(highs.contains(occupiers) && highs.contains(attackers)) {
			ticksHeld = 0;
		}
		siege.markDirty();
		return captured;
	}

	private void attacked(SiegeTeam team) {
		siege.announceToAllPlayers(team.color + team.getTeamName() + GOLD + " is attacking " + WHITE + name + GOLD + "!");
		attackers = team;
	}

	private void occupied(SiegeTeam team) {
		siege.announceToAllPlayers(team.color + team.getTeamName() + GOLD + " has taken " + WHITE + name + GOLD + "!");
		occupiers = team;
		attackers = null;
	}

	public void setValue(CommandSource sender, int val) {
		ticksTillOccupation = val * 4;
	}

	@Override
	public int compareTo(ZoneControl other) {
		return other.order - this.order;
	}

}
