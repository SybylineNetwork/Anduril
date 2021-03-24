package siege.common.zone;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import siege.common.siege.Siege;

public abstract class Zone {

	public Zone() {}
	
	public Zone(int x, int y, int z, int size) {
		box = new AxisAlignedBB((double)(x - size) + 0.5, (double)y - 1, (double)(z - size) + 0.5, (double)(x + size) + 0.5, y + 1, (double)(z + size) + 0.5);
	}

	public AxisAlignedBB box;
	public Siege siege;
	
	public void fromNBT(Siege siege, CompoundNBT nbt) {
		this.siege = siege;
		box = new AxisAlignedBB(nbt.getDouble("minx"), nbt.getDouble("miny"), nbt.getDouble("minz"), nbt.getDouble("maxx"), nbt.getDouble("maxy"), nbt.getDouble("maxz"));
		this.fromNBT0(nbt);
	}
	
	public CompoundNBT toNBT(Siege siege, CompoundNBT nbt) {
		this.siege = siege;
		nbt.putDouble("minx", box.minX);
		nbt.putDouble("miny", box.minY);
		nbt.putDouble("minz", box.minY);
		nbt.putDouble("maxx", box.maxX);
		nbt.putDouble("maxy", box.maxY);
		nbt.putDouble("maxz", box.maxZ);
		this.toNBT0(nbt);
		return nbt;
	}
	
	protected abstract void fromNBT0(CompoundNBT nbt);
	protected abstract void toNBT0(CompoundNBT nbt);

	public abstract boolean tick();
	
}
