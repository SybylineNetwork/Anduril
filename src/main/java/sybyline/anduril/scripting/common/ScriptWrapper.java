package sybyline.anduril.scripting.common;

import java.io.*;
import java.util.function.BiFunction;
import org.apache.commons.io.FileUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import sybyline.anduril.scripting.api.ClientScriptExtensions;
import sybyline.anduril.scripting.api.CommonScriptExtensions;
import sybyline.anduril.scripting.api.ServerScriptExtensions;
import sybyline.anduril.scripting.server.ScriptServer;
import sybyline.anduril.util.Util;
import sybyline.anduril.util.data.IFormat;
import sybyline.satiafenris.ene.Script;
import sybyline.satiafenris.ene.ScriptRuntimeException;

public abstract class ScriptWrapper<T> {

	public static Script newScript() {
		return Script.graalOrNashorn();
	}

	public ScriptWrapper(String name, String source) {
		this.name = name;
		this.source = source;
	}

	public String domain = Util.MINECRAFT;
	public String name;
	public final String source;
	public final Script script = newScript();

	// Must call setupInternal()
	public abstract T setupWithContext(T context) throws ScriptRuntimeException;

	protected abstract void bindVariables() throws ScriptRuntimeException;

	protected abstract LogicalSide side();

	protected final void setupInternal() throws ScriptRuntimeException {
		script.strict().allowClasses(
			"java.math.*",
			"sybyline.anduril.util.math.*",
			Object.class
		);
		if (side() != LogicalSide.CLIENT) {
			if (CommonScripting.INSTANCE.areServerAddonsEnabled()) {
				CommonScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
				ServerScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
			}
			this.script.bind("server", ScriptServer.INSTANCE);
		} else {
			if (CommonScripting.INSTANCE.areClientAddonsEnabled()) {
				CommonScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
				ClientScriptExtensions.forEach((name, function) -> script.bind(name, function.apply(this.script)));
			}
		}
		this.script.bind("util", ScriptUtil.INSTANCE);
		this.bindVariables();
		this.script.eval(source);
	}

	public static final <Wrapper extends ScriptWrapper<T>, T> IFormat<Wrapper> formatOf(BiFunction<String, String, Wrapper> constructor) {
		return new ScriptWrapperFormat<Wrapper>(constructor);
	}
	
	private static final class ScriptWrapperFormat<Wrapper extends ScriptWrapper<?>> implements IFormat<Wrapper> {

		private ScriptWrapperFormat(BiFunction<String, String, Wrapper> constructor) {
			this.constructor = constructor;
		}

		private final BiFunction<String, String, Wrapper> constructor;

		@Override
		public Wrapper create() {
			return constructor.apply(null, "");
		}

		@Override
		public Wrapper read(File file) throws IOException {
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			String commandname = index == -1 ? filename : filename.substring(0, index);
			String source = Util.IO.readString(new FileInputStream(file));
			return constructor.apply(commandname, source);
		}

		@Override
		public Wrapper read(PacketBuffer packet) throws IOException {
			String commandname = packet.readString(64);
			String source = packet.readString(32767);
			return constructor.apply(commandname, source);
		}

		@Override
		public Wrapper read(ResourceLocation loc, InputStream stream) throws IOException {
			String locname = loc.getPath();
			int indexS = locname.lastIndexOf('/');
			int indexE = locname.lastIndexOf('.');
			if (indexS == -1) indexS = 0;
			String commandname = indexE == -1 ? locname.substring(indexS) : locname.substring(indexS, indexE);
			String source = Util.IO.readString(stream);
			return constructor.apply(commandname, source);
		}

		@Override
		public void write(File file, Wrapper data) throws IOException {
			FileUtils.write(file, data.source, Util.IO.DEFAULT, false);
		}

		@Override
		public void write(PacketBuffer packet, Wrapper data) throws IOException {
			packet.writeString(data.name, 64);
			packet.writeString(data.source, 32767);
		}

		@Override
		public void write(OutputStream stream, Wrapper data) throws IOException {
			Util.IO.writeString(data.source, stream);
		}

		@Override
		public String filename(String id) {
			return id + ".js";
		}

	}

}
