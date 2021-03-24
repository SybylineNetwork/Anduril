/*\
| | This coremod allows for more functionality in advancement rewards.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"Deserializer.new()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.advancements.AdvancementRewards$Deserializer",
				"methodName": "<init>",
				"methodDesc": "()V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.RETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/ExtraRewards$ExtraDeserializer", "hook_constructor", "(Lnet/minecraft/advancements/AdvancementRewards$Deserializer;)Lnet/minecraft/advancements/AdvancementRewards$Deserializer;"));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		}
	};
}
