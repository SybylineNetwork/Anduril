package siege.common.siege.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public interface ArgumentEnum<E extends Enum<E>&ArgumentEnum<E>> {

	public abstract <T extends ArgumentBuilder<CommandSource, T>, O extends Enum<O>> T addCommand(T in, O operation);

	public static <E extends Enum<E>&ArgumentEnum<E>, T extends ArgumentBuilder<CommandSource, T>, O extends Enum<O>> T iterate(Class<E> clazz, O operation, T argument) {
		for (E e : clazz.getEnumConstants()) {
			argument = e.addCommand(argument, operation);
		}
		return argument;
	}

	public static <E extends Enum<E>&ArgumentEnum<E>, O extends Enum<O>> LiteralArgumentBuilder<CommandSource> iterateOnLiteral(Class<E> clazz, O operation, String literal) {
		return iterate(clazz, operation, Commands.literal(literal));
	}

}
