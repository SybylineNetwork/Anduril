package sybyline.vinyarion.dynamiccontent.api;

import java.io.InputStream;

public interface Resource {

	/**
	 * Downloads the resource from the remote repository.
	 * There should never be a need to call this in production code.
	 */
	public void download();

	/**
	 * Forces a lazy redownload of this resource.
	 */
	public void setDownloaded(boolean downloaded);

	/**
	 * @returns Whether this resource has been downloaded this session
	 */
	public boolean isDownloaded();

	/**
	 * @return Whether this resource exists as an entry in the index.json
	 */
	public boolean existsAsDownload();

	/**
	 * @return Whether this resource exists locally as a file already
	 */
	public boolean existsAsFile();

	/**
	 * Gets the resource, downloading if necessary.
	 * @return The resource stream, throwing a generic RuntimeException if it fails
	 */
	public InputStream retrieve();

}
