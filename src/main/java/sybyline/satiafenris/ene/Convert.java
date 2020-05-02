package sybyline.satiafenris.ene;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.google.common.collect.*;
import com.google.gson.*;
import jdk.nashorn.api.scripting.*;
import net.minecraft.nbt.*;

public final class Convert {

	private Convert() {}

	@Nullable
	public static String js_string_of(Object object) {
		return js_string_of(object, true);
	}

	@Nullable
	public static String js_string_of(Object object, boolean quote_strings) {
		// Null
		if (object == null)
			return "null";
		INBT nbt = nbt_of(object);
		StringBuilder builder = new StringBuilder();
		_string_append(builder, nbt, quote_strings);
		return builder.toString();
	}

	private static void _string_append(StringBuilder string, INBT nbt, boolean quote_strings) {
		if (nbt instanceof NumberNBT) {
			string.append(((NumberNBT)nbt).getAsNumber());
		} else if (nbt instanceof StringNBT) {
			if (quote_strings) string.append('\"');
			String str = ((StringNBT)nbt).getString();
			for (int i = 0; i < str.length(); i++) {
				char chr = str.charAt(i);
				if (chr == '\"') string.append('\\');
				string.append(chr);
			}
			if (quote_strings) string.append('\"');
		} else if (nbt instanceof CollectionNBT) {
			CollectionNBT<?> collection = (CollectionNBT<?>)nbt;
			string.append('[');
			for (int i = 0; i < collection.size(); i++) {
				_string_append(string, collection.get(i), true);
				string.append(',');
			}
			string.setCharAt(string.length() - 1, ']');
		} else if (nbt instanceof CompoundNBT) {
			CompoundNBT compound = (CompoundNBT)nbt;
			string.append('{');
			boolean back = false;
			for (String key : compound.keySet()) {
				string.append('\"').append(key).append('\"').append(':');
				_string_append(string, compound.get(key), true);
				string.append(',');
				back = true;
			}
			if (back) {
				string.setCharAt(string.length() - 1, '}');
			} else {
				string.append('}');
			}
		}
	}

	@Nullable
	public static CompoundNBT nbt_of_expect_compound(Object object) {
		return (CompoundNBT)nbt_of(object);
	}

	@Nullable
	public static INBT nbt_of(Object object) {
		// Null
		if (object == null)
			return null;
		// Dynamic
		if (object instanceof ScriptExtension)
			return ((ScriptExtension<?>)object).getTypifier().convertDefault(object);
		// Self
		if (object instanceof INBT)
			return (INBT)object;
		// JS
		if (object instanceof JSObject)
			return _nbt_from_js((JSObject)object);
		// Java
		if (object instanceof Boolean)
			return ByteNBT.valueOf((Boolean)object);
		if (object instanceof Number)
			return _nbt_from_number((Number)object);
		if (object instanceof String)
			return StringNBT.valueOf((String)object);
		if (object instanceof boolean[])
			return _nbt_from_array(object);
		if (object instanceof byte[])
			return _nbt_from_array(object);
		if (object instanceof char[])
			return _nbt_from_array(object);
		if (object instanceof short[])
			return _nbt_from_array(object);
		if (object instanceof int[])
			return _nbt_from_array(object);
		if (object instanceof long[])
			return _nbt_from_array(object);
		if (object instanceof float[])
			return _nbt_from_array(object);
		if (object instanceof double[])
			return _nbt_from_array(object);
		if (object instanceof Object[])
			return _nbt_from_array(object);
		if (object instanceof List)
			return _nbt_from_list((List<?>)object);
		if (object instanceof Map)
			return _nbt_from_map((Map<?, ?>)object);
		return null;
	}

