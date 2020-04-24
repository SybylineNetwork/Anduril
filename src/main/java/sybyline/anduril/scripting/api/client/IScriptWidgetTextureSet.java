package sybyline.anduril.scripting.api.client;

import sybyline.anduril.scripting.api.common.IMCResource;

public interface IScriptWidgetTextureSet {

	/**
	 * @param u x coord of upper left pixel
	 * @param v y coord of upper left pixel
	 * @param s width of texture part
	 * @param t height of texture part
	 */
	public IScriptWidgetTexture getSubTexture(int u, int v, int s, int t);

	static IScriptWidgetTextureSet of(IMCResource resource, int w, int h) {
		float w_f = w, h_f = h;
		return new IScriptWidgetTextureSet() {
			@Override
			public IScriptWidgetTexture getSubTexture(int u, int v, int s, int t) {
				float u_f = u, v_f = v, s_f = s, t_f = t;
				return new IScriptWidgetTexture() {
					@Override
					public void draw(IScriptGui<?> screen, int x, int y, int w, int h) {
						screen.bind_resource(resource);
						screen.draw_blitStretch(x, y, w, h, u_f, v_f, s_f, t_f, w_f, h_f);
					}
				};
			}
		};
	}

}
