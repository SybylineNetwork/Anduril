// Changes the enum SlabType
/*
Opcodes = Java.type("org.objectweb.asm.Opcodes");
MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

function initializeCoreMod() {
	return {
		"addSlabTypes": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.state.properties.SlabType"
			},
			"transformer": function (classNode) {
				classNode.methods.forEach(function (methodNode) {
					if (methodNode.name == "<clinit>") {
						for (var idx = 0; idx < methodNode.instructions.size(); idx++) {
							var insn = methodNode.instructions.get(idx);
							if (insn.getOpcode() == Opcodes.RETURN) {
								methodNode.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/test/Testing$Slabs", "init", "()V"));
								return classNode;
							}
						}
					}
				});
				return classNode;
			}
		}
	}
};

initializeCoreMod
*/