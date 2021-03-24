package siege.common.siege.command;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@Deprecated
public interface DefaultingCommand<T> extends Command<T> {

	public default <V> DefaultingCommand<T> withData(String name, Class<V> clazz, V object) {
		DefaultingCommand<T> parent = this;
		return (context, arguments) -> {
			arguments.putArgument(name, clazz, object);
			return parent.run(context, arguments);
		};
	}

	@Override
	public default int run(CommandContext<T> context) throws CommandSyntaxException {
		return this.run(context, new DefaultArgs<T>(context));
	}

	public abstract int run(CommandContext<T> context, DefaultArgs<T> arguments) throws CommandSyntaxException;

	public static final class DefaultArgs<T> {

		private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<Class<?>, Class<?>>();

	    static {
	        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
	        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
	        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
	        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
	        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
	        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
	        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
	        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
	    }
	  
		DefaultArgs(CommandContext<T> context) {
			this.context = context;
		}

		private final CommandContext<T> context;
		private final Map<String, Object> arguments = new HashMap<String, Object>();

		public <V> DefaultArgs<T> putArgument(String name, Class<V> clazz, V object) {
			arguments.put(name, object);
			return this;
		}

		@SuppressWarnings("unchecked")
		public <V> V getArgument(String name, Class<V> clazz) {
			Object object = arguments.get(name);
			if (object != null) {
				if (PRIMITIVE_TO_WRAPPER.getOrDefault(clazz, clazz).isAssignableFrom(object.getClass())) {
		            return (V) object;
		        } else {
		            throw new IllegalArgumentException("Argument '" + name + "' is defined as " + object.getClass().getSimpleName() + ", not " + clazz);
		        }
			}
			return context.getArgument(name, clazz);
		}
	}

}
