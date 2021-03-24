package sybyline.anduril.util.math;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import sybyline.anduril.util.Util;

public interface Interpolation {

	public double interpolate(double normal);

	public static double linear(double normal, double start, double end) {
		return normal * end + (1.0D - normal) * start;
	}

	public static float linear(float normal, float start, float end) {
		return normal * end + (1.0F - normal) * start;
	}

	public static Vec2f linear(float normal, Vec2f start, Vec2f end) {
		return new Vec2f(linear(normal, start.x, end.x), linear(normal, start.y, end.y));
	}

	public static Vec3d linear(double normal, Vec3d start, Vec3d end) {
		return end.scale(normal).add(start.scale(1.0D - normal));
	}

	public static float linear_index(int index, int length) {
		return ((float)(index)) / ((float)(length-1));
	}

	public static void linear_bezier(float normal, float... ins) {
		int order = ins.length;
		while(order --> 0)
			for (int i = 0; i < order; i++)
				ins[i] = linear(normal, ins[i], ins[i + 1]);
	}

	public static Vec2f linear_bezier(float normal, float[][] flatxy) {
		linear_bezier(normal, flatxy[0]);
		linear_bezier(normal, flatxy[1]);
		return new Vec2f(flatxy[0][0], flatxy[1][0]);
	}

	public static Vec2f[] linear_bezier_curve_const(int points, Vec2f... ins) {
		if (points < 2) throw new IllegalArgumentException(String.valueOf(points));
		float[][] flatxy = Util.Structs.flatten(ins);
		float[][] flatxy_reuse = null;
		Vec2f[] ret = new Vec2f[points];
		for (int i = 0; i < points; i++) {
			if (flatxy_reuse == null) {
				flatxy_reuse = flatxy.clone();
			} else {
				System.arraycopy(flatxy[0], 0, flatxy_reuse[0], 0, flatxy.length);
				System.arraycopy(flatxy[1], 0, flatxy_reuse[1], 0, flatxy.length);
			}
			ret[i] = linear_bezier(linear_index(i, points), flatxy_reuse);
		}
		return ret;
	}

	public static List<Vec2f> linear_bezier_curve_dyn_normal(float normalstart, Vec2f... ins) {
		float[][] flatxy = Util.Structs.flatten(ins);
		List<Vec2f> ret = new ArrayList<>(MathHelper.ceil(1.2F / normalstart));
		ret.add(ins[0]);
		Vec2f prev = ins[0];
		float prevDist = 1;
		float stepAdj = normalstart;
		for (float k = stepAdj; k < 1.0F; k += stepAdj) {
			Vec2f fin = linear_bezier(k, flatxy);
			float dist = Util.Numbers.dist(prev, fin);
			if (k != stepAdj) // Only false for first iteration
				stepAdj *= (prevDist / dist);
			prevDist = dist;
			prev = fin;
			ret.add(fin);
		}
		ret.add(ins[ins.length - 1]);
		return ret;
	}

	public static List<Vec2f> linear_bezier_curve_dyn_step(float step, Vec2f... ins) {
		return linear_bezier_curve_dyn_normal((float) Util.Calculus.estimate(5, 5, 0.01, step, step, x -> Util.Numbers.dist(ins[0], linear_bezier((float)x, Util.Structs.flatten(ins)))), ins);
	}

	public static double linear(double normal) {
		return normal;
	}

	public static double singed2unsigned(double normal) {
		return normal * 0.5D + 0.5D;
	}

	public static double unsigned2signed(double normal) {
		return normal * 2.0D - 1.0D;
	}

	public interface Asymetric extends Interpolation {

		public static Asymetric of(double start, double median, double end) {
			double diff = median - (start + end) / 2.0D;
			return normal -> jumpOrder2(normal) * diff + linear(normal, start, end);
		}

	}

	public static double accelerateOrder2(double normal) {
		return normal * normal;
	}

	public static double accelerateOrder3(double normal) {
		return normal * normal * normal;
	}

	public static double accelerateOrder4(double normal) {
		return normal * normal * normal * normal;
	}

	public static double decelerateOrder2(double normal) {
		normal = 1.0D - normal;
		return 1.0D - normal * normal;
	}

	public static double decelerateOrder3(double normal) {
		normal = 1.0D - normal;
		return 1.0D - normal * normal * normal;
	}

	public static double decelerateOrder4(double normal) {
		normal = 1.0D - normal;
		return 1.0D - normal * normal * normal * normal;
	}

	public static double _accelerateOrder2(double normal) {
		return Math.pow(normal, 1D/2D);
	}

	public static double _accelerateOrder3(double normal) {
		return Math.pow(normal, 1D/3D);
	}

	public static double _accelerateOrder4(double normal) {
		return Math.pow(normal, 1D/4D);
	}

	public static double _decelerateOrder2(double normal) {
		normal = 1.0D - normal;
		return 1.0D - Math.pow(normal, 1D/2D);
	}

	public static double _decelerateOrder3(double normal) {
		normal = 1.0D - normal;
		return 1.0D - Math.pow(normal, 1D/3D);
	}

	public static double _decelerateOrder4(double normal) {
		normal = 1.0D - normal;
		return 1.0D - Math.pow(normal, 1D/4D);
	}

	public static double smoothOrder2(double normal) {
		return (1.0D - normal) * decelerateOrder2(normal) + normal * accelerateOrder2(normal);
	}

	public static double smoothOrder3(double normal) {
		return (1.0D - normal) * decelerateOrder3(normal) + normal * accelerateOrder3(normal);
	}

	public static double smoothOrder4(double normal) {
		return (1.0D - normal) * decelerateOrder4(normal) + normal * accelerateOrder4(normal);
	}

	public static double _smoothOrder2(double normal) {
		return (1.0D - normal) * _decelerateOrder2(normal) + normal * _accelerateOrder2(normal);
	}

	public static double _smoothOrder3(double normal) {
		return (1.0D - normal) * _decelerateOrder3(normal) + normal * _accelerateOrder3(normal);
	}

	public static double _smoothOrder4(double normal) {
		return (1.0D - normal) * _decelerateOrder4(normal) + normal * _accelerateOrder4(normal);
	}

	public static double jumpOrder2(double normal) {
		return decelerateOrder2(normal) * accelerateOrder2(1.0D - normal) * 4.0D;
	}

	public static double jumpOrder3(double normal) {
		return decelerateOrder3(normal) * accelerateOrder3(1.0D - normal) * 9.0D;
	}

	public static double jumpOrder4(double normal) {
		return decelerateOrder4(normal) * accelerateOrder4(1.0D - normal) * 16.0D;
	}

}