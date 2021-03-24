package sybyline.anduril.extensions;

import java.util.List;
import com.google.common.collect.Lists;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RepairItemRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import sybyline.anduril.extensions.forge.IAndurilItem;
import sybyline.anduril.util.rtc.RuntimeTricks;
import sybyline.anduril.util.rtc.RuntimeTypeChanger;

public class CustomRecipes {

	public static IRecipeType<?> getOrRegister(String name) {
		return getOrRegister(new ResourceLocation(name));
	}

	public static IRecipeType<?> getOrRegister(String domain, String name) {
		return getOrRegister(new ResourceLocation(domain, name));
	}

	public static IRecipeType<?> getOrRegister(ResourceLocation location) {
		return Registry.RECIPE_TYPE.getValue(location).orElseGet(() -> register(location));
	}

	public static <T extends IRecipe<?>> IRecipeType<T> register(String name) {
		return register(new ResourceLocation(name));
	}

	public static <T extends IRecipe<?>> IRecipeType<T> register(String domain, String name) {
		return register(new ResourceLocation(domain, name));
	}

	public static <T extends IRecipe<?>> IRecipeType<T> register(ResourceLocation id) {
		return Registry.register(Registry.RECIPE_TYPE, id, new IRecipeType<T>() {
			public String toString() {
				return id.toString();
			}
		});
	}

	@Deprecated
	public static void hookRepairItemRecipeConstructor(RepairItemRecipe recipe) {
		if (recipe.getClass() == RepairItemRecipe.class)
			RepairItemRecipeExtra.rtc.changeType(recipe);
	}

}

class RepairItemRecipeExtra extends RepairItemRecipe {

	public static RuntimeTypeChanger<RepairItemRecipeExtra> rtc = RuntimeTricks.getTypeChanger(RepairItemRecipeExtra.class);

	public RepairItemRecipeExtra(ResourceLocation idIn) {
		super(idIn);
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		List<ItemStack> list = Lists.newArrayList();
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			list.add(stack);
			if (list.size() <= 1)
				continue;
			ItemStack first = list.get(0);
			if (stack.getItem() != first.getItem() || first.getCount() != 1 || stack.getCount() != 1 || !first.isRepairable())
				return ItemStack.EMPTY;
			if (!IAndurilItem.isSame(first, stack))
				return ItemStack.EMPTY;
		}
		if (list.size() == 2) {
			ItemStack first = list.get(0);
			ItemStack second = list.get(1);
			if (first.getItem() == second.getItem() && first.getCount() == 1 && second.getCount() == 1 && first.isRepairable()) {
				int ddf = first.getMaxDamage() - first.getDamage();
				int dds = first.getMaxDamage() - second.getDamage();
				int l = ddf + dds + first.getMaxDamage() * 5 / 100;
				int dmg = first.getMaxDamage() - l;
				if (dmg < 0)
					dmg = 0;
				ItemStack out = new ItemStack(first.getItem());
				out.setDamage(dmg);
				IAndurilItem extra = IAndurilItem.of(first.getItem());
				extra.copyDynamicData(first, out, null);
				return out;
			}
		}
		return ItemStack.EMPTY;
	}
	
	
}