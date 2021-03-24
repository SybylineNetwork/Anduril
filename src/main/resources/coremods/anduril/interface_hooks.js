/*\
| | This coremod allows for more functionality in base classes.
\*/

function initializeCoreMod() {
	return {
		"Item=>IForgeItem": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.item.Item"
			},
			"transformer": function(clazz) {
				if (clazz.interfaces.remove("net/minecraftforge/common/extensions/IForgeItem")) {
					clazz.interfaces.add("sybyline/anduril/extensions/forge/IAndurilItem");
				}
				return clazz;
			}
		},
		"ItemStack=>IForgeItemStack": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.item.ItemStack"
			},
			"transformer": function(clazz) {
				if (clazz.interfaces.remove("net/minecraftforge/common/extensions/IForgeItemStack")) {
					clazz.interfaces.add("sybyline/anduril/extensions/forge/IAndurilItemStack");
				}
				return clazz;
			}
		}
	};
}
