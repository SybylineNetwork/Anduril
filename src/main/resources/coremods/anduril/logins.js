/*\
| | This coremod allows for more functionality before the connection is finalized.
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"Server.login": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.network.login.ServerLoginNetHandler",
				"methodName": "tick",
				"methodDesc": "()V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var inst = new InsnList();
				var lbl = new LabelNode();
				var n = new VarInsnNode(Opcodes.ALOAD, 0);
					inst.add(n);
					inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Conn$Server", "conn_server", "(Lnet/minecraft/network/login/ServerLoginNetHandler;)Z", false));
					inst.add(new JumpInsnNode(Opcodes.IFEQ, lbl));
					inst.add(new InsnNode(Opcodes.RETURN));
					inst.add(lbl);
				insns.insertBefore(insns.getFirst(), inst);
				return method;
			}
		},
		"Client.login": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.network.login.ClientLoginNetHandler",
				"methodName": "handleCustomPayloadLogin",
				"methodDesc": "(Lnet/minecraft/network/login/server/SCustomPayloadLoginPacket;)V"
			},
			"transformer": function(method) {
				var insns = method.instructions;
				var inst = new InsnList();
				var lbl = new LabelNode();
				var n1 = new VarInsnNode(Opcodes.ALOAD, 0);
				var n2 = new VarInsnNode(Opcodes.ALOAD, 1);
					inst.add(n1);
					inst.add(n2);
					inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Conn$Client", "conn_client", "(Lnet/minecraft/client/network/login/ClientLoginNetHandler;Lnet/minecraft/network/login/server/SCustomPayloadLoginPacket;)Z", false));
					inst.add(new JumpInsnNode(Opcodes.IFEQ, lbl));
					inst.add(new InsnNode(Opcodes.RETURN));
					inst.add(lbl);
				insns.insertBefore(insns.getFirst(), inst);
				return method;
			}
		},
		"Common.loginhandler": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.network.NetworkManager",
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/network/PacketDirection;)V"
			},
			"transformer": function(method) {
				var ret_insn = ASMAPI.findFirstInstruction(method, Opcodes.RETURN);
				var insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/boot/Conn", "conn_net", "(Lnet/minecraft/network/NetworkManager;)V", false));
				method.instructions.insertBefore(ret_insn, insns);
				return method;
				return method;
			}
		}
	};
}
