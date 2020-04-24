package sybyline.anduril.util.math;

public interface Interpolation {

	public double interpolate(double normal);

	public static double linear(double normal, double start, double end) {
		return normal * end + (1.0D - normal) * start;
	}

	public static float linear(float normal, float start, float end) {
		return normal * end + (1.0F - normal) * start;
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

	public static final class Asymetric {

		private Asymetric() {}

		public static Interpolation of(double start, double median, double end) {
			return new AsymetricPseudoLogarithmic(start, median, end);
		}

		private static class AsymetricPseudoLogarithmic implements Interpolation {
			private final double start;
			private final double end;
			private final double diff;
			public AsymetricPseudoLogarithmic(double start, double median, double end) {
				this.start = start;
				this.end = end;
				this.diff = median - (start + end) / 2.0D;
			}
			@Override
			public double interpolate(double normal) {
				return jumpOrder2(normal) * diff + linear(normal, start, end);
			}
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