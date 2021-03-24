
var TestItem = define(function(){
	this.extend(net.minecraft.item.Item);
	this.override("hasEffect", [Types.boolean, net.minecraft.item.ItemStack], function(stack){
		return true;
	});
});

function create() {
	var props = new net.minecraft.item.Item.Properties();
	return new TestItem(props);
};
