package siege.common.siege.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import siege.common.siege.Siege;
import siege.common.siege.SiegeDatabase;

public class SiegeArgumentType implements ArgumentType<String> {

	public static final SimpleCommandExceptionType NO_SUCH_SIEGE = SiegeCommands.simple("No such siege");

	public static SiegeArgumentType siege() {
		return new SiegeArgumentType();
	}

	public static RequiredArgumentBuilder<CommandSource, String> siegeArgument() {
		return Commands.argument(SiegeCommands.SIEGE, siege());
	}

	public static <S> Siege getSiege(CommandContext<S> context) {
		return SiegeDatabase.getSiege(context.getArgument(SiegeCommands.SIEGE, String.class));
	}

	public static <S> Siege getSiegeOrNull(CommandContext<S> context) {
		try {
			return getSiege(context);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readUnquotedString();
	}

	@Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(SiegeDatabase.getAllSiegeNames().stream(), builder);
    }

	@Override
    public Collection<String> getExamples() {
        return SiegeDatabase.getAllSiegeNames();
    }

}
