package jsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import sybyline.json.ParseError;
import sybyline.json.ext.jsm.BytecodeUtil;

public final class Return {

	public static Return of(Object... objects) {
		return new Return(objects);
	}

	private Return(Object[] array) {
		this.array = array;
	}

	private int index = 0;
	private final Object[] array;

	public final Object getObject() {
		try {
			return array[index];
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Return index out of range: " + index);
		} finally {
			index++;
		}
	}

	public final boolean getBoolean() {
		return ((Boolean)getObject()).booleanValue();
	}

	public final char getChar() {
		return ((Character)getObject()).charValue();
	}

	public final byte getByte() {
		return ((Number)getObject()).byteValue();
	}

	public final short getShort() {
		return ((Number)getObject()).shortValue();
	}

	public final int getInt() {
		return ((Number)getObject()).intValue();
	}

	public final long getLong() {
		return ((Number)getObject()).longValue();
	}

	public final float getFloat() {
		return ((Number)getObject()).floatValue();
	}

	public final double getDouble() {
		return ((Number)getObject()).doubleValue();
	}

	public static final class ReturnBuilder implements Returnable {
		public static ReturnBuilder create() {
			return new ReturnBuilder();
		}
		private ReturnBuilder() {}
		private List<Object> array = new ArrayList<>();
		public static final ReturnBuilder add(Object obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addBoolean(boolean obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addChar(char obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addByte(byte obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addShort(short obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addInt(int obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addLong(long obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addFloat(float obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		public static final ReturnBuilder addDouble(double obj, ReturnBuilder builder) {
			builder.array.add(obj);
			return builder;
		}
		@Override
		public Return getAsReturn() {
			try {
				Collections.reverse(array);
				return new Return(array.toArray());
			} finally {
				array = null;
			}
		}
	}

	// Utility

	public static final Type TUPLE_TYPE = Type.getType(Return.class);
    public static void tupleUnboxer(InsnList insns, Type[] types) throws ParseError {
    	// tuple
    	if (types.length == 0) {
    		
    	} else {
	    	for (int i = 0; i < types.length; i++) {
	        	insns.add(new InsnNode(Opcodes.DUP));
	        	// things, tuple, tuple
	    		Type type = types[i];
	    		switch(type.getSort()) {
	    		case Type.BOOLEAN:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getBoolean", "()Z", false));
	    			break;
	    		case Type.BYTE:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getByte", "()B", false));
	    			break;
	    		case Type.CHAR:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getChar", "()C", false));
	    			break;
	    		case Type.SHORT:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getShort", "()S", false));
	    			break;
	    		case Type.INT:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getInt", "()I", false));
	    			break;
	    		case Type.LONG:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getLong", "()J", false));
	    			break;
	    		case Type.FLOAT:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getFloat", "()F", false));
	    			break;
	    		case Type.DOUBLE:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getDouble", "()D", false));
	    			break;
	    		case Type.OBJECT:
	    		case Type.ARRAY:
	    			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jsm/Return", "getObject", "()Ljava/lang/Object;", false));
	    			insns.add(new TypeInsnNode(Opcodes.CHECKCAST, type.getInternalName()));
	    			break;
	    		case Type.VOID:
	    		case Type.METHOD:
	    		default:
	    			throw new ParseError("Can't unbox return with void or method types!");
	    		}
	    		// things, tuple, thing
	    		BytecodeUtil.swap(insns, type, TUPLE_TYPE);
	    		// things, thing, tuple
    		}
    	}
    	// things, tuple
		insns.add(new InsnNode(Opcodes.POP));
    	// things
    }
    public static void tupleBoxer(InsnList insns, Type[] types) throws ParseError {
    	// things, thing
    	insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "create", "()Ljsm/Return$ReturnBuilder;", false));
    	// things, thing, builder
    	for (int i = types.length - 1; i >= 0; i--) {
    		Type type = types[i];
    		switch(type.getSort()) {
    		case Type.BOOLEAN:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addBoolean", "(ZLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.BYTE:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addByte", "(BLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.CHAR:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addChar", "(CLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.SHORT:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addShort", "(SLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.INT:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addInt", "(ILjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.LONG:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addLong", "(JLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.FLOAT:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addFloat", "(FLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.DOUBLE:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addDouble", "(DLjsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			break;
    		case Type.OBJECT:
    		case Type.ARRAY:
    			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jsm/Return$ReturnBuilder", "addObject", "(Ljava/lang/Object;Ljsm/Return$ReturnBuilder;)Ljsm/Return$ReturnBuilder;", false));
    			insns.add(new TypeInsnNode(Opcodes.CHECKCAST, type.getInternalName()));
    			break;
    		case Type.VOID:
    		case Type.METHOD:
    		default:
    			throw new ParseError("Can't box v Returnith void or method types!");
    		}
    		// things, builder
    	}
    	// builder
    	insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "jsm/Returnable", "getAsReturn", "()Ljsm/Return;", true));
    	// tuple
    }

}
