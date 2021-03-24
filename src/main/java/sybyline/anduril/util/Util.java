package sybyline.anduril.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.jodah.typetools.TypeResolver;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import sybyline.anduril.common.Anduril;
import sybyline.anduril.common.modelbb.ILocalized;
import sybyline.anduril.util.data.Syncable;
import sybyline.anduril.util.function.Prims;
import sybyline.anduril.util.function.ThrowingConsumer;
import sybyline.anduril.util.function.ThrowingFunction;
import sybyline.anduril.util.rtc.Failable;
import sybyline.anduril.util.rtc.ReflectionTricks;

public final class Util {

	private Util() {}

	public static final Logger LOG = LogManager.getLogger("Sybyline");

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

	public static final String MINECRAFT = "minecraft";
	public static final String SYBYLINE = "sybyline";
	public static final String ANDURIL = Anduril.MODID;

	public static final ResourceLocation NULL_RESOURCE = new ResourceLocation("null:null");
	public static final UUID NULL_UUID = new UUID(0L, 0L);
	public static final AxisAlignedBB MAX = new AxisAlignedBB(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

	public static boolean isNull(Object object) {
		if (object == null) return true;
		if (object instanceof UUID) return NULL_UUID.equals(object);
		if (object instanceof ResourceLocation) return NULL_RESOURCE.equals(object);
		return false;
	}

	public static <T> T loadConfig(Function<ForgeConfigSpec.Builder, T> func, ModConfig.Type type, ConfigFileFormat format, String name) {
    	String filename = name+"-"+type.extension()+"."+format.extension;
    	Pair<T, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(func);
		T ret = pair.getLeft();
    	ForgeConfigSpec spec = pair.getRight();
		CommentedFileConfig configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(filename), format.format.get()).sync().autosave().writingMode(WritingMode.REPLACE).build();
		configData.load();
		spec.setConfig(configData);
    	ModLoadingContext.get().registerConfig(type, spec, filename);
    	return ret;
	}

