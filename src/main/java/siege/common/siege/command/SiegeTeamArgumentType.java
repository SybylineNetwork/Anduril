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
import siege.common.siege.Siege;
import siege.common.siege.SiegeDatabase;
import siege.common.siege.SiegeTeam;

public class SiegeTeamArgumentType implements ArgumentType<String> {

	public static final SimpleCommandExceptionType NO_SUCH_SIEGE = SiegeCommands.simple("No such siege");

	public static SiegeTeamArgumentType siegeTeam() {
		return new SiegeTeamArgumentType();
	}

	public static <S> SiegeTeam getSiegeTeam(CommandContext<S> context) {
		Siege siege = SiegeArgumentType.getSiege(context);
		if (siege != null) {
			String siegeTeamName = context.getArgument(SiegeCommands.SIEGE_TEAM, String.class);
			return siege.getTeam(siegeTeamName);
		}
		return null;
	}

	public static <S> SiegeTeam getSiegeTeamOrNull(CommandContext<S> context) {
		try {
			return getSiegeTeam(context);
		} catch(Exception e) {}
		return null;
	}

	public static RequiredArgumentBuilder<CommandSource, String> siegeTeamArgument() {
		return Commands.argument(SiegeCommands.SIEGE_TEAM, siegeTeam());
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readUnquotedString();
	}

	@Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		Siege siege = SiegeArgumentType.getSiegeOrNull(context);
		if (siege == null) {
			try {
				siege = SiegeDatabase.getActiveSiegeForPlayer(((CommandSource)context.getSource()).asPlayer());
			} catch (Exception e) {}
		}
		if (siege != null) {
			return ISuggestionProvider.suggest(siege.listTeamNames().stream(), builder);
		}
		return Suggestions.empty();
    }

	@Override
    public Collection<String> getExamples() {
        return Collections.emptyList();
    }

}
