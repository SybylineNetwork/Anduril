ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
Opcodes = Java.type("org.objectweb.asm.Opcodes");

AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");
IincInsnNode = Java.type("org.objectweb.asm.tree.IincInsnNode");
InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
InsnList = Java.type("org.objectweb.asm.tree.InsnList");
InvokeDynamicInsnNode = Java.type("org.objectweb.asm.tree.InvokeDynamicInsnNode");
JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
LocalVariableAnnotationNode = Java.type("org.objectweb.asm.tree.LocalVariableAnnotationNode");
LocalVariableNode = Java.type("org.objectweb.asm.tree.LocalVariableNode");
LookupSwitchInsnNode = Java.type("org.objectweb.asm.tree.LookupSwitchInsnNode");
MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
MultiANewArrayInsnNode = Java.type("org.objectweb.asm.tree.MultiANewArrayInsnNode");
TableSwitchInsnNode = Java.type("org.objectweb.asm.tree.TableSwitchInsnNode");
TryCatchBlockNode = Java.type("org.objectweb.asm.tree.TryCatchBlockNode");
TypeAnnotationNode = Java.type("org.objectweb.asm.tree.TypeAnnotationNode");
TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");

MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");
ParameterNode = Java.type("org.objectweb.asm.tree.ParameterNode");

Attribute = Java.type("org.objectweb.asm.Attribute");
Handle = Java.type("org.objectweb.asm.Handle");
Label = Java.type("org.objectweb.asm.Label");
Type = Java.type("org.objectweb.asm.Type");
TypePath = Java.type("org.objectweb.asm.TypePath");
TypeReference = Java.type("org.objectweb.asm.TypeReference");

Codex = {};
Codex.intConstInsn = function(integer) {
	switch (integer) {
	case 0: return new InsnNode(Opcodes.ICONST_0);
	case 1: return new InsnNode(Opcodes.ICONST_1);
	case 2: return new InsnNode(Opcodes.ICONST_2);
	case 3: return new InsnNode(Opcodes.ICONST_3);
	case 4: return new InsnNode(Opcodes.ICONST_4);
	case 5: return new InsnNode(Opcodes.ICONST_5);
	default: if (integer < 256) {
		return new IntInsnNode(Opcodes.BIPUSH, integer);
	} else {
		return new IntInsnNode(Opcodes.SIPUSH, integer);
	}}
};
Codex.ENUM_FIELD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL;
Codex.addEnumField = function(classNode, nameUpper, index) {
	classNode.fields.add(new FieldNode(Codex.ENUM_FIELD_ACCESS, nameUpper, "L"+classNode.name+";", null, null));
	var insns = new InsnList();
		insns.add(new InsnNode(Opcodes.DUP));
		insns.add(Codex.intConstInsn(index));
		insns.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, nameUpper, "L"+classNode.name+";"));
		insns.add(new InsnNode(Opcodes.AASTORE));
	return insns;
};

