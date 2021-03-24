/*\
| | This coremod allows for more functionality in pistons.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"PistonBlock.doMove()": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.PistonBlock",
				"methodName": "func_176319_a",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Z)Z"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var constructor_insn = ASMAPI.findFirstInstruction(method, Opcodes.INVOKESPECIAL);
				var add = new InsnList();
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/PistonExtra", "hook_constructor", "(Lnet/minecraft/block/PistonBlockStructureHelper;)Lnet/minecraft/block/PistonBlockStructureHelper;"));
				insns.insert(constructor_insn, add);
				return method;
			}
		},
		"PistonBlock.checkForMove(World,BlockPos,BlockState)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.PistonBlock",
				"methodName": "func_176316_e",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var constructor_insn = ASMAPI.findFirstInstruction(method, Opcodes.NEW).getNext().getNext().getNext().getNext().getNext().getNext();
				var add = new InsnList();
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/coremods/PistonExtra", "hook_constructor", "(Lnet/minecraft/block/PistonBlockStructureHelper;)Lnet/minecraft/block/PistonBlockStructureHelper;"));
				insns.insert(constructor_insn, add);
				return method;
			}
		}
	};
}
