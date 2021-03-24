/*\
| | This coremod allows custom items not registered to exist UwU
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	return {
		"Entity.anduril_entity_extra": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.item.ItemStack"
			},
			"transformer": function(clazz) {
				clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "anduril_itemstack_extra", "Lsybyline/anduril/coremods/ItemStackExtra;", null, null));
				return clazz;
			}
		},
		"ItemStack.write(CompoundNBT)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_77955_b",
				"methodDesc": "(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;"
			},
			"transformer": function(method) {
				method.instructions.clear();
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "write", "(Lnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/CompoundNBT;", false));
					insns.add(new InsnNode(Opcodes.ARETURN));
				method.instructions.add(insns);
				return method;
			}
		},
		"ItemStack.getAttributeModifiers": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_77973_b",
				"methodDesc": "()Lnet/minecraft/item/Item;"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "getItemHook", "(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/Item;", false));
				method.instructions.insertBefore(ASMAPI.findFirstInstruction(method, Opcodes.ARETURN), insns);
				return method;
			}
		},
		"ItemStack.getDisplayName": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_200301_q",
				"methodDesc": "()Lnet/minecraft/util/text/ITextComponent;"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "getDisplayNameExtra", "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/text/ITextComponent;", false));
					insns.add(new InsnNode(Opcodes.DUP));
				var lbl = new LabelNode();
					insns.add(new JumpInsnNode(Opcodes.IFNULL, lbl));
					insns.add(new InsnNode(Opcodes.ARETURN));
					insns.add(lbl);
					insns.add(new InsnNode(Opcodes.POP));
				method.instructions.insertBefore(method.instructions.getFirst(), insns);
				return method;
			}
		},
		"ItemStack.getUseAction": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_77661_b",
				"methodDesc": "()Lnet/minecraft/item/UseAction;"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "getUseActionExtra", "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/UseAction;", false));
					insns.add(new InsnNode(Opcodes.DUP));
				var lbl = new LabelNode();
					insns.add(new JumpInsnNode(Opcodes.IFNULL, lbl));
					insns.add(new InsnNode(Opcodes.ARETURN));
					insns.add(lbl);
					insns.add(new InsnNode(Opcodes.POP));
				method.instructions.insertBefore(method.instructions.getFirst(), insns);
				return method;
			}
		},
		"ItemStack.getUseDuration": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_77661_b",
				"methodDesc": "()I"
			},
			"transformer": function(method) {
				var iret_insn = ASMAPI.findFirstInstruction(method, Opcodes.IRETURN);
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "getUseDurationExtra", "(ILnet/minecraft/item/ItemStack;)I", false));
				method.instructions.insertBefore(iret_insn, insns);
				return method;
			}
		},
		"ItemParser.<init>": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.command.arguments.ItemParser",
				"methodName": "<init>",
				"methodDesc": "(Lcom/mojang/brigadier/StringReader;Z)V"
			},
			"transformer": function(method) {
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.RETURN);
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "hookItemParserConstructor", "(Lnet/minecraft/command/arguments/ItemParser;)V", false));
				method.instructions.insertBefore(ret_insn, insns);
				return method;
			}
		},
		"PlayerEntity.<init>": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.entity.player.PlayerEntity",
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/world/World;Lcom/mojang/authlib/GameProfile;)V"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/EntityExtra", "hookPlayerEntityConstructor", "(Lnet/minecraft/entity/player/PlayerEntity;)V", false));
				method.instructions.insertBefore(ASMAPI.findFirstInstruction(method, Opcodes.RETURN), insns);
				return method;
			}
		},
		"RepairItemRecipe.<init>": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.crafting.RepairItemRecipe",
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/util/ResourceLocation;)V"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/CustomRecipes", "hookRepairItemRecipeConstructor", "(Lnet/minecraft/item/crafting/RepairItemRecipe;)V", false));
				method.instructions.insertBefore(ASMAPI.findFirstInstruction(method, Opcodes.RETURN), insns);
				return method;
			}
		}
	};
}
