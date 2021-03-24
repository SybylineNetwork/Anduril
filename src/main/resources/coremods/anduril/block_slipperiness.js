/*\
| | This coremod allows for more functionality in handling blockstates.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"IForgeBlockState.getSlipperiness(IWorldReader,BlockPos,Entity)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraftforge.common.extensions.IForgeBlockState",
				"methodName": "getSlipperiness",
				"methodDesc": "(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)F"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.FRETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new VarInsnNode(Opcodes.ALOAD, 1));
					add.add(new VarInsnNode(Opcodes.ALOAD, 2));
					add.add(new VarInsnNode(Opcodes.ALOAD, 3));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/GenericHooks", "IForgeBlockState_getSlipperiness", "(FLnet/minecraftforge/common/extensions/IForgeBlockState;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)F"));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		}
	};
}
