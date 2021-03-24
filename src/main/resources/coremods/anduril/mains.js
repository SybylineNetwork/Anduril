/*\
| | This coremod allows for more functionality before the game loads.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"Client.main": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.main.Main",
				"methodName": "main",
				"methodDesc": "([Ljava/lang/String;)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var inst = new InsnList();
					inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
					inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Boot", "boot_client", "([Ljava/lang/String;)V", false));
				insns.insertBefore(insns.getFirst(), inst);
				return method;
			}
		},
		"Server.main": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.server.MinecraftServer",
				"methodName": "main",
				"methodDesc": "([Ljava/lang/String;)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var inst = new InsnList();
					inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
					inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Boot", "boot_server", "([Ljava/lang/String;)V", false));
				insns.insertBefore(insns.getFirst(), inst);
				return method;
			}
		},
		"Server.reload": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.server.management.PlayerList",
				"methodName": "func_193244_w",
				"methodDesc": "()V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.RETURN);
				var add = new InsnList();
					add.add(new VarInsnNode(Opcodes.ALOAD, 0));
					add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Boot", "boot_server_reload", "(Lnet/minecraft/server/management/PlayerList;)V", false));
				insns.insertBefore(ret_insn, add);
				return method;
			}
		}
	};
}
