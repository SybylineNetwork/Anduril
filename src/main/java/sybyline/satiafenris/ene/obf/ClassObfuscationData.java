package sybyline.satiafenris.ene.obf;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sybyline.satiafenris.ene.obf.ScriptObfuscation.DefinitionsContext;

public class ClassObfuscationData {

	public final String className;
	public final String obfName;

	ClassObfuscationData(String className, String obfName) {
		this.className = className;
		this.obfName = obfName;
	}

	// deobfname, data
	private final Map<String, FieldObfuscationData> fields = Maps.newHashMap();
	// deobfname, data
	private final Map<String, List<MethodObfuscationData>> methods = Maps.newHashMap();

	public void putField(String deobfName, String srgName, String vanillaObfName) {
		FieldObfuscationData field = new FieldObfuscationData(deobfName, srgName, vanillaObfName);
		fields.put(field.deobfName, field);
	}

	public FieldObfuscationData getField(String name) {
		return fields.get(name);
	}

	public void putMethod(String deobfName, String srgName, String vanillaObfName, String returnType, String[] argumentTypes) {
		MethodObfuscationData method = new MethodObfuscationData(deobfName, srgName, vanillaObfName, returnType, argumentTypes);
		methods.computeIfAbsent(method.deobfName, name -> Lists.newArrayList()).add(method);
	}

	public List<MethodObfuscationData> getMethod(String name) {
		return methods.getOrDefault(name, Collections.emptyList());
	}

	public boolean isSameType(String string) {
		// Wow, such nice to be package/name/TypeName
		if (className.equals(string)) return true;
		// Maybe it's a package.name.Typename?
		if (className.equals(string = string.replace('.', '/'))) return true;
		// Oh, it could be a package.name.Type.SubType -> package/name/Type/SubType -> package/name/Type$SubType
		StringBuffer buffer = new StringBuffer(string);
		int lastSlash; while ((lastSlash = string.indexOf("/")) != -1) {
			buffer.setCharAt(lastSlash, '$');
			if (className.equals(buffer.toString())) return true;
		}
		return false;
	}

	public class FieldObfuscationData {

		public final String deobfName;
		public final String srgName;
		public final String vanillaObfName;

		FieldObfuscationData(String deobfName, String srgName, String vanillaObfName) {
			this.deobfName = deobfName != null ? deobfName : srgName;
			this.srgName = srgName;
			this.vanillaObfName = vanillaObfName;
		}

		public String getFieldNameFor(ObfuscationStatus status) {
			switch(status) {
			case DEOBF:
				return deobfName;
			case SRG:
				return srgName;
			default: throw new NullPointerException("status");
			}
		}

	}

	public class MethodObfuscationData {

		public final String deobfName;
		public final String srgName;
		public final String vanillaObfName;
		public final String returnType;
		public final String[] argumentTypes;
		public final int length;

		MethodObfuscationData(String deobfName, String srgName, String vanillaObfName, String returnType, String[] argumentTypes) {
			this.deobfName = deobfName != null ? deobfName : srgName;
			this.srgName = srgName;
			this.vanillaObfName = vanillaObfName;
			this.returnType = returnType;
			this.argumentTypes = argumentTypes;
			this.length = argumentTypes.length;
		}

		public String getMethodNameFor(ObfuscationStatus status) {
			switch(status) {
			case DEOBF:
				return deobfName;
			case SRG:
				return srgName;
			default: throw new NullPointerException("status");
			}
		}

		public boolean matchesArguments(String[] arguments, DefinitionsContext definitions) {
			if (argumentTypes.length != arguments.length) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				ClassObfuscationData classData = definitions.variables.get(arguments[i]);
				if (classData != null) {
					if (!classData.isSameType(argumentTypes[i])) {
						return false;
					}
				}
			}
			return true;
		}

	}

}
