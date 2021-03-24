/*\
| | This coremod allows for more functionality in block and item model loading.
\*/

function initializeCoreMod() {
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");
	return {
		"ModelLoader.loadModel": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraftforge.client.model.ModelLoader"
			},
			"transformer": function(clazz) {
				// Yes, this is duplicated. I don't care.
				var loadModel = new MethodNode(Opcodes.ACC_PROTECTED, "loadModel", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/model/BlockModel;", null, null);
				var insns = loadModel.instructions = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/client/ItemDisplay", "loadModel", "(Lnet/minecraft/client/renderer/model/ModelBakery;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/model/BlockModel;", false));
					insns.add(new InsnNode(Opcodes.ARETURN));
				clazz.methods.add(loadModel);
				var loadModel2 = new MethodNode(Opcodes.ACC_PROTECTED, "func_177594_c", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/model/BlockModel;", null, null);
				var insns2 = loadModel2.instructions = new InsnList();
					insns2.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns2.add(new VarInsnNode(Opcodes.ALOAD, 1));
					insns2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "sybyline/anduril/client/ItemDisplay", "loadModel", "(Lnet/minecraft/client/renderer/model/ModelBakery;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/model/BlockModel;", false));
					insns2.add(new InsnNode(Opcodes.ARETURN));
				clazz.methods.add(loadModel2);
				return clazz;
			}
		}
	};
}
