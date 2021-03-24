/*\
| | This coremod allows items to have more control over the behavior of ItemStack.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
/*		"ItemStack.getAttributeModifiers()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_111283_C",
				"methodDesc": "(Lnet/minecraft/inventory/EquipmentSlotType;)Lcom/google/common/collect/Multimap;"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/ItemModifier", "addCustomAttributes", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/EquipmentSlotType;Lcom/google/common/collect/Multimap;)V", false));
				method.instructions.insertBefore(ASMAPI.findFirstInstruction(method, Opcodes.ARETURN), insns);
				return method;
			}
		},*/
		"ItemStack.getMaxDamage()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.ItemStack",
				"methodName": "func_77958_k",
				"methodDesc": "()I"
			},
			"transformer": function(method) {
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/extensions/forge/IAndurilItemStack", "getMaxDamage", "(ILnet/minecraft/item/ItemStack;)I", false));
				method.instructions.insertBefore(ASMAPI.findFirstInstruction(method, Opcodes.IRETURN), insns);
				return method;
			}
		}
	};
}
