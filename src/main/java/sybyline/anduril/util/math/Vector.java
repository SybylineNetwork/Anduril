package sybyline.anduril.util.math;

import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.math.MathHelper;
import sybyline.anduril.scripting.api.common.IScriptCommandFormattable;
import sybyline.anduril.util.Util;
import sybyline.satiafenris.ene.ScriptExtensionSerial;
import sybyline.satiafenris.ene.ScriptExtensionType;
import sybyline.satiafenris.ene.ScriptExtensionTypeSerial;

public class Vector implements ScriptExtensionSerial<Vector, ListNBT>,IScriptCommandFormattable,IVector {

	public static final Vector zero() {
		return new Vector(0.0D, 0.0D, 0.0D);
	}

//	public static Vector from_euler(Vec2f rot) {
//		return from_euler(rot.x, rot.y);
//	}

	public static Vector from_euler(float pitch, float yaw) {
		float f1 = MathHelper.cos(-yaw * Util.Numbers.DEGREES_F - Util.Numbers.PI_F);
		float f2 = MathHelper.sin(-yaw * Util.Numbers.DEGREES_F - Util.Numbers.PI_F);
		float f3 = -MathHelper.cos(-pitch * Util.Numbers.DEGREES_F);
		float f4 = MathHelper.sin(-pitch * Util.Numbers.DEGREES_F);
		return new Vector((f2 * f3), f4, (f1 * f3));
	}

	private final double x;
	private final double y;
	private final double z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector asVector() {
		return this;
	}

	public IntVector asIntVector() {
		return new IntVector(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}

	public double x() {
		return x;
	}

	public double y() {
		return x;
	}

	public double z() {
		return x;
	}

	public Vector x(double x) {
		return new Vector(x, y, z);
	}

	public Vector y(double y) {
		return new Vector(x, y, z);
	}

	public Vector z(double z) {
		return new Vector(x, y, z);
	}

	public Vector normalize() {
		double len_sq = (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		return len_sq < 1.0E-4D ? zero() : new Vector(this.x / len_sq, this.y / len_sq, this.z / len_sq);
	}

	public double dotProduct(Vector other) {
		return x * other.x + y * other.y + z * other.z;
	}

	public Vector crossProduct(Vector other) {
		return new Vector(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
	}

	public Vector subtract(Vector vec) {
		return this.subtract(vec.x, vec.y, vec.z);
	}

	public Vector subtract(double x, double y, double z) {
		return this.add(-x, -y, -z);
	}

	public Vector subtractReverse(Vector vec) {
		return new Vector(vec.x - this.x, vec.y - this.y, vec.z - this.z);
	}

	public Vector add(Vector vec) {
		return this.add(vec.x, vec.y, vec.z);
	}

	public Vector add(double x, double y, double z) {
		return new Vector(this.x + x, this.y + y, this.z + z);
	}

	public double distanceTo(Vector vec) {
		return (double) MathHelper.sqrt(squareDistanceTo(vec));
	}

	public double squareDistanceTo(Vector vec) {
		return squareDistanceTo(vec.x, vec.y, vec.z);
	}

	public double squareDistanceTo(double xIn, double yIn, double zIn) {
		double dx = xIn - this.x;
		double dy = yIn - this.y;
		double dz = zIn - this.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public Vector scale(double factor) {
		return this.mul(factor, factor, factor);
	}

	public Vector inverse() {
		return this.scale(-1.0D);
	}

	public Vector mul(Vector p_216369_1_) {
		return this.mul(p_216369_1_.x, p_216369_1_.y, p_216369_1_.z);
	}

	public Vector mul(double factorX, double factorY, double factorZ) {
		return new Vector(this.x * factorX, this.y * factorY, this.z * factorZ);
	}

	public double length() {
		return (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public double lengthSquared() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public boolean equals(Object p_equals_1_) {
		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof Vector)) {
			return false;
		} else {
			Vector vec3d = (Vector) p_equals_1_;
			if (Double.compare(vec3d.x, this.x) != 0) {
				return false;
			} else if (Double.compare(vec3d.y, this.y) != 0) {
				return false;
			} else {
				return Double.compare(vec3d.z, this.z) == 0;
			}
		}
	}

	public Vector rotatePitch(float pitch) {
		float f = MathHelper.cos(pitch);
		float f1 = MathHelper.sin(pitch);
		double d0 = this.x;
		double d1 = this.y * (double) f + this.z * (double) f1;
		double d2 = this.z * (double) f - this.y * (double) f1;
		return new Vector(d0, d1, d2);
	}

	public Vector rotateYaw(float yaw) {
		float cos = MathHelper.cos(yaw);
		float sin = MathHelper.sin(yaw);
		double d0 = this.x * (double) cos + this.z * (double) sin;
		double d1 = this.y;
		double d2 = this.z * (double) cos - this.x * (double) sin;
		return new Vector(d0, d1, d2);
	}

	public Vector rotateRoll(float roll) {
		float cos = MathHelper.cos(roll);
		float sin = MathHelper.sin(roll);
		double d0 = this.x * (double) cos + this.z * (double) sin;
		double d1 = this.y * (double) cos + this.z * (double) sin;
		double d2 = this.z;
		return new Vector(d0, d1, d2);
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

	public static final ScriptExtensionTypeSerial<Vector, ListNBT> EXTENSION = ScriptExtensionTypeSerial.register((nbt) -> {
		if (nbt == null)
			return null;
		if (!(nbt instanceof ListNBT))
			return null;
		ListNBT list = (ListNBT)nbt;
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
		return new Vector(((NumberNBT)xnbt).getDouble(), ((NumberNBT)xnbt).getDouble(), ((NumberNBT)xnbt).getDouble());
	});

	@Override
	public ScriptExtensionType<Vector> getTypifier() {
		return EXTENSION;
	}

	@Override
	public ListNBT toNBT() {
		ListNBT list = new ListNBT();
		list.add(DoubleNBT.valueOf(x));
		list.add(DoubleNBT.valueOf(y));
		list.add(DoubleNBT.valueOf(z));
		return list;
	}

}
