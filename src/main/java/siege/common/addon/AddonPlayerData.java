package siege.common.addon;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import siege.common.siege.Siege;
import siege.common.siege.SiegePlayerData;

public class AddonPlayerData {
	
	@SuppressWarnings("unused")
	private final SiegePlayerData parent;
	public final Siege siege;
	public boolean isSiegeActive = false;
	public Vec3d joinedSiegePos = Vec3d.ZERO;
	public DimensionType joinedSiegeDim = DimensionType.OVERWORLD;
	public int personalscore = 0;
	
	public AddonPlayerData(SiegePlayerData parent, Siege siege) {
		this.parent = parent;
		this.siege = siege;
	}
	
	public void toNBT(CompoundNBT nbt) {
		nbt.putBoolean("VinyarionAddon_IsSiegeActive", this.isSiegeActive);
		nbt.putDouble("VinyarionAddon_JoinedSiegePosX", this.joinedSiegePos.x);
		nbt.putDouble("VinyarionAddon_JoinedSiegePosY", this.joinedSiegePos.y);
		nbt.putDouble("VinyarionAddon_JoinedSiegePosZ", this.joinedSiegePos.z);
		nbt.putString("VinyarionAddon_JoinedSiegePosDim", this.joinedSiegeDim.getRegistryName().toString());
		nbt.putInt("VinyarionAddon_PersonalScore", this.personalscore);
	}
	
	public void fromNBT(CompoundNBT nbt) {
		this.isSiegeActive = nbt.getBoolean("VinyarionAddon_IsSiegeActive");
		this.joinedSiegePos = new Vec3d(
			nbt.getDouble("VinyarionAddon_JoinedSiegePosX"), 
			nbt.getDouble("VinyarionAddon_JoinedSiegePosY"), 
			nbt.getDouble("VinyarionAddon_JoinedSiegePosZ")
		);
		this.joinedSiegeDim = DimensionType.byName(new ResourceLocation(nbt.getString("VinyarionAddon_JoinedSiegePosDim")));
		this.personalscore = nbt.getInt("VinyarionAddon_PersonalScore");
	}
	
}
