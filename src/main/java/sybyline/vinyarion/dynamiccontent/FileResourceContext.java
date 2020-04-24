package sybyline.vinyarion.dynamiccontent;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.google.gson.*;

import sybyline.vinyarion.dynamiccontent.api.*;

public class FileResourceContext implements ResourceContext {

	public static final String INDEX_JSON = "index.json";
	public static final JsonParser PARSE = new JsonParser();

	public FileResourceContext(File allContextsLocation, String domain, String project, String version) {
		String versionedproject = project + '/' + version + '/';
		this.root = new File(allContextsLocation, versionedproject);
		this.domain = domain;
		this.project = project;
		this.version = version;
		this.internal_cache = new HashMap<String, Resource>();
		this.internal_hash = ((((long)domain.hashCode()) << 32) ^ (((long)project.hashCode()) << 32)) | (((long)version.hashCode()) << 00);
		this.internal_verbose = false;
		this.internal_download_all = false;
		this.internal_branch_url_stub = domain + '/' + versionedproject;
		this.internal_json_index = null;
		this.internal_repository_version = 0;
		this.internal_json_index_old = null;
	}

	public void setVerbose(boolean verbose) {
		this.internal_verbose = verbose;
	}

	public boolean isVerbose() {
		return internal_verbose;
	}

	public void setDownloadAll(boolean downloadAll) {
		this.internal_download_all = downloadAll;
	}

	public void reDownloadAll() {
		internal_cache.values().forEach(resource -> resource.setDownloaded(false));
	}

	public boolean isDownloadAll() {
		return internal_download_all;
	}

	public void initialize() {
		if (root.exists()) {
			if (!root.isDirectory()) {
				throw new RuntimeException("File already exists as a non-directory!\nPath is: " + root.getAbsolutePath());
			}
		} else {
			if (!root.mkdirs()) {
				throw new RuntimeException("Folder could not be created!\nPath is: " + root.getAbsolutePath());
			}
		}
		try {
			String path = internal_branch_url_stub + INDEX_JSON;
			File indexFile = new File(root, INDEX_JSON);
			if (internal_verbose) System.out.println("Dynamic content: Atempting to retrieve index: " + path);
			URL newJsonPath = new URL(path);
			try (
				FileReader oldIndex = new FileReader(indexFile);
			) {
				JsonObject jsonOld = PARSE.parse(oldIndex).getAsJsonObject();
				this.internal_json_index_old = jsonOld;
			} catch (Exception e) {
			}
			try (
				BufferedReader newIndex = new BufferedReader(new InputStreamReader(newJsonPath.openStream()));
			) {
				JsonObject jsonNew = PARSE.parse(newIndex).getAsJsonObject();
				this.internal_json_index = jsonNew;
				JsonElement versionResource = jsonNew.get("version-resource");
				this.internal_repository_version = ContextUtil.getSemanticVersionL(versionResource == null ? null : versionResource.getAsString());
				String jsonString = jsonNew.toString();
				if (internal_verbose) System.out.println("Dynamic content: retrieved index: " + jsonString);
				ContextUtil.writeString(indexFile, jsonString);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final File root;
	public final String domain;
	public final String project;
	public final String version;

	private final Map<String, Resource> internal_cache;
	final long internal_hash;
	private boolean internal_verbose;
	private boolean internal_download_all;
	private String internal_branch_url_stub;
	private JsonObject internal_json_index;
	private long internal_repository_version;
	@SuppressWarnings("unused")
	private JsonObject internal_json_index_old;

	//@Nullable
	public String getResourcePathFromID(String id) {
		if (internal_json_index == null)
			throw new NullPointerException("index");
		JsonObject entries = internal_json_index.getAsJsonObject("entries");
		if (entries == null)
			throw new NullPointerException("entries");
		JsonElement element = entries.get(id);
		if (element == null)
			return null;
		return internal_branch_url_stub + element.getAsString();
	}

	//@Nonnull
	public Resource getResource(String id) {
		return internal_cache.computeIfAbsent(id, _id -> new FileResource(this, _id));
	}

	public long getRemoteVersion() {
		return internal_repository_version;
	}

}
