package sybyline.vinyarion.dynamiccontent.api;

public interface ResourceContext {

	/**
	 * Sets up the context. Calling more than once is UB, but it refreshes the index.json
	 */
	public void initialize();

//	/**
//	 * Refreshes the cached index.json.
//	 */
//	public void refreshIndex();

	/**
	 * Sets the printing of debug messages. This method is thread-safe.
	 * @param verbose True to log debug messages, false to disable them
	 */
	public void setVerbose(boolean verbose);

	/**
	 * @return Whether debug messages are being logged
	 */
	public boolean isVerbose();

	/**
	 * Sets the downloaded status of all active resources.
	 * @param downloadAll Whether all the resources currently active are to be redownloaded
	 * every time Resource.retrieve is called
	 */
	public void setDownloadAll(boolean downloadAll);

	/**
	 * Forces a lazy redownload of all active resources, pending the call of Resource.retrieve
	 */
	public void reDownloadAll();

	/**
	 * @return Whether all the resources currently active are to be redownloaded
	 * every time Resource.retrieve is called
	 */
	public boolean isDownloadAll();

	/**
	 * 
	 * @param id The resource name
	 * @return The path in the remote repository at which this resource exists
	 */
	public String getResourcePathFromID(String id);

	/**
	 * Gets an instance of a resource.
	 * Instances are cached, so it is permissible to check for equality with ==
	 * @param id The resource name
	 * @return The instance of the resource
	 */
	public Resource getResource(String id);

	/**
	 * @return The version of the remote repository
	 */
	public long getRemoteVersion();

}
