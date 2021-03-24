package siege.common.siege.command;

import java.util.Collection;
import java.util.Collections;
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
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class SiegeKitArgumentType implements ArgumentType<String> {

	public static final SimpleCommandExceptionType NO_SUCH_KIT = SiegeCommands.simple("No such kit");

	public static SiegeKitArgumentType siegeKit() {
		return new SiegeKitArgumentType();
	}

	public static <S> Kit getSiegeKit(CommandContext<S> context) {
		return KitDatabase.getKit(getSiegeKitName(context));
	}

	public static <S> String getSiegeKitName(CommandContext<S> context) {
		return context.getArgument(SiegeCommands.SIEGE_KIT, String.class);
	}

	public static <S> Kit getSiegeTeamOrNull(CommandContext<S> context) {
		try {
			return getSiegeKit(context);
		} catch(Exception e) {}
		return null;
	}

	public static RequiredArgumentBuilder<CommandSource, String> siegeKitArgument() {
		return Commands.argument(SiegeCommands.SIEGE_KIT, siegeKit());
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readUnquotedString();
	}

	@Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return ISuggestionProvider.suggest(KitDatabase.getAllKitNames().stream(), builder);
    }

	@Override
    public Collection<String> getExamples() {
        return Collections.emptyList();
    }

}
