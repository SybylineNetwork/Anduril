package siege.common.rule;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import siege.common.siege.Siege;
import siege.common.siege.SiegePlayerData;

public class RuleLivesPlayer extends RuleLives {
	
	public RuleLivesPlayer() {
		this.type = "VinyarionAddon_LivesPlayer";
	}

	@Override
	public SiegeRule rule() {
		return SiegeRule.LIVES_PLAYER;
	}
	
	public void playerDie(Siege siege, PlayerEntity player) {
		SiegePlayerData pd = siege.getPlayerData(player);
		if(this.lives <= pd.getDeaths()) {
			siege.leavePlayer((ServerPlayerEntity) player, true);
		}
	}
	
	public void playerJoin(Siege siege, PlayerEntity player) {
		SiegePlayerData pd = siege.getPlayerData(player);
		if(this.lives <= pd.getDeaths()) {
			siege.leavePlayer((ServerPlayerEntity) player, true);
			player.sendMessage(new StringTextComponent("You have run out of lives, so you can not join this siege again!"));
		}
	}
	
}