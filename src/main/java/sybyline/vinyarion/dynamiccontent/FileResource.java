package sybyline.vinyarion.dynamiccontent;

import java.io.*;
import java.net.URL;
import java.util.UUID;

import sybyline.vinyarion.dynamiccontent.api.*;

public class FileResource implements Resource {

	FileResource (FileResourceContext context, String id) {
		this.context = context;
		this.id = id;
		this.path = context.getResourcePathFromID(id);
		this.hash = new UUID(context.internal_hash, (((long)id.hashCode()) << 32));
		this.file = new File(context.root, hash.toString());
		this.file_version = new File(context.root, hash.toString() + ".ver");
		this.internal_file_version_long = ContextUtil.read8Bytes(file_version);
	}

	public final ResourceContext context;
	public final String id;
	public final String path;
	public final UUID hash;
	public final File file;
	public final File file_version;
	private long internal_file_version_long;
	private boolean internal_downloaded = false;

	private static final int BUFFER_SIZE = 1024;

	public void download() {
		if (path == null) {
			if (context.isVerbose()) {
				System.out.println("Null resource: " + id);
			}
			return;
		}
		if (context.isVerbose()) {
			System.out.println("Downloading resource: " + id + " -> " + path);
		}
		try (
			BufferedInputStream in = new BufferedInputStream(new URL(path).openStream());
			FileOutputStream out = new FileOutputStream(file);
		) {
		    byte data[] = new byte[BUFFER_SIZE];
		    int byteContent;
		    while ((byteContent = in.read(data, 0, BUFFER_SIZE)) != -1) {
		        out.write(data, 0, byteContent);
		    }
		    ContextUtil.write8Bytes(file_version, internal_file_version_long = context.getRemoteVersion());
		    internal_downloaded = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setDownloaded(boolean downloaded) {
		this.internal_downloaded = downloaded;
	}

	public boolean isDownloaded() {
		return internal_downloaded;
	}

	public boolean existsAsDownload() {
		return path != null;
	}

	public boolean existsAsFile() {
		return file.exists() && file.isFile() && file.canRead();
	}

	public InputStream retrieve() {
		synchronized (this) {
			if (existsAsFile() ? needsUpdate() : true) {
				this.download();
			} else if (context.isVerbose()) System.out.println("Using cached resource: " + id + " -> " + file.getAbsolutePath());
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean needsUpdate() {
		if (context.isDownloadAll()) {
			return true;
		}
		if (internal_downloaded) {
			return true;
		}
		return internal_file_version_long < context.getRemoteVersion();
	}

}
