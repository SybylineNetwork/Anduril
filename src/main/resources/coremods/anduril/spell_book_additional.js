/*\
| | This coremod allows spell books to have nicer text
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	return {
		"EditBookScreen.render": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.gui.screen.EditBookScreen",
				"methodName": "render",
				"methodDesc": "(IIF)V"
			},
			"transformer": function(method) {
				var target = ASMAPI.findFirstMethodCall(method, ASMAPI.MethodType.SPECIAL, "net/minecraft/client/gui/screen/EditBookScreen", ASMAPI.mapMethod("func_214222_m"), "(Ljava/lang/String;)V");
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/screen/EditBookScreen", ASMAPI.mapField("field_214233_b"), "Lnet/minecraft/item/ItemStack;"));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/client/overlay/HoverOverlay", "hookSpellBookGui", "(Ljava/lang/String;Lnet/minecraft/item/ItemStack;)Ljava/lang/String;", false));
				method.instructions.insertBefore(target, insns);
				return method;
			}
		}
	};
}
