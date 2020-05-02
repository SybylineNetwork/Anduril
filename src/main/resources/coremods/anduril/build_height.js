/*\
| | This coremod allows players to build higher than 256 blocks.
| | You can do things with BuildHeight.of(Chunk) or BuildHeight.of(ChunkPrimer)
| | 
\*/

function initializeCoreMod() {
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	return {
		"Chunk.anduril_build_height": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.chunk.Chunk"
			},
			"transformer": function(clazz) {
				clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "anduril_build_height", "Lsybyline/anduril/coremods/BuildHeight;", null, null));
				return clazz;
			}
		},
		"ChunkPrimer.anduril_build_height": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.chunk.ChunkPrimer"
			},
			"transformer": function(clazz) {
				clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "anduril_build_height", "Lsybyline/anduril/coremods/BuildHeight;", null, null));
				return clazz;
			}
		}
	};
}