	@Nullable
	public static Object java_of(Object object) {
		// Null
		if (object == null)
			return null;
		// Dynamic
		if (object instanceof ScriptBridge)
			return object;
		// NBT
		if (object instanceof INBT) {
			ScriptExtension<?> dynamic = ScriptExtensions.fromNBT((INBT)object);
			if (dynamic != null) 
				return dynamic;
			if (object instanceof NumberNBT)
				return ((NumberNBT)object).getAsNumber();
			if (object instanceof StringNBT)
				return ((StringNBT)object).getString();
			if (object instanceof CollectionNBT) {
				if (object instanceof ByteArrayNBT)
					return ((ByteArrayNBT)object).getByteArray();
				if (object instanceof IntArrayNBT)
					return ((IntArrayNBT)object).getIntArray();
				if (object instanceof LongArrayNBT)
					return ((LongArrayNBT)object).getAsLongArray();
				if (object instanceof ListNBT)
					return ((ListNBT)object).toArray(new INBT[0]);
			}
			if (object instanceof CompoundNBT)
				return _java_from_compound((CompoundNBT)object);
		}
		// JS
		if (object instanceof JSObject)
			return _java_from_js((JSObject)object);
		// Self
		return object;
	}

	@Nullable
	public static Object js_of(Object object) {
		return js_of(object, Script::graalOrNashorn);
	}

	@Nullable
	public static Object js_of(Object object, Script script) {
		return js_of(object, () -> script);
	}

	@Nullable
	public static Object js_of(Object object, Supplier<Script> script) {
		// Null
		if (object == null)
			return null;
		// Self
		if (object instanceof JSObject)
			return object;
		// Does this want to stay itself?
		if (object instanceof ScriptBridge)
			return object;
		if (object instanceof INBT) {
		// Dynamic
			ScriptExtension<?> dynamic = ScriptExtensions.fromNBT((INBT)object);
			if (dynamic != null) 
				return dynamic;
		// NBT
			return _js_from_nbt((INBT)object, script.get());
		}
		// Java
		if (object instanceof boolean[])
			return _js_from_array(object, script.get());
		if (object instanceof byte[])
			return _js_from_array(object, script.get());
		if (object instanceof char[])
			return _js_from_array(object, script.get());
		if (object instanceof short[])
			return _js_from_array(object, script.get());
		if (object instanceof int[])
			return _js_from_array(object, script.get());
		if (object instanceof long[])
			return _js_from_array(object, script.get());
		if (object instanceof float[])
			return _js_from_array(object, script.get());
		if (object instanceof double[])
			return _js_from_array(object, script.get());
		if (object instanceof Object[])
			return _js_from_array(object, script.get());
		if (object instanceof List)
			return _js_from_list((List<?>)object, script.get());
		if (object instanceof Map)
			return _js_from_map((Map<?, ?>)object, script.get());
		return null;
	}

	@Nullable
	public static JsonElement json_of(Object object) {
		// Null
		if (object == null)
			return JsonNull.INSTANCE;
		throw new UnsupportedOperationException("TODO : implement");
	}

	// Internal

	// NBT -> JS

	private static Object _js_from_nbt(INBT nbt, Script script) {
		if (nbt instanceof CollectionNBT)
			return _js_from_nbtlist((CollectionNBT<?>)nbt, script);
		if (nbt instanceof NumberNBT)
			return ((NumberNBT)nbt).getAsNumber();
		if (nbt instanceof StringNBT)
			return ((StringNBT)nbt).getString();
		CompoundNBT compound = (CompoundNBT)nbt;
		JSObject js = script.newObjectType();
		for (String key : compound.keySet()) {
			INBT value = compound.get(key);
			js.setMember(key, _js_from_nbt(value, script));
		}
		return js;
	}

	private static Object _js_from_nbtlist(CollectionNBT<?> nbt, Script script) {
		JSObject js = script.newObjectType();
		for (int i = 0; i < nbt.size(); i++) {
			INBT elem = nbt.get(i);
			js.setSlot(i, _js_from_nbt(elem, script));
		}
		return js;
	}

	// JS -> NBT

