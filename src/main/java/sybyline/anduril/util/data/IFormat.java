package sybyline.anduril.util.data;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import javax.annotation.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import sybyline.anduril.util.Util;

public interface IFormat<D> {

	@Nonnull
	public D create();

	@Nullable
	public D read(File file) throws IOException;

	@Nullable
	public D read(PacketBuffer packet) throws IOException;

	@Nullable
	public D read(ResourceLocation loc, InputStream stream) throws IOException;

	@Nullable
	public default D readOrNull(File file) {
		try {
			return this.read(file);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void write(File file, @Nonnull D data) throws IOException;

	public void write(PacketBuffer packet, @Nonnull D data) throws IOException;

	public void write(OutputStream stream, @Nonnull D data) throws IOException;

	public default void writeOrNull(File file, @Nonnull D data) {
		try {
			this.write(file, data);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public String filename(String id);

	public static final IFormat<JsonObject> JSON = new IFormat<JsonObject>() {
		private final Charset DEF = Charset.defaultCharset();
		@Override
		public JsonObject create() {
			return new JsonObject();
		}
		@Override
		public JsonObject read(File file) throws IOException {
			List<String> list = IOUtils.readLines(new FileInputStream(file), DEF);
			String string = StringUtils.join(list, '\n');
			return JSONUtils.fromJson(string);
		}
		@Override
		public JsonObject read(PacketBuffer packet) throws IOException {
			return Util.IO.readJsonFromBuffer(packet);
		}
		@Override
		public JsonObject read(ResourceLocation loc, InputStream stream) throws IOException {
			return Util.IO.readJsonFromStream(stream);
		}
		@Override
		public void write(File file, JsonObject data) throws IOException {
			if (data == null) return;
			String string = data.toString();
			IOUtils.write(string, new FileOutputStream(file), DEF);
		}
		@Override
		public void write(PacketBuffer packet, JsonObject data) throws IOException {
			packet.writeString(data.toString(), 32767);
		}
		@Override
		public void write(OutputStream stream, JsonObject data) throws IOException {
			Util.IO.writeJsonToStream(data, stream);
		}
		@Override
		public String filename(String id) {
			return id + ".json";
		}
	};

	public static final IFormat<CompoundNBT> NBT = new IFormat<CompoundNBT>() {
		@Override
		public CompoundNBT create() {
			return new CompoundNBT();
		}
		@Override
		public CompoundNBT read(File file) throws IOException {
			return CompressedStreamTools.read(file);
		}
		@Override
		public CompoundNBT read(PacketBuffer packet) throws IOException {
			return packet.readCompoundTag();
		}
		@Override
		public CompoundNBT read(ResourceLocation loc, InputStream stream) throws IOException {
			return CompressedStreamTools.readCompressed(stream);
		}
		@Override
		public void write(File file, CompoundNBT data) throws IOException {
			CompressedStreamTools.safeWrite(data, file);
		}
		@Override
		public void write(PacketBuffer packet, CompoundNBT data) throws IOException {
			packet.writeCompoundTag(data);
		}
		@Override
		public void write(OutputStream stream, CompoundNBT data) throws IOException {
			CompressedStreamTools.writeCompressed(data, stream);
		}
		@Override
		public String filename(String id) {
			return id + ".dat";
		}
	};

}
