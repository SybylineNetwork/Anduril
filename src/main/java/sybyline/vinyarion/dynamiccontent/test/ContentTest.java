package sybyline.vinyarion.dynamiccontent.test;

import java.io.*;

import sybyline.vinyarion.dynamiccontent.api.*;

public class ContentTest {

	public static final File TEST_ROOT = new File(".");
	public static final String TEST_USER = "VinyarionHyarmendacil";
	public static final String TEST_REPOSITORY = "dynamic-content";
	public static final String TEST_PROJECT = "dynamic-content";
	public static final String TEST_VERION = "0.0.0";

	public static void main(String[] args) {
		
		// Create the context -- this is a permanent object
		ResourceContext context = ContextProvider.createGithub(TEST_ROOT, TEST_USER, TEST_REPOSITORY, TEST_PROJECT, TEST_VERION);
		// Set whether the context logs info -- may call at any time
		context.setVerbose(true);
		// Initialize the context -- only called once
		context.initialize();
		
		// Get a resource
		Resource resource = context.getResource("test");
		// Retrieve downloads the file if it does not exist locally
		InputStream stream = resource.retrieve();
		new BufferedReader(new InputStreamReader(stream)).lines().forEach(System.out::println);
			
		Resource bad = context.getResource("nonexistant");
		System.out.println("Does a nonexistant resource exist: " + bad.existsAsDownload());
		
		try {
			bad.retrieve();
			System.out.println("Incorrect behavior: retrieved nonexistant resource");
		} catch (Exception e) {
			System.out.println("Correct behavior: failed to retrieve nonexistant resource");
		}
		
	}

}
