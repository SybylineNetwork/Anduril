/*\
|*| Happy little trees. Yes. Keep thinking of the happy trees, nothing to see here.
|*| We don't want to talk about this ever again, ok? This coremod never existed...
\*/

function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	return {
		"Item.fuckery": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.item.Item"
			},
			"transformer": function(clazz) {
				var FMLEnvironment = Java.type("net.minecraftforge.fml.loading.FMLEnvironment");
				var Dist = Java.type("net.minecraftforge.api.distmarker.Dist");
				var server = FMLEnvironment.dist.equals(Dist.DEDICATED_SERVER);
				
				if (server) {
					clazz.fields.forEach(function(field){
						if (field.name.equals("ister")) {
							field.signature = null;
							ASMAPI.log("ERROR", "Removed ister fref");
						}
					});
					var thing = null;
					var thing2 = null;
					clazz.methods.forEach(function(method){
						if (method.name.equals("getItemStackTileEntityRenderer")) {
							thing = method;
						}
						if (method.name.equals("lambda$new$5")) {
							thing2 = method;
						}
						if (method.name.equals("<init>")) {
							var map_insn = ASMAPI.findFirstMethodCall(method, ASMAPI.MethodType.INTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V");
							while (!map_insn.getNext()) {
								method.instructions.remove(map_insn.getNext());
							}
							method.instructions.insert(map_insn, new InsnNode(Opcodes.RETURN));
						}
					});
					if (thing) {
						clazz.methods.remove(thing);
						ASMAPI.log("ERROR", "Removed ister mref");
					}
					if (thing2) {
						clazz.methods.remove(thing2);
						ASMAPI.log("ERROR", "Removed ister mref FROM A FUCKING LAMBDA");
					}
				}
				return clazz;
			}
		}
	};
}
