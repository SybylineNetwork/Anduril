package sybyline.anduril.extensions;

import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

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

}
