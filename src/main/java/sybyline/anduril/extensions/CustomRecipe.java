package sybyline.anduril.extensions;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public abstract class CustomRecipe<Inv extends IInventory> implements IRecipe<Inv> {

	public static final int MAX_HEIGHT = 3;
	public static final int MAX_WIDTH = 3;

	JsonObject json;
	IRecipeSerializer<?> serializer;
	private ResourceLocation location;
	private IRecipeType<?> type;

	public CustomRecipe(ResourceLocation location, JsonObject json) {
		this.location = location;
		this.json = json;
		String type = JSONUtils.getString(json, "type_recipe", JSONUtils.getString(json, "type"));
		this.type = CustomRecipes.getOrRegister(type);
		json.addProperty("NET_ID", location.toString());
	}

	@SuppressWarnings("unchecked")
	<T extends CustomRecipe<Inv>> T setSerializer(IRecipeSerializer<T> serializer) {
		this.serializer = serializer;
		return (T)this;
	}

	public JsonObject getJson() {
		return this.json;
	}

	@Override
	public ResourceLocation getId() {
		return this.location;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return this.serializer;
	}

	@Override
	public IRecipeType<?> getType() {
		return this.type;
	}

	protected static Map<String, Ingredient> deserializeKey(JsonObject json) {
		Map<String, Ingredient> map = Maps.newHashMap();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			}
			if (" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}
			map.put(entry.getKey(), Ingredient.deserialize(entry.getValue()));
		}
		map.put(" ", Ingredient.EMPTY);
		return map;
	}

	protected static String[] shrink(String... toShrink) {
		int i = Integer.MAX_VALUE;
		int j = 0;
		int k = 0;
		int l = 0;
		for (int i1 = 0; i1 < toShrink.length; ++i1) {
			String s = toShrink[i1];
			i = Math.min(i, firstNonSpace(s));
			int j1 = lastNonSpace(s);
			j = Math.max(j, j1);
			if (j1 < 0) {
				if (k == i1) {
					++k;
				}
				++l;
			} else {
				l = 0;
			}
		}
		if (toShrink.length == l) {
			return new String[0];
		} else {
			String[] astring = new String[toShrink.length - l - k];
			for (int k1 = 0; k1 < astring.length; ++k1) {
				astring[k1] = toShrink[k1 + k].substring(i, j + 1);
			}
			return astring;
		}
	}

	protected static int firstNonSpace(String str) {
		int i;
		for (i = 0; i < str.length() && str.charAt(i) == ' '; ++i);
		return i;
	}

	protected static int lastNonSpace(String str) {
		int i;
		for (i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; --i);
		return i;
	}

	protected static String[] patternFromJson(JsonArray jsonArr) {
		String[] astring = new String[jsonArr.size()];
		if (astring.length > MAX_HEIGHT) {
			throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
		} else if (astring.length == 0) {
			throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
		} else {
			for (int i = 0; i < astring.length; ++i) {
				String s = JSONUtils.getString(jsonArr.get(i), "pattern[" + i + "]");
				if (s.length() > MAX_WIDTH) {
					throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");
				}
				if (i > 0 && astring[0].length() != s.length()) {
					throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
				}
				astring[i] = s;
			}
			return astring;
		}
	}

	protected static NonNullList<Ingredient> deserializeIngredients(String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight) {
		NonNullList<Ingredient> nonnulllist = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
		Set<String> set = Sets.newHashSet(keys.keySet());
		set.remove(" ");
		for (int i = 0; i < pattern.length; ++i) {
			for (int j = 0; j < pattern[i].length(); ++j) {
				String s = pattern[i].substring(j, j + 1);
				Ingredient ingredient = keys.get(s);
				if (ingredient == null) {
					throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
				}
				set.remove(s);
				nonnulllist.set(j + patternWidth * i, ingredient);
			}
		}
		if (!set.isEmpty()) {
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
		} else {
			return nonnulllist;
		}
	}

}
