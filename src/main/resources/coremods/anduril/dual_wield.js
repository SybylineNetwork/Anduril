/*\
| | This coremod allows players to attack entities with both hands.
| | You can do things with DualWield.of(LivingEntity). This method
| | lazily populates this field and is not thread-safe.
\*/

function initializeCoreMod() {
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	return {
		"LivingEntity.anduril_dual_wield": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.LivingEntity"
			},
			"transformer": function(clazz) {
				clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "anduril_dual_wield", "Lsybyline/anduril/coremods/DualWield;", null, null));
				return clazz;
			}
		}
	};
}
