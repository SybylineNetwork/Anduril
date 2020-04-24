package sybyline.anduril.scripting.common;

import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.api.data.IScriptData;
import sybyline.anduril.util.math.Vector;

public class ScriptPlayer implements IScriptPlayer {

	public ScriptPlayer(String domain, ScriptPlayerData parent, Supplier<PlayerEntity> player) {
		this.domain = domain;
		this.parent = parent;
		this.player_supplier = player;
	}

	private final String domain;
	private final ScriptPlayerData parent;
	private final Supplier<PlayerEntity> player_supplier;
	private PlayerEntity player;

	private PlayerEntity check() {
		if (!is_online())
			throw new UnsupportedOperationException("Script failed to check if the player was logged on!");
		return player;
	}

	@Override
	public boolean is_online() {
		return (player = player_supplier.get()) != null;
	}

	@Override
	public IScriptData data() {
		return parent.data(domain);
	}

	@Override
	public Vector pos() {
		Entity entity = check();
		return new Vector(entity.getPosX(), entity.getPosY(), entity.getPosZ());
	}

	@Override
	public void pos(Vector position) {
		check().setPositionAndUpdate(position.x(), position.y(), position.z());
	}

	@Override
	public void move(Vector position) {
		check().move(MoverType.SELF, new Vec3d(position.x(), position.y(), position.z()));
	}

	@Override
	public Vector look() {
		Vec3d look = check().getLookVec();
		return new Vector(look.x, look.y, look.z);
	}

	@Override
	public void look(Vector look) {
		Entity entity = check();
		double x = look.x(), z = look.z();
		double dist = MathHelper.sqrt(x * x + z * z);
		float rotationPitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(look.y(), dist) * (double)(180F / (float)Math.PI))));
	    float rotationYaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(z, x) * (double)(180F / (float)Math.PI)) - 90.0F);
	    entity.setPositionAndRotation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), rotationYaw, rotationPitch);
	}

	@Override
	public float health() {
		return check().getHealth();
	}

	@Override
	public void health(float health) {
		check().setHealth(health);
	}

	@Override
	public void heal(float amount) {
		check().heal(amount);
	}

	@Override
	public void hurt(float damage) {
		check().attackEntityFrom(DamageSource.GENERIC, damage);
	}

	@Override
	public float health_max() {
		return check().getMaxHealth();
	}

	@Override
	public String name() {
		return check().getScoreboardName();
	}

	@Override
	public void send_chat(String text) {
		check().sendMessage(new StringTextComponent(text));
	}

}
