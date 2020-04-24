package sybyline.vinyarion.dynamiccontent.api;

import java.io.File;

import sybyline.vinyarion.dynamiccontent.*;

public final class ContextProvider {

	public static final String GITHUB_BASE_PATH = "https://raw.githubusercontent.com/";

	private ContextProvider() {}

	public static ResourceContext create(File allContextsLocation, String domain, String project, String version) {
		return new FileResourceContext(allContextsLocation, domain, project, version);
	}

	public static ResourceContext createGithub(File allContextsLocation, String user, String resourceRepository, String projectName, String version) {
		return new FileResourceContext(allContextsLocation, GITHUB_BASE_PATH + user + '/' + resourceRepository, projectName, version);
	}

}
