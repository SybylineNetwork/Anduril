package sybyline.vinyarion.dynamiccontent.api;

import java.io.*;

public final class ContextUtil {

	private ContextUtil() {}

	//@Nonnull
	public static String readFirstLine(File file) {
		try (
			BufferedReader reader = new BufferedReader(new FileReader(file));
		) {
			String line = reader.readLine();
			return line;
		} catch (IOException e) {
			return "";
		}
	}

	public static void writeString(File file, String string) {
		try (
			FileWriter writer = new FileWriter(file);
		) {
			writer.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int read4Bytes(File file) {
		if (!file.exists())
			return -1;
		try (
			FileInputStream in = new FileInputStream(file);
		) {
			byte[] bytes = new byte[4];
			in.read(bytes);
			int version = 0;
			version = version | ((0xFF & bytes[0]) << 24);
			version = version | ((0xFF & bytes[1]) << 16);
			version = version | ((0xFF & bytes[2]) <<  8);
			version = version | ((0xFF & bytes[3]) <<  0);
			return version;
		} catch (IOException e) {
			return -1;
		}
	}

	public static void write4Bytes(File file, int i) {
		try (
			FileOutputStream out = new FileOutputStream(file);
		) {
			out.write(0xFF & (i >> 24));
			out.write(0xFF & (i >> 16));
			out.write(0xFF & (i >>  8));
			out.write(0xFF & (i >>  0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static long read8Bytes(File file) {
		if (!file.exists())
			return -1;
		try (
			FileInputStream in = new FileInputStream(file);
		) {
			byte[] bytes = new byte[8];
			in.read(bytes);
			int version = 0;
			version = version | ((0xFF & bytes[0]) << 56);
			version = version | ((0xFF & bytes[1]) << 48);
			version = version | ((0xFF & bytes[2]) << 40);
			version = version | ((0xFF & bytes[3]) << 32);
			version = version | ((0xFF & bytes[4]) << 24);
			version = version | ((0xFF & bytes[5]) << 16);
			version = version | ((0xFF & bytes[6]) <<  8);
			version = version | ((0xFF & bytes[7]) <<  0);
			return version;
		} catch (IOException e) {
			return -1;
		}
	}

	public static void write8Bytes(File file, long l) {
		try (
			FileOutputStream out = new FileOutputStream(file);
		) {
			out.write((int)(0xFF & (l >> 56)));
			out.write((int)(0xFF & (l >> 48)));
			out.write((int)(0xFF & (l >> 40)));
			out.write((int)(0xFF & (l >> 32)));
			out.write((int)(0xFF & (l >> 24)));
			out.write((int)(0xFF & (l >> 16)));
			out.write((int)(0xFF & (l >>  8)));
			out.write((int)(0xFF & (l >>  0)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getSemanticVersion(String string) {
		String[] strings = string.split("\\.");
		int version = 0;
		int size = Math.min(4, strings.length);
		for (int i = 0; i < size; i++) {
			try {
				int partial = 0xFF & Integer.parseUnsignedInt(strings[i]);
				int offset = (4 - i) << 3;
				version = version | (partial << offset);
			} catch(Exception e) {}
		}
		return version;
	}

	public static long getSemanticVersionL(String string) {
		String[] strings = string.split("\\.");
		long version = 0;
		int size = Math.min(8, strings.length);
		for (int i = 0; i < size; i++) {
			try {
				int partial = 0xFF & Integer.parseUnsignedInt(strings[i]);
				int offset = (8 - i) << 3;
				version = version | (partial << offset);
			} catch(Exception e) {}
		}
		return version;
	}

}