	private static INBT _nbt_from_js(JSObject js) {
		if (js.isFunction()) return null;
		if (js.isArray()) return _nbt_from_jsarray(js);
		CompoundNBT ret = new CompoundNBT();
		for (String key : js.keySet()) {
			Object value = js.getMember(key);
			ret.put(key, nbt_of(value));
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private static <NBT extends INBT> INBT _nbt_from_jsarray(JSObject js) {
		CollectionNBT<NBT> list = null;
		for (int i = 0; js.hasSlot(i); i++) {
			Object slot = js.getSlot(i);
			if (list == null) {
				list = _nbt_new_collection(slot);
			}
			list.add(i, (NBT)nbt_of(slot));
		}
		if (list == null) {
			list = (CollectionNBT<NBT>) new ListNBT();
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static <NBT extends INBT> CollectionNBT<NBT> _nbt_new_collection(Object obj) {
		if (obj instanceof Byte)
			return (CollectionNBT<NBT>) new ByteArrayNBT(new byte[0]);
		if (obj instanceof Integer)
			return (CollectionNBT<NBT>) new IntArrayNBT(new int[0]);
		if (obj instanceof Long)
			return (CollectionNBT<NBT>) new LongArrayNBT(new long[0]);
		return (CollectionNBT<NBT>) new ListNBT();
	}

	// Java -> NBT

	private static INBT _nbt_from_number(Number number) {
		if (number instanceof Byte)
			return ByteNBT.valueOf(number.byteValue());
		if (number instanceof Short)
			return ShortNBT.valueOf(number.shortValue());
		if (number instanceof Integer)
			return IntNBT.valueOf(number.intValue());
		if (number instanceof Long)
			return LongNBT.valueOf(number.longValue());
		if (number instanceof Float)
			return FloatNBT.valueOf(number.floatValue());
		return DoubleNBT.valueOf(number.doubleValue());
	}

	@SuppressWarnings("unchecked")
	private static <NBT extends INBT> INBT _nbt_from_array(Object array) {
		CollectionNBT<NBT> list = null;
		for (int i = 0; i < Array.getLength(array); i++) {
			Object slot = Array.get(array, i);
			if (list == null) {
				list = _nbt_new_collection(slot);
			}
			list.add(i, (NBT)nbt_of(slot));
		}
		if (list == null) {
			list = (CollectionNBT<NBT>) new ListNBT();
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static <NBT extends INBT> INBT _nbt_from_list(List<?> java) {
		CollectionNBT<NBT> list = null;
		for (int i = 0; i < java.size(); i++) {
			Object slot = java.get(i);
			if (list == null) {
				list = _nbt_new_collection(slot);
			}
			list.add(i, (NBT)nbt_of(slot));
		}
		if (list == null) {
			list = (CollectionNBT<NBT>) new ListNBT();
		}
		return list;
	}

	private static INBT _nbt_from_map(Map<?, ?> map) {
		CompoundNBT ret = new CompoundNBT();
		for (Entry<?, ?> entry : map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			INBT value = nbt_of(entry.getValue());
			ret.put(key, value);
		}
		return ret;
	}

	// NBT -> Java

	private static Object _java_from_compound(CompoundNBT nbt) {
		Map<String, Object> ret = Maps.newHashMap();
		for (String key : nbt.keySet())
			ret.put(key, java_of(nbt.get(key)));
		return ret;
	}

	// Java -> JS

	private static JSObject _js_from_array(Object array, Script script) {
		JSObject js = script.newArrayType();
		for (int i = 0; i < Array.getLength(array); i++)
			js.setSlot(i, Array.get(array, i));
		return js;
	}

	private static JSObject _js_from_list(List<?> list, Script script) {
		JSObject js = script.newArrayType();
		for (int i = 0; i < list.size(); i++)
			js.setSlot(i, list.get(i));
		return js;
	}

	private static JSObject _js_from_map(Map<?, ?> map, Script script) {
		JSObject js = script.newArrayType();
		for (Entry<?, ?> entry : map.entrySet())
			js.setMember(String.valueOf(entry.getKey()), js_of(entry.getValue(), script));
		return js;
	}

	// JS -> Java

	private static Object _java_from_js(JSObject js) {
		if (js.isFunction()) return null;
		if (js.isArray()) return _java_from_jsarray(js);
		Map<String, Object> ret = Maps.newHashMap();
		for (String key : js.keySet()) {
			Object value = js.getMember(key);
			ret.put(key, java_of(value));
		}
		return ret;
	}

	private static Object _java_from_jsarray(JSObject js) {
		List<Object> ret = Lists.newArrayList();
		for (int i = 0; js.hasSlot(i); i++) {
			Object slot = js.getSlot(i);
			ret.add(java_of(slot));
		}
		return ret;
	}

}
