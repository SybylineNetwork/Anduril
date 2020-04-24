package sybyline.anduril.scripting.api.client;

public interface IScriptWindow extends IScriptPane {

	public int window_width();

	public int window_height();

	public default int window_widthHalf() {
		return this.window_width() / 2;
	}

	public default int window_heightHalf() {
		return this.window_height() / 2;
	}

	public default int ww() {
		return this.window_width();
	}

	public default int wh() {
		return this.window_height();
	}

	public default int ww_h() {
		return this.window_widthHalf();
	}

	public default int wh_h() {
		return this.window_heightHalf();
	}

}
