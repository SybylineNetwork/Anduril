package siege.common.rule;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import siege.common.siege.Siege;
import siege.common.siege.SiegeTeam;

public class RuleLivesTeam extends RuleLives {
	
	public RuleLivesTeam() {
		this.type = "VinyarionAddon_LivesTeam";
	}

	@Override
	public SiegeRule rule() {
		return SiegeRule.LIVES_TEAM;
	}
	
	public void playerDie(Siege siege, PlayerEntity player) {
		SiegeTeam team = siege.getPlayerTeam(player);
		if(this.lives <= team.getTeamDeaths()) {
			siege.leavePlayer((ServerPlayerEntity) player, true);
		}
	}
	
	public void playerJoin(Siege siege, PlayerEntity player) {
		SiegeTeam team = siege.getPlayerTeam(player);
		if(this.lives <= team.getTeamDeaths()) {
			siege.leavePlayer((ServerPlayerEntity) player, true);
			player.sendMessage(new StringTextComponent("This team ran out of lives, so you can not join it!"));
		}
	}
	
}