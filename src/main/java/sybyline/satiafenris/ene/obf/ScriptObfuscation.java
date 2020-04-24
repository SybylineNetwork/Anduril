package sybyline.satiafenris.ene.obf;

import java.util.*;
import java.util.function.*;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.*;

import sybyline.satiafenris.ene.obf.ClassObfuscationData.FieldObfuscationData;
import sybyline.satiafenris.ene.obf.ClassObfuscationData.MethodObfuscationData;

// Mappings for comments: 1.14.4_stable_58
public class ScriptObfuscation {

	public ScriptObfuscation(ObfuscationStatus status) {
		this.status = status;
	}

	public final ObfuscationStatus status;
	public final Consumer<String> err = System.err::println;

	// classname, data
	private final Map<String, ClassObfuscationData> classData = Maps.newHashMap();
	private final Map<String, DefinitionsContext> headers = Maps.newHashMap();
	private final Map<String, String> obfToDeobfClassNames = Maps.newHashMap();

	public void putClass(ClassObfuscationData clazz) {
		classData.put(clazz.className, clazz);
	}

	public ClassObfuscationData getClass(String name) {
		return classData.get(name);
	}

	public ClassObfuscationData getOrCreate(String name, String obfName) {
		return classData.computeIfAbsent(name, _name -> {
			obfToDeobfClassNames.put(obfName, _name);
			return new ClassObfuscationData(_name, obfName);
		});
	}

	public static final String ASSIGNMENT = "=";
	public static final String DECLARATION = "var";
	public static final Set<String> JAVA_PRIMITIVES = ImmutableSet.of("void", "boolean", "byte", "char", "short", "int", "long", "float", "double");

	public static final String DEF_PREFIX = "///";
	public static final int DEF_PREFIX_OFFSET = DEF_PREFIX.length();
	public static final String DEF_USETYPES = "include";
	public static final String DEF_USETYPES_AS = "as";
	public static final String DEF_MANUAL = DEF_PREFIX + "manual ";
	public static final int DEF_MANUAL_OFFSET = DEF_MANUAL.length();
	public static final String DEF_ENDSCOPE = DEF_PREFIX + "scope ";
	public static final int DEF_ENDSCOPE_OFFSET = DEF_ENDSCOPE.length();

	public static final String ERR_NO_SUCH_HEADER = "%s: No such header '%s'!";
	public static final String ERR_NO_SUCH_TYPE = "%s: No type '%s' found that can map to '%s'!";
	public static final String ERR_NO_SUCH_TYPEDEF = "%s: No type '%s' found for variable '%s'!";
	public static final String ERR_REMOVE_NODEF_VAR = "%s: Ended scope of nonexistant variable '%s'!";

	public void error(int line, String errorname, Object... args) {
		err.accept(String.format(errorname, line, args));
	}

	public String transformString(String script) {
		return StringUtils.join(transformList(Arrays.asList(script.split("\\R"))), '\n');
	}

	public List<String> transformList(List<String> script) {
		List<String> ret = Lists.newArrayList();
		DefinitionsContext definitions = new DefinitionsContext();
		String line; for (int index = 0; index < script.size(); index++) {
			line = script.get(index);
			if (line.isEmpty()) continue;
			line = line.trim();
			if (line.startsWith(DEF_PREFIX)) {
				String[] definition = line.substring(DEF_PREFIX_OFFSET).split(" ");
				if (definition.length < 2) {
					// ERROR: Bad definition in any case
					continue;
				}
				if (DEF_USETYPES.equals(definition[0])) {
					if (definition.length < 4) {
						String headerName = definition[1];
						DefinitionsContext header = headers.get(headerName);
						if (header != null) {
							definitions.types.putAll(header.types);
						} else {
							error(index, ERR_NO_SUCH_HEADER, headerName);
						}
					} else if (DEF_USETYPES_AS.equals(definition[2])) {
						String type = definition[1];
						String nickname = definition[3];
						if (classData.get(type) != null) {
							definitions.types.put(nickname, type);
						} else {
							error(index, ERR_NO_SUCH_TYPE, type, nickname);
						}
					}
				}
			} else {
				String[] array = line.split(" ", 4);
				// Declaration:
				//js:
				// var player = context.getPlayer();
				//custom:
				// PlayerEntity player = context.getPlayer();
				if (array.length == 4) {
					String type = array[0];
					String variable = array[1];
					String shouldBeEquals = array[2];
					// Check for assigning the variable
					if (ASSIGNMENT.equals(shouldBeEquals)) {
						String mapped = definitions.types.get(type);
						// If 'PlayerEntity' has been defined
						if (mapped != null) {
							ClassObfuscationData typeInformation = classData.get(mapped);
							if (typeInformation != null) {
								definitions.variables.put(variable, typeInformation);
							} else {
								error(index, ERR_NO_SUCH_TYPEDEF, type, variable);
							}
						} else {
							error(index, ERR_NO_SUCH_TYPEDEF, type, variable);
						}
					}
				}
				// Begin scope manually
				int manual = line.indexOf(DEF_MANUAL);
				if (manual != -1) {
					String[] variables = line.substring(manual + DEF_MANUAL_OFFSET).trim().split(" ");
					for (String declarationunified : variables) {
						String[] declaration = declarationunified.split(":", 2);
						if (declaration.length != 2) {
							// ERROR: wut
							continue;
						}
						String variable = declaration[0];
						String type = declaration[1];
						String mapped = definitions.types.get(type);
						if (mapped != null) {
							ClassObfuscationData typeInformation = classData.get(mapped);
							if (typeInformation != null) {
								definitions.variables.put(variable, typeInformation);
							} else {
								error(index, ERR_NO_SUCH_TYPEDEF, type, variable);
							}
						} else {
							error(index, ERR_NO_SUCH_TYPEDEF, type, variable);
						}
					}
				}
				// End scope manually
				int endscope = line.indexOf(DEF_ENDSCOPE);
				if (endscope != -1) {
					String[] variables = line.substring(endscope + DEF_ENDSCOPE_OFFSET).trim().split(" ");
					for (String variable : variables) {
						if (definitions.variables.remove(variable) == null) {
							error(index, ERR_REMOVE_NODEF_VAR, variable);
						}
					}
				}
				// replace fields and methods
				line = replaceTokens(index, line, definitions);
				ret.add(line);
			}
		}
		return ret;
	}

