/*\
| | This coremod allows for more functionality in the base Entity classes.
| | You can do things with EntityExtra.of(Entity). This method eagerly
| | populates this field and is thread-safe. There is good reason to use
| | this method over Capabilities, and the use of Capabilities was deemed
| | to be too costly for the purposes at hand.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	
	hookProjectileCreation = function(type) {
		if (type == "hook_tool_origin_entity") {
			return new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", type, "(Lnet/minecraft/entity/projectile/AbstractArrowEntity;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;");
		} else {
			return new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", type, "(Lnet/minecraft/entity/projectile/AbstractArrowEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;");
		}
	};
	
	return {
		"Entity.anduril_entity_extra": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.Entity"
			},
			"transformer": function(clazz) {
				clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "anduril_entity_extra", "Lsybyline/anduril/coremods/EntityExtra;", null, null));
				return clazz;
			}
		},
		"Entity.tick()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.Entity",
				"methodName": "func_70030_z",
				"methodDesc": "()V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.RETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "of", "(Lnet/minecraft/entity/Entity;)Lsybyline/anduril/coremods/EntityExtra;"));
					add.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "sybyline/anduril/coremods/EntityExtra", "tick", "()V"));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		},
		"Entity.isInvisible()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.Entity",
				"methodName": "func_82150_aj",
				"methodDesc": "()Z"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var iret_insn = ASMAPI.findFirstInstruction(method, Opcodes.IRETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "of", "(Lnet/minecraft/entity/Entity;)Lsybyline/anduril/coremods/EntityExtra;"));
					add.add(new InsnNode(Opcodes.SWAP));
					add.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "sybyline/anduril/coremods/EntityExtra", "hookIsInvisible", "(Z)Z"));
				insns.insertBefore(iret_insn, add);
				return method;
			}
		},
		"Entity.isInvisibleToPlayer(PlayerEntity)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.Entity",
				"methodName": "func_98034_c",
				"methodDesc": "(Lnet/minecraft/entity/player/PlayerEntity;)Z"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var iret_insn = ASMAPI.findFirstInstructionBefore(method, Opcodes.IRETURN, method.instructions.size() - 1);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "of", "(Lnet/minecraft/entity/Entity;)Lsybyline/anduril/coremods/EntityExtra;"));
					add.add(new InsnNode(Opcodes.SWAP));
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "sybyline/anduril/coremods/EntityExtra", "hookIsInvisibleToPlayer", "(ZLnet/minecraft/entity/player/PlayerEntity;)Z"));
				insns.insertBefore(iret_insn, add);
				return method;
			}
		},
		"Entity.read(CompoundNBT)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.Entity",
				"methodName": "func_70020_e",
				"methodDesc": "(Lnet/minecraft/nbt/CompoundNBT;)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var aload_insn = ASMAPI.findFirstInstruction(method, Opcodes.ALOAD);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "readextra", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/CompoundNBT;)V"));
				insns.insertBefore(aload_insn, add);
				return method;
			}
		},
		"Entity.write(CompoundNBT)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.Entity",
				"methodName": "func_189511_e",
				"methodDesc": "(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var aload_insn = ASMAPI.findFirstInstruction(method, Opcodes.ALOAD);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "writeextra", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/CompoundNBT;)V"));
				insns.insertBefore(aload_insn, add);
				return method;
			}
		},
		"LivingEntity.applyArmorCalculations(DamageSource,float)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.LivingEntity",
				"methodName": "func_70655_b",
				"methodDesc": "(Lnet/minecraft/util/DamageSource;F)F"
			},
			"transformer": function(method) {
				var damagearmor = ASMAPI.findFirstMethodCall(method, ASMAPI.MethodType.VIRTUAL, "net/minecraft/entity/LivingEntity", ASMAPI.mapMethod("func_70675_k"), "(F)V");
					damagearmor.setOpcode(Opcodes.INVOKESTATIC);
					damagearmor.owner = "sybyline/anduril/common/skill/SkillInstance";
					damagearmor.name = "hook_damageArmor";
					damagearmor.desc = "(Lnet/minecraft/entity/LivingEntity;F)V";
				var combatrules = ASMAPI.findFirstMethodCall(method, ASMAPI.MethodType.STATIC, "net/minecraft/util/CombatRules", ASMAPI.mapMethod("func_189427_a"), "(FFF)F");
					combatrules.owner = "sybyline/anduril/common/skill/SkillInstance";
					combatrules.name = "hook_getDamageAfterAbsorb";
				return method;
			}
		},
		"CrossbowItem.createArrow(World,LivingEntity,ItemStack,ItemStack)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.CrossbowItem",
				"methodName": "func_200887_a",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.ARETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 2));
					add.add(hookProjectileCreation("hook_tool_origin"));
					add.add(new VarInsnNode(Opcodes.ALOAD, 3));
					add.add(hookProjectileCreation("hook_material_origin"));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		},
		"ProjectileHelper.fireArrow(LivingEntity,ItemStack,float)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.projectile.ProjectileHelper",
				"methodName": "func_221272_a",
				"methodDesc": "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/projectile/AbstractArrowEntity;"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.ARETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(hookProjectileCreation("hook_tool_origin_entity"));
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(hookProjectileCreation("hook_material_origin"));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		},
		"BowItem.onPlayerStoppedUsing(ItemStack,World,LivingEntity,int)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.BowItem",
				"methodName": "func_77615_a",
				"methodDesc": "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var customeArrow_insn = ASMAPI.findFirstMethodCall(method, ASMAPI.MethodType.VIRTUAL, "net/minecraft/item/BowItem", "customeArrow", "(Lnet/minecraft/entity/projectile/AbstractArrowEntity;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;");
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(hookProjectileCreation("hook_tool_origin"));
					add.add(new VarInsnNode(Opcodes.ALOAD, 7));
					add.add(hookProjectileCreation("hook_material_origin"));
				insns.insert(customeArrow_insn, add);
				return method;
			}
		}
	};
}
