package sybyline.anduril.scripting.api.client;

import sybyline.anduril.scripting.api.common.IMCResource;

public interface IScriptWidgetTexture {

	public void draw(IScriptGui<?> screen, int x, int y, int w, int h);

	static IScriptWidgetTexture of(IMCResource resource) {
		return new IScriptWidgetTexture() {
			@Override
			public void draw(IScriptGui<?> screen, int x, int y, int w, int h) {
				screen.bind_resource(resource);
				screen.draw_blitStretch(x, y, w, h, 0, 0, 1, 1, 1, 1);
			}
		};
	}

}
