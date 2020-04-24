package sybyline.anduril.util;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.*;
import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.gson.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import sybyline.anduril.Anduril;

public final class Util {

	private Util() {}

	public static final Logger LOG = LogManager.getLogger("Sybyline");

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static final String MINECRAFT = "minecraft";
	public static final String SYBYLINE = "sybyline";
	public static final String ANDURIL = Anduril.MODID;

	public static final ResourceLocation NULL_RESOURCE = new ResourceLocation("null:null");

	@SuppressWarnings("unchecked")
	public static <T> List<T> scrapeDeclaredFields(Class<?> clazz, Object instance, List<T> ret, Predicate<T> filter) {
		for(Field f : clazz.getDeclaredFields()) {
			try {
				T t = (T)f.get(instance);
				if(t != null && filter.test(t)) {
					ret.add(t);
				}
			} catch(Exception e) { }
		}
		return ret;
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

	public static final class Numbers {

		private Numbers() {}

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

	}

	public static final class IO {

		private IO() {}

		public static final int MAX_STRING = 32767;

		public static final Charset DEFAULT = Charset.defaultCharset();

		@Nullable
		public static JsonObject readJsonFromStream(InputStream inputStream) {
			return JSONUtils.fromJson(GSON, new InputStreamReader(inputStream), JsonObject.class);
		}

		public static void writeJsonToStream(JsonObject obj, OutputStream stream) {
			try {
				new OutputStreamWriter(stream).append(obj.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Nullable
		public static JsonObject readJsonFromBuffer(PacketBuffer buffer) {
			return JSONUtils.fromJson(GSON, buffer.readString(MAX_STRING), JsonObject.class);
		}

		public static void writeJsonToBuffer(JsonElement obj, PacketBuffer buffer) {
			buffer.writeString(obj.toString(), MAX_STRING);
		}

		@Nullable
		public static String readString(InputStream stream) {
			if (stream == null) return null;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
				StringBuffer buff = new StringBuffer();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) buff.append(line).append('\n');
				return buff.toString();
			} catch(Exception e) {
				return null;
			}
		}

		public static void writeString(String source, OutputStream stream) {
			try {
				new OutputStreamWriter(stream).append(source);
			} catch(Exception e) {
			}
		}

		@Nullable
		public static String readString(Reader readerThing) {
			if (readerThing == null) return null;
			try {
				BufferedReader reader = new BufferedReader(readerThing);
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

		@Nullable
		public static List<String> readStringList(InputStream stream) {
			if (stream == null) return null;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				return reader.lines().collect(Collectors.toList());
			} catch(Exception e) {
				return null;
			}
		}

	}

}
