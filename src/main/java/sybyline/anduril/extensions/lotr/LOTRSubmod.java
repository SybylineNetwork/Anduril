package sybyline.anduril.extensions.lotr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sybyline.anduril.extensions.Submod;
import sybyline.anduril.extensions.SubmodMarker;

@SubmodMarker("lotr:anduril")
public class LOTRSubmod extends Submod {

	private LOTRSubmod() {}

	public static final LOTRSubmod INSTANCE = new LOTRSubmod();
	public static final Logger LOGGER = LogManager.getLogger("Anduril(LOTR:Renewed)");

	@Override
	protected void submodSetup() {
		LOGGER.info("Submod setup!");
	}

	@Override
	protected void enqueIMCMessages() {
		LOGGER.info("Submod IMCMessages!");
	}

}
