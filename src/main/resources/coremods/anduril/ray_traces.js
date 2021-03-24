/*\
| | This coremod allows for more functionality in from ray tracing.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"ProjectileHelper.rayTrace()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.projectile.ProjectileHelper",
				"methodName": "func_221268_a",
				"methodDesc": "(Lnet/minecraft/entity/Entity;ZZLnet/minecraft/entity/Entity;Lnet/minecraft/util/math/RayTraceContext$BlockMode;ZLjava/util/function/Predicate;Lnet/minecraft/util/math/AxisAlignedBB;)Lnet/minecraft/util/math/RayTraceResult;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
					insns.clear();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
					insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 4));
					insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 6));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 7));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/common/modelbb/ProjectileHelperHooks", "rayTrace", "(Lnet/minecraft/entity/Entity;ZZLnet/minecraft/entity/Entity;Lnet/minecraft/util/math/RayTraceContext$BlockMode;ZLjava/util/function/Predicate;Lnet/minecraft/util/math/AxisAlignedBB;)Lnet/minecraft/util/math/RayTraceResult;"));
					insns.add(new InsnNode(Opcodes.ARETURN));
				return method;
			}
		},
		"ProjectileHelper.rayTraceEntities()1": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.projectile.ProjectileHelper",
				"methodName": "func_221273_a",
				"methodDesc": "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
					insns.clear();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 4));
					insns.add(new VarInsnNode(Opcodes.DLOAD, 5));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/common/modelbb/ProjectileHelperHooks", "rayTraceEntities", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"));
					insns.add(new InsnNode(Opcodes.ARETURN));
				return method;
			}
		},
		"ProjectileHelper.rayTraceEntities()2": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.projectile.ProjectileHelper",
				"methodName": "func_221269_a",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
					insns.clear();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 4));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 5));
					insns.add(new VarInsnNode(Opcodes.DLOAD, 6));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/common/modelbb/ProjectileHelperHooks", "rayTraceEntities", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"));
					insns.add(new InsnNode(Opcodes.ARETURN));
				return method;
			}
		}
	};
}
