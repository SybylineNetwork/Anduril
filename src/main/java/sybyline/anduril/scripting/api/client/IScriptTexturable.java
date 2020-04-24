package sybyline.anduril.scripting.api.client;

public interface IScriptTexturable<T extends IScriptTexturable<T>> {

	public T withTexture(IScriptWidgetTexture texture);

}
