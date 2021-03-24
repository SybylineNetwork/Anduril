package sybyline.anduril.extensions;

import java.util.function.BiFunction;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import sybyline.anduril.util.Util;

public class CustomRecipeSerializer<Inv extends IInventory, T extends CustomRecipe<Inv>> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

	public CustomRecipeSerializer(BiFunction<ResourceLocation, JsonObject, T> factory) {
		this.factory = factory;
	}

	private final BiFunction<ResourceLocation, JsonObject, T> factory;

	@Override
	public T read(ResourceLocation recipeId, JsonObject json) {
		return this.factory.apply(recipeId, json).setSerializer(this);
	}

	@Override
	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		JsonObject object = sandbox ? new JsonObject() : Util.IO.readJsonFromBuffer(buffer);
		return this.read(recipeId, object);
	}

	@Override
	public void write(PacketBuffer buffer, T recipe) {
		if (!sandbox) Util.IO.writeJsonToBuffer(recipe.getJson(), buffer);
	}

	public boolean sandbox = false;

}
