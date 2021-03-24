/*\
| | This coremod is an example/template for adding shadowing hooks.
\*/

function generateInstanceField(ret, coremodname, target, name, desc) {
	var Opcodes = Java.type("org.objectweb.asm.Opcodes");
	var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	ret[coremodname] = {
		"target": {
			"type": "CLASS",
			"name": target
		},
		"transformer": function(clazz) {
			clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, name, desc, null, null));
			return clazz;
		}
	};
}

function generateConstructorHook(ret, coremodname, target, instrumentID) {
	var shadow_hook_internal = "sybyline/anduril/util/rtc/shadow/Shadow";
	var shadow_hook_name = "_internal_constructor_hook";
	var shadow_hook_desc = "(Ljava/lang/Object;Ljava/lang/String;)V";
	var Opcodes = Java.type("org.objectweb.asm.Opcodes");
	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	ret[coremodname] = {
		"target": {
			"type": "CLASS",
			"name": target
		},
		"transformer": function(node) {
			node.methods.forEach(function(method) {
				if ("<init>".equals(method.name)) {
					var return_insns = [];
					method.instructions.forEach(function(insn) {
						if (insn.getOpcode() == Opcodes.RETURN) {
							return_insns.push(insn);
						}
					});
					return_insns.forEach(function(returnInsn) {
						InsnList insns = new InsnList();
						insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
						insns.add(new LdcInsnNode(instrumentID));
						insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, shadow_hook_internal, shadow_hook_name, shadow_hook_desc));
						method.instructions.insertBefore(returnInsn, insns);
					});
				}
			});
			return node;
		}
	};
}