	public static final String TERMINATION = ";";
	public static final String PROPERTYGET = ".";
	public static final String PROPERTYGET_SPLIT = "\\.";
	public static final String ARGUMENT_SPLIT = "\\,";
	public static final String METHODCALL_START = "(";
	public static final String METHODCALL_END = ")";

	//  Ex:
	//    PlayerEntity player = context.getPlayer();
	//    LivingEntity living = context.getTarget();
	//    PlayerAbilities abilities = player.abilities;
	//    boolean isCreative = abilities.isCreativeMode;
	////When multiple arguments, only use variables and no whitespace: canAttack(living,entitypredicate);
	//    boolean canAttack = player.canAttack(living);
	////GameProfile is Mojang, not Minecraft
	//    var profile = player.getGameProfile();
	//    \/
	//    var player = context.getPlayer();
	//    var abilities = player.field_71075_bZ;
	//    var isCreative = abilities.field_75098_d;
	//    var canAttack = player.func_213336_c(living);
	//    var profile = player.field_146106_i();
	public String replaceTokens(int index, String line, DefinitionsContext definitions) {
		String[] array = line.split(" ");
		// Targets: Type decl = variable.field;
		if (array.length == 4) {
			String type = array[0];
			String infoType = definitions.getType(type);
			// Only replace if actually obf
			if (status.isObf()) {
				String expression = array[3];
				// Delete the trailing semicolons
				while (expression.endsWith(TERMINATION)) {
					expression = expression.substring(0, expression.length() - 1);
				}
				// Check if method
				if (expression.endsWith(METHODCALL_END)) {
					expression = doMethod(expression, definitions);
				}
				// Otherwise, assume field
				else {
					expression = doField(expression, definitions);
				}
				expression = expression + TERMINATION;
				array[3] = expression;
			}
			// If the type was found, make sure JS knows it's a declaration
			if (infoType != null) {
				array[0] = DECLARATION;
			}
		}
		// Targets: variable.method(args...);
		else if (array.length == 1) {
			// Only replace if actually obf
			if (status.isObf()) {
				array[0] = doMethod(array[0], definitions);
			}
		}
		line = StringUtils.join(array, ' ');
		return line;
	}

	public String doMethod(String expression, DefinitionsContext definitions) {
		// Delete the trailing semicolons
		while (expression.endsWith(TERMINATION)) {
			expression = expression.substring(0, expression.length() - 1);
		}
		// methodcall, else assume field
		if (expression.endsWith(METHODCALL_END)) {
			String[] parts = expression.split(PROPERTYGET_SPLIT, 2);
			if (parts.length != 2) {
				// wut
				return expression;
			}
			String variable = parts[0];
			String method = parts[1];
			ClassObfuscationData data = definitions.variables.get(variable);
			// If the variable in "Type decl = variable.field;" has type info, look it up
			if (data != null) {
				int methodCallIndex = method.indexOf(METHODCALL_START);
				String name = method.substring(0, methodCallIndex);
				String descriptor = method.substring(methodCallIndex + 1, method.length() - 1);
				String[] arguments = descriptor.split(ARGUMENT_SPLIT);
				List<MethodObfuscationData> methodDatas = data.getMethod(name);
				if (!methodDatas.isEmpty()) {
					for (MethodObfuscationData methodData : methodDatas) {
						if (methodData.matchesArguments(arguments, definitions)) {
							// Replace the name
							parts[1] = methodData.getMethodNameFor(status) + METHODCALL_START + descriptor + METHODCALL_END;
							break;
						}
					}
				}
			}
			expression = StringUtils.join(parts, PROPERTYGET);
		}
		return expression;
	}

	public String doField(String expression, DefinitionsContext definitions) {
		String[] parts = expression.split(PROPERTYGET_SPLIT, 2);
		if (parts.length != 2) {
			// wut
			return expression;
		}
		String variable = parts[0];
		String field = parts[1];
		ClassObfuscationData data = definitions.variables.get(variable);
		// If the [variable] in "Type decl = [variable].[field];" has type info, look it up
		if (data != null) {
			FieldObfuscationData fieldData = data.getField(field);
			if (fieldData != null) {
				// Replace the name
				parts[1] = fieldData.getFieldNameFor(status);
			}
		}
		expression = StringUtils.join(parts, PROPERTYGET);
		return expression;
	}

	public class DefinitionsContext {

		// TypeName -> package.name.TypeName
		public final Map<String, String> types = Maps.newHashMap();
		public final Map<String, ClassObfuscationData> variables = Maps.newHashMap();

		public String getType(String type) {
			if (JAVA_PRIMITIVES.contains(type)) {
				return null;
			} else if (DECLARATION.equals(type)) {
				return null;
			} else {
				return types.get(type);
			}
		}

	}

	public String mapType2mcpOrFallthrough(String string) {
		return null;
	}

}
