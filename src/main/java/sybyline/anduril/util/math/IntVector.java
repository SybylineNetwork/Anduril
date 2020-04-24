package sybyline.anduril.util.math;

import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import sybyline.anduril.scripting.api.common.IScriptCommandFormattable;
import sybyline.satiafenris.ene.ScriptExtensionSerial;
import sybyline.satiafenris.ene.ScriptExtensionType;
import sybyline.satiafenris.ene.ScriptExtensionTypeSerial;

public class IntVector implements ScriptExtensionSerial<IntVector, CollectionNBT<?>>,IScriptCommandFormattable,IVector {

	public static final IntVector zero() {
		return new IntVector(0, 0, 0);
	}

	private final int x;
	private final int y;
	private final int z;

	public IntVector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector asVector() {
		return new Vector(x + 0.5D, y + 0.5D, z + 0.5D);
	}

	public IntVector asIntVector() {
		return this;
	}

	public int x() {
		return x;
	}

	public int y() {
		return x;
	}

	public int z() {
		return x;
	}

	public IntVector x(int x) {
		return new IntVector(x, y, z);
	}

	public IntVector y(int y) {
		return new IntVector(x, y, z);
	}

	public IntVector z(int z) {
		return new IntVector(x, y, z);
	}

	public IntVector subtract(IntVector vec) {
		return this.subtract(vec.x, vec.y, vec.z);
	}

	public IntVector subtract(int x, int y, int z) {
		return this.add(-x, -y, -z);
	}

	public IntVector subtractReverse(IntVector vec) {
		return new IntVector(vec.x - this.x, vec.y - this.y, vec.z - this.z);
	}

	public IntVector add(IntVector vec) {
		return this.add(vec.x, vec.y, vec.z);
	}

	public IntVector add(int x, int y, int z) {
		return new IntVector(this.x + x, this.y + y, this.z + z);
	}

	public IntVector inverse() {
		return new IntVector(-x, -y, -z);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof IntVector)) {
			return false;
		} else {
			IntVector other = (IntVector) object;
			if (Integer.compare(other.x, this.x) != 0) {
				return false;
			} else if (Integer.compare(other.y, this.y) != 0) {
				return false;
			} else {
				return Integer.compare(other.z, this.z) == 0;
			}
		}
	}

	// Format interchange

	public int hashCode() {
		long j = Double.doubleToLongBits(this.x);
		int i = (int) (j ^ j >>> 32);
		j = Double.doubleToLongBits(this.y);
		i = 31 * i + (int) (j ^ j >>> 32);
		j = Double.doubleToLongBits(this.z);
		i = 31 * i + (int) (j ^ j >>> 32);
		return i;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	@Override
	public void toCommandString(StringBuilder string) {
		string.append(x).append(' ').append(y).append(' ').append(z);
	}

	public static final ScriptExtensionTypeSerial<IntVector, CollectionNBT<?>> EXTENSION = ScriptExtensionTypeSerial.register((nbt) -> {
		if (nbt == null)
			return null;
		if (!(nbt instanceof CollectionNBT))
			return null;
		CollectionNBT<?> list = (CollectionNBT<?>)nbt;
		if (list.size() != 3)
			return null;
		INBT xnbt = list.get(0);
		if (!(xnbt instanceof NumberNBT))
			return null;
		INBT ynbt = list.get(1);
		if (!(ynbt instanceof NumberNBT))
			return null;
		INBT znbt = list.get(2);
		if (!(znbt instanceof NumberNBT))
			return null;
		return new IntVector(((NumberNBT)xnbt).getInt(), ((NumberNBT)xnbt).getInt(), ((NumberNBT)xnbt).getInt());
	});

	@Override
	public ScriptExtensionType<IntVector> getTypifier() {
		return EXTENSION;
	}

	@Override
	public CollectionNBT<?> toNBT() {
		IntArrayNBT list = new IntArrayNBT(new int[3]);
		list.add(IntNBT.valueOf(x));
		list.add(IntNBT.valueOf(y));
		list.add(IntNBT.valueOf(z));
		return list;
	}

}
