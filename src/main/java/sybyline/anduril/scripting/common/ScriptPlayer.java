package sybyline.anduril.scripting.common;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import sybyline.anduril.scripting.api.common.IScriptPlayer;
import sybyline.anduril.scripting.api.server.IPermission;
import sybyline.anduril.scripting.server.ServerManagement;

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
	public CompoundNBT data() {
		return parent.data(domain);
	}

	@Override
	public Vec3d pos() {
		Entity entity = check();
		return entity.getPositionVec();
	}

	@Override
	public void pos(Vec3d position) {
		check().setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
	}

	@Override
	public void move(Vec3d position) {
		check().move(MoverType.SELF, position);
	}

	@Override
	public Vec3d look() {
		return check().getLookVec();
	}

	@Override
	public void look(Vec3d look) {
		Entity entity = check();
		double x = look.getX(), z = look.getZ();
		double dist = MathHelper.sqrt(x * x + z * z);
		float rotationPitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(look.getY(), dist) * (double)(180F / (float)Math.PI))));
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
		return check().getGameProfile().getName();
	}

	@Override
	public UUID uuid() {
		return check().getGameProfile().getId();
	}

	@Override
	public void send_chat(String text) {
		check().sendMessage(new StringTextComponent(text));
	}

	@Override
	public boolean hasPermission(IPermission permission) {
		return ServerManagement.INSTANCE.permissions.hasPermission(parent.profile, permission);
	}

	@Override
	public void grantPermission(IPermission permission) {
		parent.permissions.add(permission.key());
	}

	@Override
	public void revokePermission(IPermission permission) {
		parent.permissions.remove(permission.key());
	}

	@Override
	public Entity getRawEntity() {
		return check();
	}

	@Override
	public LivingEntity getRawLiving() {
		return check();
	}

	@Override
	public PlayerEntity getRawPlayer() {
		return check();
	}

}