	public static void runWhenModExists(String mod, Supplier<Failable<?>> task) {
		try {
			if (ModList.get().getModObjectById(mod).isPresent()) {
				task.get().fail();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public static void runWhenClassExists(String clazz, Supplier<Failable<?>> task) {
		try {
			Class.forName(clazz);
			task.get().fail();
		} catch(ClassNotFoundException e) {
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	// NOT component type
	@SuppressWarnings("unchecked")
	public static <T> T[] create(Class<?> arrayClass, int length) {
		return (T[])Array.newInstance(arrayClass.getComponentType(), length);
	}
	public static <T> T[] create(T[] array, int length) {
		return create(array.getClass(), length);
	}

	public static <T> T[] populate(T[] array, IntFunction<T> populator) {
		for (int i = 0; i < array.length; i++) array[i] = populator.apply(i);
		return array;
	}

	public static <T> T[] fromIndex(T[] params, int fromIndex) {
		return Arrays.copyOfRange(params, fromIndex, params.length);
	}

	public static <T> T[] firstN(T[] params, int n) {
		return Arrays.copyOfRange(params, 0, n);
	}

	public static <T> T[] withoutLastN(T[] params, int n) {
		return firstN(params, params.length - n);
	}

	public static <T> T[] withoutIndex(T[] params, int index) {
		if (params == null || params.length < 2) return null;
		int len = params.length - 1;
		T[] ret = create(params, len);
		if (index == 0) {
			System.arraycopy(params, 1, ret, 0, len);
		} else if (index == len) {
			System.arraycopy(params, 0, ret, 0, len);
		} else {
			System.arraycopy(params, 0, ret, 0, index);
			System.arraycopy(params, index + 1, ret, index, len - index);
		}
		return ret;
	}

	public static <T> T[] withoutReferences(T[] array, T... refs) {
		for (T t : refs)
			array = withoutReference(array, t);
		return array;
	}

	public static <T> T[] withoutReference(T[] array, T ref) {
		if (array == null || array.length == 0) return array;
		for (int i = 0; i < array.length; i++)
			if (array[i] == ref)
				return withoutIndex(array, i);
		return array;
	}

	@SafeVarargs
	public static <T> T[] precat(T[] after, T... before) {
		return concat(before, after);
	}
	@SafeVarargs
	public static <T> T[] concat(T[] first, T... after) {
		if (first == null && after == null) return null;
		if (first.length == 0 && after.length == 0) return first;
		if (first == null || first.length == 0) return after;
		if (after == null || after.length == 0) return first;
		int total = first.length + after.length;
		T[] ret = create(first.getClass(), total);
		System.arraycopy(first, 0, ret, 0, first.length);
		System.arraycopy(after, 0, ret, first.length, after.length);
		return ret;
	}

	public static <I, O, E extends Throwable> O[] mapArray(Class<O> to, I[] in, ThrowingFunction<I, O, E> func) throws E {
		@SuppressWarnings("unchecked")
		O[] out = (O[])Array.newInstance(to, in.length);
		for (int i = 0; i < in.length; i++) {
			out[i] = func.apply(in[i]);
		}
		return out;
	}

	public static <I, E extends Throwable> I[] mapArray(boolean recreate, I[] in, ThrowingFunction<I, I, E> func) throws E {
		I[] out = recreate ? create(in.getClass(), in.length) : in;
		for (int i = 0; i < in.length; i++) {
			out[i] = func.apply(in[i]);
		}
		return out;
	}

	public static boolean arrayContains(Object thing, Object array) {
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++)
			if (Objects.equals(Array.get(array, i), thing))
				return true;
		return false;
	}

	public static <T> T throwSilent(Throwable throwable) {
		return Impl.<RuntimeException, T>throwSilent(throwable);
	}

	public static <I, O, E extends Throwable> Function<I, O> silenceF(ThrowingFunction<I, O, E> func) {
		return in -> {
			try {
				return func.apply(in);
			} catch(Throwable t) {
				return throwSilent(t);
			}
		};
	}

	public static <I, E extends Throwable> Consumer<I> silenceC(ThrowingConsumer<I, E> func) {
		return in -> {
			try {
				func.accept(in);
			} catch(Throwable t) {
				throwSilent(t);
			}
		};
	}

	public static Collector<Integer, ?, Integer> logicOp(IntBinaryOperator binary) {
		return Collectors.reducing(0, (l, r) -> binary.applyAsInt(l != null ? l : 0, r != null ? r : 0));
	}

	public static String randomString(int i) {
		return Impl.randomString(-1) + Impl.randomString(i);
	}

	public static String resource(Class<?> clazz, String resource) {
		try {
			if (!resource.startsWith("/")) resource = "/"+resource;
			InputStream stream;
			do {
				stream = clazz.getResourceAsStream(resource);
				resource = resource.replaceFirst("\\.+", "/");
			} while (stream == null && resource.indexOf('.') >= 0);
			return new java.io.BufferedReader(
				new java.io.InputStreamReader(
					stream
				)
			).lines()
			.collect(java.util.stream.Collectors.joining("\n"));
		} catch(Exception e) {
			return null;
		}
	}

	public static InputStream resourceStream(Class<?> clazz, String resource) {
		try {
			if (!resource.startsWith("/")) resource = "/"+resource;
			InputStream stream;
			do {
				stream = clazz.getResourceAsStream(resource);
				resource = resource.replaceFirst("\\.+", "/");
			} while (stream == null && resource.indexOf('.') >= 0);
			return stream;
		} catch(Exception e) {
			return null;
		}
	}

	public static enum ConfigFileFormat {
		TOML("toml", TomlFormat::instance),
		JSON("json", Util::unsupported),
		YAML("yaml", Util::unsupported),
		HOCON("conf", Util::unsupported);
		private ConfigFileFormat(String extension, Supplier<ConfigFormat<? extends CommentedConfig>> format) {
			this.extension = extension;
			this.format = format;
		}
		private final String extension;
		private final Supplier<ConfigFormat<? extends CommentedConfig>> format;
	}

	public static <T> T unsupported() {
		throw new UnsupportedOperationException("Hey, you shouldn't have done this! Try something else.");
	}

	public static <T extends RuntimeException> T exception() {
		throw new UnsupportedOperationException("Hey, you shouldn't have done this! Try something else.");
	}

	public static <P, T> void swap(P left, P right, Function<P, T> get, BiConsumer<P, T> set) {
		T first = get.apply(left);
		set.accept(left, get.apply(right));
		set.accept(left, first);
	}

	public static <P extends Collection<T>, T> void swapContents(P left, P right) {
		List<T> left_objs = new ArrayList<>(left);
		left.clear();
		left.addAll(right);
		right.clear();
		right.addAll(left_objs);
	}

	public static <K, V> void swapContents(Map<K, V> left, Map<K, V> right) {
		Map<K, V> left_objs = new HashMap<>(left);
		left.clear();
		left.putAll(right);
		right.clear();
		right.putAll(left_objs);
	}

	public static final class Types {

		private Types() {}

		@SuppressWarnings("unchecked")
		public static <T> Map<String, T> keyEnum() {
			return (Map<String, T>) keyEnum(ReflectionTricks.callingClass());
		}
	
		@SuppressWarnings("unchecked")
		public static <T> Map<String, T> keyEnum(Class<T> clazz) {
			Map<String, T> ret = new HashMap<>();
			for (Object o : clazz.getEnumConstants())
				ret.put(((ILocalized)o).codeName(), (T)o);
			return ret;
		}
	
		@SuppressWarnings("unchecked")
		public static <T> T[] genericArray(IntFunction<?> object, int count) {
			return (T[])object.apply(count);
		}
	
		@SuppressWarnings("unchecked")
		public static <T> T[] genericArray(Object[] object) {
			return (T[])object;
		}
	
		@SuppressWarnings("unchecked")
		@Nullable
		public static <T> Class<T> getGenericType(Supplier<T> value) {
			Class<?> arg = TypeResolver.resolveRawArgument(Supplier.class, value.getClass());
			if (arg == TypeResolver.Unknown.class)
				return null;
			return (Class<T>) arg;
		}
	
		@SuppressWarnings("unchecked")
		@Nullable
		public static <T> Class<T> getGenericType(Consumer<T> value) {
			Class<?> arg = TypeResolver.resolveRawArgument(Consumer.class, value.getClass());
			if (arg == TypeResolver.Unknown.class)
				return null;
			return (Class<T>) arg;
		}
	
		@SuppressWarnings("unchecked")
		@Nullable
		public static <T> Class<T> getGenericType(Predicate<T> value) {
			Class<?> arg = TypeResolver.resolveRawArgument(Predicate.class, value.getClass());
			if (arg == TypeResolver.Unknown.class)
				return null;
			return (Class<T>) arg;
		}
	
		@SuppressWarnings("unchecked")
		@Nullable
		public static <T, G> Class<T> getGenericType(G value, Class<G> type) {
			Class<?> arg = TypeResolver.resolveRawArgument(type, value.getClass());
			if (arg == TypeResolver.Unknown.class)
				return null;
			return (Class<T>) arg;
		}
	
		@SuppressWarnings("unchecked")
		public static <T, GENERIC extends T, E> GENERIC[] genericMappedArray(IntFunction<T[]> object, E[] objects, Function<E, T> func) {
			GENERIC[] ret = (GENERIC[])object.apply(objects.length);
			for (int i = 0; i < objects.length; i++)
				ret[i] = (GENERIC) func.apply(objects[i]);
			return ret;
		}

	}

	public static final class Numbers {

		private Numbers() {}

		public static final Random random = new Random();

		public static final DecimalFormat POINT_ZERO = new DecimalFormat("#####0");
		public static final DecimalFormat POINT_ONE = new DecimalFormat("#####0.0");
		public static final DecimalFormat POINT_TWO = new DecimalFormat("#####0.00");

		public static final double PI_HALF = Math.PI / 2D;
		public static final float PI_HALF_F = (float)Math.PI / 2F;
		public static final double DEGREES = 360D / (Math.PI * 2D);
		public static final float DEGREES_F = 360F / (float)(Math.PI * 2);
		public static final double PI = Math.PI;
		public static final float PI_F = (float)Math.PI;
		public static final double TAU = Math.PI * 2D;
		public static final float TAU_F = (float)Math.PI * 2F;

		public static float dist(Vec2f one, Vec2f two) {
			float dx = one.x - two.x;
			float dy = one.y - two.y;
			return MathHelper.sqrt(dx * dx + dy * dy);
		}

		public static int signum(int direction) {
			return direction == 0 ? 0 : direction > 0 ? 1 : -1;
		}

		public static int pmod(int v, int m) {
			v %= m;
			return v < 0 ? v + m : v;
		}

		// Not secure at all, intended to quickly generate pseudo-random ints from entity IDs during rendering etc
		public static int smear(int i) {
			i = (i + 0x7ed55d16) + (i << 12);
			i = (i ^ 0xc761c23c) ^ (i >> 19);
			i = (i + 0x165667b1) + (i << 5);
			i = (i + 0xd3a2646c) ^ (i << 9);
			i = (i + 0xfd7046c5) + (i << 3);
			i = (i ^ 0xb55a4f09) ^ (i >> 16);
			return i;
		}
		public static long smear(long l) {
			l = (l + 0x7ed55d16) + (l << 12);
			l = (l ^ 0xc761c23c) ^ (l >> 19);
			l = (l + 0x165667b1) + (l << 5);
			l = (l + 0xd3a2646c) ^ (l << 9);
			l = (l + 0xfd7046c5) + (l << 3);
			l = (l ^ 0xb55a4f09) ^ (l >> 16);
			return l;
		}

		public static int increaseColorBrightness(int color) {
			return ~((~color >> 1) & 0x7F7F7F7F);
		}

		public static float percentIncrease(float in, float by) {
			return in + in * (by / 100F);
		}

		public static double percentIncrease(double in, double by) {
			return in + in * (by / 100D);
		}

		public static Prims.FloatUnaryOp percentIncrease(float by) {
			return in -> percentIncrease(in, by);
		}

		public static Prims.DoubleUnaryOp percentIncrease(double by) {
			return in -> percentIncrease(in, by);
		}

		private static final int[] values = {
			12096000, 1728000, 72000, 1200, 20
		};
		private static final String[] names = {
			"month", "day", "hour", "minute", "second"
		};
		public static String ticksToHMS(int ticks) {
			StringBuilder ret = new StringBuilder();
			for (int i = 0; i < 5; i++) {
				int val = values[i];
				if (ticks > val) {
					int amt = ticks / val;
					ret.append(String.valueOf(amt)).append(' ').append(names[i]).append(amt == 1 ? ", " : "s, ");
					ticks %= val;
				} else if (ret.length() > 0) {
					ret.append("0 ").append(names[i]).append("s, ");
				}
			}
			return ret.append(String.valueOf(ticks)).append(ticks == 1 ? " tick" : " ticks").toString();
		}

		private static enum RNum {
			\u2188(100_000),
			\u2182\u2188(90_000),
			\u2187(50_000),
			\u2182\u2187(40_000),
			\u2182(10_000),
			M\u2182(9_000),
			\u2181(5_000),
			M\u2181(4_000),
			M(1_000),
			CM(900),
			D(500),
			CD(400),
			C(100),
			XC(90),
			L(50),
			XL(40),
			X(10),
			IX(9),
			V(5),
			IV(4),
			I(1),
			;
			private RNum(int value) {
				this.value = value;
			}
			private final int value;
			private final int dec(StringBuilder str, int num) {
				str.append(name());
				return num - value;
			}
		}
		public static String romanNumeral(int num) {
			StringBuilder ret = new StringBuilder();
			if (num < 0) {
				ret.append('-');
				num = -num;
			}
			for (RNum rnum : RNum.values())
				while (num >= rnum.value)
					num = rnum.dec(ret, num);
			return ret.toString();
		}

		public static byte clamp(byte num, byte min, byte max) {
			return num < min ? min : num > max ? max : num;
		}

		public static short clamp(short num, short min, short max) {
			return num < min ? min : num > max ? max : num;
		}

		public static int clamp(int num, int min, int max) {
			return num < min ? min : num > max ? max : num;
		}

		public static long clamp(long num, long min, long max) {
			return num < min ? min : num > max ? max : num;
		}

		public static float clamp(float num, float min, float max) {
			return num < min ? min : num > max ? max : num;
		}

		public static double clamp(double num, double min, double max) {
			return num < min ? min : num > max ? max : num;
		}

		public static float clampRotationDegrees(float in, float target, float travel) {
//			Vec3d ret = Vec3d.fromPitchYaw(0.0F, in).add(Vec3d.fromPitchYaw(0.0F, target).scale(travel)).normalize();
//			return (float) MathHelper.atan2(ret.x, ret.z) * DEGREES_F;
			return MathHelper.approachDegrees(in, target, travel * (MathHelper.abs(MathHelper.wrapSubtractDegrees(target, in)) + 10.0F) / 10.0F);
		}

		public static boolean decrementIfAbove(int floor, AtomicInteger atom) {
			if (atom.intValue() <= floor)
				return false;
			atom.decrementAndGet();
			return true;
		}

		public static boolean decrementIfAbove(long floor, LongAdder atom) {
			if (atom.longValue() <= floor)
				return false;
			atom.decrement();
			return true;
		}

		public static boolean decrementIfAbove(long floor, AtomicLong atom) {
			if (atom.longValue() <= floor)
				return false;
			atom.decrementAndGet();
			return true;
		}

		public static void subtractGreedy(long floor, long number, LongAdder atom) {
			if (atom.longValue() - number >= floor) {
				atom.add(-number);
			} else {
				atom.reset();
				atom.add(floor);
			}
		}

		public static double sanD(double val) {
			return Double.isFinite(val) ? val : 0.0D;
		}
		public static double sanDD(double val) {
			return Double.isFinite(val) ? Math.abs(val) > 0.0001D ? val : 1.0D : 1.0D;
		}

		public static float sanF(float val) {
			return Float.isFinite(val) ? val : 0.0F;
		}
		public static float sanFD(float val) {
			return Float.isFinite(val) ? Math.abs(val) > 0.0001F ? val : 1.0F : 1.0F;
		}

		public static double mean(double... values) {
			if (values == null || values.length == 0) return 0;
			double acc = 0;
			for (double value : values) acc += value;
			return acc / (double)values.length;
		}

		public static double meanExc(double exception, double... values) {
			if (values == null || values.length == 0) return 0;
			if (values.length == 1) return exception;
			double acc = 0;
			boolean excepted = false;
			for (double value : values)
				if (value == exception && !excepted)
					excepted = true;
				else
					acc += value;
			return excepted && values.length == 1
				? 0
				: acc / (double)(excepted ? values.length : (values.length - 1));
		}

	}

	public static final class Calculus {

		private Calculus() {}

		public static double integrate_trapezoid(int precision, double from, double to, Prims.DoubleUnaryOp function) {
			if (precision <= 0) return Double.NaN;
			double h = (to - from) / precision;
			double sum = 0.5 * (function.apply(from) + function.apply(to));
			for (int i = 1; i < precision; i++)
				sum += function.apply(from + h * i);
			return sum * h;
		}

		public static double integtate_simpson(int precision, double from, double to, Prims.DoubleUnaryOp function) {
			if (precision <= 0) return Double.NaN;
			double h = (to - from) / (precision - 1);
			double sum = 1.0 / 3.0 * (function.apply(from) + function.apply(to));
			for (int i = 1; i < precision - 1; i += 2)
				sum += 4.0 / 3.0 * function.apply(from + h * i);
			for (int i = 2; i < precision - 1; i += 2)
				sum += 2.0 / 3.0 * function.apply(from + h * i);
			return sum * h;
		}

		public static Prims.DoubleUnaryOp integral_trapezoid(int precision, double from, Prims.DoubleUnaryOp function) {
			return x -> integrate_trapezoid(precision, from, x, function);
		}

		public static Prims.DoubleUnaryOp integral_simpson(int precision, double from, Prims.DoubleUnaryOp function) {
			return x -> integtate_simpson(precision, from, x, function);
		}

		public static double derrivate(int precision, double h, double at, Prims.DoubleUnaryOp function) {
			if (precision <= 0) return Double.NaN;
			double sum = 0;
			for (int i = -precision; i < precision; i++) if (i != 0)
				sum += (function.apply(at + h * i) - function.apply(at)) / (h * i);
			return sum / (precision * 2 - 1);
		}

		public static Prims.DoubleUnaryOp derrivative(int precision, double h, Prims.DoubleUnaryOp function) {
			return x -> derrivate(precision, h, x, function);
		}

		public static double estimate(int precisionSolve, int precisionDerrivate, double h, double inputEstimate, double outputDesired, Prims.DoubleUnaryOp function) {
			while (precisionSolve --> 0)
				inputEstimate = inputEstimate + (outputDesired - function.apply(inputEstimate)) / derrivate(precisionDerrivate, h, inputEstimate, function);
			return inputEstimate;
		}

		public static Prims.DoubleUnaryOp estimate_inverse(int precisionSolve, int precisionDerrivate, double h, double inputEstimate, Prims.DoubleUnaryOp function) {
			return x -> estimate(precisionSolve, precisionDerrivate, h, inputEstimate, x, function);
		}

	}

	public static final class Structs {

		private Structs() {}

		public static ColumnPos fromLong(long l) {
			int x = (int)((l >> 00) & 4294967295L);
			int z = (int)((l >> 32) & 4294967295L);
			return new ColumnPos(x, z);
		}

		public static ColumnPos fromJson(JsonObject json, String member) {
			JsonArray array = json.get(member).getAsJsonArray();
			return fromJson(array);
		}

		public static ColumnPos fromJson(JsonArray array) {
			return new ColumnPos(
				array.get(0).getAsInt(),
				array.get(1).getAsInt()
			);
		}

		public static Vec2f fromJsonF(JsonObject json, String member) {
			JsonArray array = json.get(member).getAsJsonArray();
			return fromJsonF(array);
		}

		public static Vec2f fromJsonF(JsonArray array) {
			return new Vec2f(
				array.get(0).getAsFloat(),
				array.get(1).getAsFloat()
			);
		}

		public static float[][] flatten(Vec2f... vecs) {
			float[][] ret = new float[2][vecs.length];
			for (int i = 0; i < vecs.length; i++) {
				ret[0][i] = vecs[i].x;
				ret[i][i] = vecs[i].y;
			}
			return ret;
		}

		public static ResourceLocation prepend(ResourceLocation location, String string) {
			return new ResourceLocation(location.getNamespace(), string + location.getPath());
		}

		public static ResourceLocation append(ResourceLocation location, String string) {
			return new ResourceLocation(location.getNamespace(), location.getPath() + string);
		}

		public static ResourceLocation sandwich(ResourceLocation location, String string1, String string2) {
			return new ResourceLocation(location.getNamespace(), string1 + location.getPath() + string2);
		}

		public static ResourceLocation format(ResourceLocation location, String string) {
			return new ResourceLocation(location.getNamespace(), String.format(string, location.getPath()));
		}

		public static UUID parseOrNull(String string) {
			try {
				return UUID.fromString(string);
			} catch(IllegalArgumentException badString) {
				return NULL_UUID;
			}
		}

		public static UUID uuid(String seed) {
			String a = seed, b = seed + "asdf", c = b + "ghjk", d = c + "lqzp";
			return new UUID(
				((long)a.hashCode()) | (((long)b.hashCode()) << 32),
				((long)c.hashCode()) | (((long)d.hashCode()) << 32)
			);
		}

		public static UUID randomUUID() {
			return UUID.randomUUID();
		}

		public static int indexOf(Object object, Object array) {
			int length = Array.getLength(array);
			for (int i = 0; i < length; i++)
				if (Objects.equals(object, Array.get(array, i)))
					return i;
			return -1;
		}

		public static String insert(String string, String inserted, int index) {
			index = MathHelper.clamp(index, 0, string.length());
			return string.substring(0, index) + inserted + string.substring(index);
		}

		public static ResourceLocation create(String string) {
			ResourceLocation ret = ResourceLocation.tryCreate(string);
			return ret != null ? ret : NULL_RESOURCE;
		}

		public static ResourceLocation create(String domain, String string) {
			ResourceLocation ret = ResourceLocation.tryCreate(domain + ':' + string);
			return ret != null ? ret : NULL_RESOURCE;
		}

		public static <T> void trySetElseAppend(List<T> list, int index, T thing) {
			if (index >= list.size())
				list.add(thing);
			else
				list.set(index, thing);
		}

		public static <T> T tryGetElseDefault(List<T> list, int index, T thing) {
			return index < list.size() && index >= 0
				? list.get(index)
				: thing;
		}

		public static String caseToUnderscores(String withCapitals) {
			return caseTo(withCapitals, "_");
		}

		public static String caseToPeriods(String withCapitals) {
			return caseTo(withCapitals, ".");
		}

		public static String caseTo(String withCapitals, String replace) {
			StringBuilder ret = new StringBuilder();
			for (char ch : withCapitals.toCharArray())
				ret.append(Character.isUpperCase(ch)
					? ((ret.length() == 0 ? "" : replace) + String.valueOf(Character.toLowerCase(ch)))
					: String.valueOf(ch)
				);
			return ret.toString();
		}

		public static Vec3d limit(Vec3d motion, double dist) {
			double distsq = dist * dist,
				sq = motion.lengthSquared();
			return sq <= distsq
				? motion
				: motion.scale(Math.sqrt(sq) / distsq);
		}

		public static String appendStrings(String... string) {
			return Arrays.stream(string).collect(Collectors.joining());
		}

		public static <T, U extends Comparable<U>> Comparator<T> compareBy(Function<T, U> sub) {
			return (left, right) -> sub.apply(left).compareTo(sub.apply(right));
		}

		public static String toKey(BlockPos pos) {
			return Long.toHexString(pos.toLong());
		}

		public static BlockPos fromKey(String key) {
			return BlockPos.fromLong(Long.decode("0x"+key));
		}

		public static String toValue(BlockState state) {
			return state.getBlock().getRegistryName().toString()+'['+state.getValues().entrySet().stream().map(entry -> entry.getKey().getName() + '=' + entry.getValue().toString()).collect(Collectors.joining(","))+']';
		}

		public static BlockState fromValue(String state) {
			try {
				return new BlockStateParser(new StringReader(state), false).parse(false).getState();
			} catch (CommandSyntaxException ex) {
				ex.printStackTrace();
				return Blocks.AIR.getDefaultState();
			}
		}

		public static String toValue(IFluidState state) {
			return state.getFluid().getRegistryName().toString()+'['+state.getValues().entrySet().stream().map(entry -> entry.getKey().getName() + '=' + entry.getValue().toString()).collect(Collectors.joining(","))+']';
		}

		public static AxisAlignedBB scale(AxisAlignedBB box, double scale) {
			return new AxisAlignedBB(box.minX * scale, box.minY * scale, box.minZ * scale, box.maxX * scale, box.maxY * scale, box.maxZ * scale);
		}

		public static <T extends Syncable> Capability.IStorage<T> capStorage() {
			return new Capability.IStorage<T>() {
				@Override
				public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
					return instance.serialize();
				}
				@Override
				public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
					if (nbt instanceof CompoundNBT) instance.deserialize((CompoundNBT)nbt);
				}
			};
		}

	}

	public static final class IO {

		private IO() {}

		public static final int MAX_STRING = 1024 * 1024;

		public static final Charset DEFAULT = Charset.defaultCharset();

		public static final ExecutorService ioExec = Executors.newCachedThreadPool();

		@Nullable
		public static JsonObject readJsonFromStream(InputStream inputStream) {
			try {
				return JSONUtils.fromJson(GSON, new InputStreamReader(inputStream), JsonObject.class);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Nullable
		public static JsonElement readJsonElementFromStream(InputStream inputStream) {
			return readJsonElementFromStream(inputStream, JsonElement.class);
		}

		@Nullable
		public static <T extends JsonElement> T readJsonElementFromStream(InputStream inputStream, Class<T> type) {
			try {
				return JSONUtils.fromJson(GSON, new InputStreamReader(inputStream), type);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static void writeJsonToStream(JsonObject obj, OutputStream stream) {
			try {
				new OutputStreamWriter(stream).append(GSON.toJson(obj));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Nullable
		public static JsonObject readJsonFromBuffer(PacketBuffer buffer) {
			try {
				String json = buffer.readString(MAX_STRING);
//				System.err.println(json);
				return JSONUtils.fromJson(GSON, json, JsonObject.class);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Nullable
		public static JsonElement readJsonElementFromBuffer(PacketBuffer buffer) {
			try {
				String json = buffer.readString(MAX_STRING);
//				System.err.println(json);
				return JSONUtils.fromJson(GSON, json, JsonElement.class);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static void writeJsonToBuffer(JsonElement obj, PacketBuffer buffer) {
			String json = GSON.toJson(obj);
//			System.err.println(json);
			buffer.writeString(json, MAX_STRING);
		}

		@Nullable
		public static String readString(InputStream stream) {
			if (stream == null) return null;
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			) {
				StringBuffer buff = new StringBuffer();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) buff.append(line).append('\n');
				return buff.toString();
			} catch(Exception e) {
				return null;
			}
		}

		@Nullable
		public static String readString(Reader readerThing) {
			if (readerThing == null) return null;
			try (
				BufferedReader reader = new BufferedReader(readerThing);
			) {
				StringBuffer buff = new StringBuffer();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) buff.append(line).append('\n');
				return buff.toString();
			} catch(Exception e) {
				return null;
			}
		}

		public static void writeString(String source, Writer writerThing) {
			try {
				writerThing.append(source);
			} catch(Exception e) {
			}
		}

		public static void writeString(String source, OutputStream stream) {
			try {
				new OutputStreamWriter(stream).append(source);
			} catch(Exception e) {
			}
		}

		@Nullable
		public static List<String> readStringList(InputStream stream) {
			if (stream == null) return null;
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			) {
				return reader.lines().collect(Collectors.toList());
			} catch(Exception e) {
				return null;
			}
		}

		public static Function<ResourceLocation, IResource> mapperOrNull(IResourceManager res) {
			return str -> {
				try {
					return res.getResource(str);
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			};
		}

		// Relative resources

		public static List<String> relativeResourceStringList(String resource) {
			return readStringList(relativeResource(ReflectionTricks.callingClass(), resource));
		}

		public static String relativeResourceString(String resource) {
			return readString(relativeResource(ReflectionTricks.callingClass(), resource));
		}

		public static InputStream relativeResource(String resource) {
			return relativeResource(ReflectionTricks.callingClass(), resource);
		}

		public static InputStream relativeResource(Class<?> caller, String resource) {
			try {
				String element = caller.getName();
				int lastPeriod = element.lastIndexOf('.') + 1;
				if (lastPeriod > 0) element = element.substring(0, lastPeriod);
				return caller.getResourceAsStream("/"+element.replace(".", "/")+resource);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static void writeResetIntAt(PacketBuffer buffer, int idx, int value) {
			int idxnow = buffer.writerIndex();
			buffer.writerIndex(idx);
			buffer.writeInt(value);
			buffer.writerIndex(idxnow);
		}

		public static boolean unzip(InputStream input, File destDirectory) throws IOException {
			if (input == null || destDirectory == null) return false;
			if (!destDirectory.exists())
				destDirectory.mkdir();
			ZipInputStream zipStream = new ZipInputStream(input);
			for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry())
				if (entry.isDirectory())
					new File(destDirectory, entry.getName()).mkdirs();
				else
					IOUtils.copy(zipStream, new FileOutputStream(new File(destDirectory, entry.getName())));
			zipStream.close();
			return true;
		}

		public static int awaitProcess(Process process) throws Throwable {
			return awaitProcess(process, System.out, System.err, null);
		}
		public static int awaitProcess(Process process, byte[] inpipe) throws Throwable {
			return awaitProcess(process, System.out, System.err, inpipe);
		}
		public static int awaitProcess(Process process, OutputStream pipe) throws Throwable {
			return awaitProcess(process, pipe, pipe, null);
		}
		public static int awaitProcess(Process process, OutputStream pipe, byte[] inpipe) throws Throwable {
			return awaitProcess(process, pipe, pipe, inpipe);
		}
		public static int awaitProcess(Process process, OutputStream outpipe, OutputStream errpipe) throws Throwable {
			return awaitProcess(process, outpipe, errpipe, null);
		}
		public static int awaitProcess(Process process, OutputStream outpipe, OutputStream errpipe, byte[] inpipe) throws Throwable {
			Future<Integer> PoutT = ioExec.submit(() -> IOUtils.copy(process.getInputStream(), outpipe));
			Future<Integer> PerrT = ioExec.submit(() -> IOUtils.copy(process.getErrorStream(), errpipe));
			Throwable io = null;
			if (inpipe != null && inpipe.length > 0) try {
				process.getOutputStream().write(inpipe);
			} catch (Throwable ioIn) {
				io = ioIn;
			}
			while (process.isAlive())
				Thread.yield();
			int ret = Integer.MIN_VALUE;
			try {
				try {
					PoutT.get();
				} catch (Throwable ee) {
					if (ee instanceof ExecutionException)
						ee = ee.getCause();
					if (io == null)
						io = ee;
					else
						io.addSuppressed(ee);
				}
				try {
					PerrT.get();
				} catch (Throwable ee) {
					if (ee instanceof ExecutionException)
						ee = ee.getCause();
					if (io == null)
						io = ee;
					else
						io.addSuppressed(ee);
				}
				ret = process.waitFor();
			} catch(Throwable interrupt) {
				if (io == null)
					io = interrupt;
				else
					io.addSuppressed(interrupt);
				try {
					ret = process.destroyForcibly().exitValue();
				} catch(Throwable ex) {
					io.addSuppressed(ex);
					ret = Integer.MIN_VALUE;
				}
			}
			if (io != null)
				throw io;
			return ret;
		}

	}

	public static final class NBT {

		private NBT() {}

		public static boolean getElse(CompoundNBT nbt, String key, boolean fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getBoolean(key)
				: fallback;
		}

		public static byte getElse(CompoundNBT nbt, String key, byte fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getByte(key)
				: fallback;
		}

		public static byte getElseClamped(CompoundNBT nbt, String key, byte fallback, byte min, byte max) {
			byte ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getByte(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static short getElse(CompoundNBT nbt, String key, short fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getShort(key)
				: fallback;
		}

		public static short getElseClamped(CompoundNBT nbt, String key, short fallback, short min, short max) {
			short ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getShort(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static int getElse(CompoundNBT nbt, String key, int fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getInt(key)
				: fallback;
		}

		public static int getElseClamped(CompoundNBT nbt, String key, int fallback, int min, int max) {
			int ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getInt(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static long getElse(CompoundNBT nbt, String key, long fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getLong(key)
				: fallback;
		}

		public static long getElseClamped(CompoundNBT nbt, String key, long fallback, long min, long max) {
			long ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getLong(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static float getElse(CompoundNBT nbt, String key, float fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getFloat(key)
				: fallback;
		}

		public static float getElseClamped(CompoundNBT nbt, String key, float fallback, float min, float max) {
			float ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getFloat(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static double getElse(CompoundNBT nbt, String key, double fallback) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getDouble(key)
				: fallback;
		}

		public static double getElseClamped(CompoundNBT nbt, String key, double fallback, double min, double max) {
			double ret = nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getDouble(key)
				: fallback;
				return Numbers.clamp(ret, min, max);
		}

		public static String getElse(CompoundNBT nbt, String key, String fallback) {
			return nbt.contains(key, Constants.NBT.TAG_STRING)
				? nbt.getString(key)
				: fallback;
		}

		public static UUID getElse(CompoundNBT nbt, String key, UUID fallback) {
			return nbt.contains(key + "Most", Constants.NBT.TAG_ANY_NUMERIC) || nbt.contains(key + "Least", Constants.NBT.TAG_ANY_NUMERIC)
				? nbt.getUniqueId(key)
				: fallback;
		}

		public static byte[] getElse(CompoundNBT nbt, String key, byte... fallback) {
			return nbt.contains(key, Constants.NBT.TAG_BYTE_ARRAY)
				? nbt.getByteArray(key)
				: fallback;
		}

		public static int[] getElse(CompoundNBT nbt, String key, int... fallback) {
			return nbt.contains(key, Constants.NBT.TAG_INT_ARRAY)
				? nbt.getIntArray(key)
				: fallback;
		}

		public static long[] getElse(CompoundNBT nbt, String key, long... fallback) {
			return nbt.contains(key, Constants.NBT.TAG_LONG_ARRAY)
				? nbt.getLongArray(key)
				: fallback;
		}

		public static ListNBT getElse(CompoundNBT nbt, String key, int type, ListNBT fallback) {
			return nbt.contains(key, Constants.NBT.TAG_LIST)
				? nbt.getList(key, type)
				: fallback;
		}

		public static CompoundNBT getElse(CompoundNBT nbt, String key, CompoundNBT fallback) {
			return nbt.contains(key, Constants.NBT.TAG_COMPOUND)
				? nbt.getCompound(key)
				: fallback;
		}

		public static CompoundNBT newCompound(CompoundNBT nbt, String key) {
			CompoundNBT ret = new CompoundNBT();
			nbt.put(key, ret);
			return ret;
		}

		public static ListNBT newList(CompoundNBT nbt, String key) {
			ListNBT ret = new ListNBT();
			nbt.put(key, ret);
			return ret;
		}

		public static ListNBT getList(CompoundNBT nbt, String key, int nbtid) {
			if (nbt.getTagId(key) != Constants.NBT.TAG_LIST)
				return new ListNBT();
			ListNBT ret = (ListNBT)nbt.get(key);
			if (!matches(ret.getTagType(), nbtid))
				return new ListNBT();
			return ret;
		}

		public static OptionalInt getOptInt(CompoundNBT nbt, String key) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC) ? OptionalInt.of(nbt.getInt(key)) : OptionalInt.empty();
		}

		public static void setOptInt(CompoundNBT nbt, String key, OptionalInt opt) {
			opt.ifPresent(value -> nbt.putInt(key, value));
		}

		public static OptionalLong getOptLong(CompoundNBT nbt, String key) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC) ? OptionalLong.of(nbt.getLong(key)) : OptionalLong.empty();
		}

		public static void setOptLong(CompoundNBT nbt, String key, OptionalLong opt) {
			opt.ifPresent(value -> nbt.putLong(key, value));
		}

		public static OptionalDouble getOptDouble(CompoundNBT nbt, String key) {
			return nbt.contains(key, Constants.NBT.TAG_ANY_NUMERIC) ? OptionalDouble.of(nbt.getDouble(key)) : OptionalDouble.empty();
		}

		public static void setOptDouble(CompoundNBT nbt, String key, OptionalDouble opt) {
			opt.ifPresent(value -> nbt.putDouble(key, value));
		}

		public static Optional<CompoundNBT> getOptCompound(CompoundNBT nbt, String key) {
			return nbt.contains(key, Constants.NBT.TAG_COMPOUND) ? Optional.of(nbt.getCompound(key)) : Optional.empty();
		}

		public static Optional<ListNBT> getOptList(CompoundNBT nbt, String key) {
			return nbt.contains(key, Constants.NBT.TAG_LIST) && nbt.get(key) instanceof ListNBT ? Optional.of((ListNBT)nbt.get(key)) : Optional.empty();
		}

		public static <Tag extends INBT> void setOptTag(CompoundNBT nbt, String key, Optional<Tag> opt) {
			setOptTag(nbt, key, opt, Function.identity());
		}

		public static <Tag extends INBT, T> void setOptTag(CompoundNBT nbt, String key, Optional<T> opt, Function<T, Tag> func) {
			opt.ifPresent(value -> nbt.put(key, func.apply(value)));
		}

		public static boolean matches(int found, int type) {
			return found == type
				? true
				: type == Constants.NBT.TAG_ANY_NUMERIC
					? Constants.NBT.TAG_BYTE <= found && found <= Constants.NBT.TAG_DOUBLE
					: false;
		}

	}

	public static final class Crypto {

		private Crypto() {}
		public static final String AES =  "AES";

		public static final byte[] crypt(boolean fromEnc, byte[] in, String key) {
			try {
				Cipher cipher = Cipher.getInstance(AES);
				cipher.init(fromEnc ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, new SecretKeySpec(getKey(key), AES));
				in = cipher.doFinal(in);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return in;
		}

	    public static byte[] getKey(String user) {
	    	byte[] raw = user.getBytes(StandardCharsets.UTF_8);
	    	byte[] ret = new byte[16];
	    	for (int i = 0; i < raw.length; i++)
	    		ret[i % 16] ^= raw[i];
	    	return ret;
	    }

	    public static InputStream wrapIn(InputStream in, String key) {
	    	try {
				Cipher cipher = Cipher.getInstance(AES);
				cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getKey(key), AES));
	    		return new CipherInputStream(in, cipher);
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    		return in;
	    	}
	    }

	    public static OutputStream wrapOut(OutputStream out, String key) {
	    	try {
				Cipher cipher = Cipher.getInstance(AES);
				cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getKey(key), AES));
	    		return new CipherOutputStream(out, cipher);
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    		return out;
	    	}
	    }

	}

	public static final class Debug {

		private Debug() {}

		public static void popup(String message) {
			String trace = Arrays.stream(new Throwable().getStackTrace())
				.map(String::valueOf)
				.collect(Collectors.joining("\n"));
			javax.swing.SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(null, trace, message, 1));
		}

	}

	public static Runnable forever(Runnable task) {
		return () -> {
			while (true) {
				try {
					task.run();
				} catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
		};
	}

}

class Impl {
    @SuppressWarnings("unchecked")
    static <T extends Throwable, O> O throwSilent(Throwable e) throws T {
        throw (T) e;
    }
    static String randomString(int i) {
		return String.valueOf(counter.nextInt()) + String.valueOf(counter.nextInt());
	}
    static final Random counter = new Random();
}
