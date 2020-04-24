package sybyline.anduril.scripting.api.common;

public interface IScriptLiving extends IScriptEntity {

	// Statistics

	public float health();

	public void health(float health);

	public void heal(float amount);

	public void hurt(float damage);

	public float health_max();

}
