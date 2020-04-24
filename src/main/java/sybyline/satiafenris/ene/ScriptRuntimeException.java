package sybyline.satiafenris.ene;

public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 2021713357117344886L;

	public ScriptRuntimeException() {
	}

	public ScriptRuntimeException(String message) {
		super(message);
	}

	public ScriptRuntimeException(Throwable cause) {
		super(cause);
	}

	public ScriptRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
