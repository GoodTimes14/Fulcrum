package it.raniero.fulcrum.api.utils;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.command.scheme.argument.impl.NormalArgument;
import java.util.Comparator;
import java.util.List;

public final class CommandTreeUtils {

    private CommandTreeUtils() {
        throw new IllegalArgumentException("Utility Class");
    }

    /**
     * Gets the full command of a scheme, such as "/gamesync quest task"
     */
    public static String getCommandPath(String label, CommandScheme scheme) {
        StringBuilder path = new StringBuilder();
        for (CommandScheme current = scheme; current.parent() != null; current = current.parent()) {
            path.insert(0, " " + current.label());
        }

        return "/" + label + path;
    }

    /**
     * Gets the deepest level the tree can be opened to without going over the max lines
     */
    public static int getTreeDepth(FulcrumSource source, CommandScheme scheme, int maxLines) {
        int depth = 1;
        while (countTreeLines(source, scheme, depth + 1) > countTreeLines(source, scheme, depth)
                && countTreeLines(source, scheme, depth + 1) <= maxLines) {
            depth++;
        }

        return depth;
    }

    /**
     * Gets the sub commands the source can use, commands first and then groups
     */
    public static List<CommandScheme> getTreeSubCommands(FulcrumSource source, CommandScheme scheme) {
        return scheme.subCommands().values().stream()
                .filter(subScheme -> countTreeCommands(source, subScheme) > 0)
                .sorted(Comparator.comparing(
                        subScheme -> !subScheme.subCommands().isEmpty()))
                .toList();
    }

    /**
     * Counts the commands the source can use inside a scheme
     */
    public static int countTreeCommands(FulcrumSource source, CommandScheme scheme) {
        if (!scheme.checkPermission(source)) {
            return 0;
        }

        if (scheme.subCommands().isEmpty()) {
            return 1;
        }

        return scheme.subCommands().values().stream()
                .mapToInt(subScheme -> countTreeCommands(source, subScheme))
                .sum();
    }

    /**
     * Gets the argument display used by the tree help: &lt;required&gt;, [optional] and name… for the rest of the line
     */
    public static String getTreeDisplay(Argument argument) {
        if (!(argument instanceof NormalArgument normalArgument)) {
            return argument.display();
        }

        String name = normalArgument.type().isAssignableFrom(String[].class)
                ? normalArgument.name() + "…"
                : normalArgument.name();
        return normalArgument.required() ? "<" + name + ">" : "[" + name + "]";
    }

    private static int countTreeLines(FulcrumSource source, CommandScheme scheme, int depth) {
        return getTreeSubCommands(source, scheme).stream()
                .mapToInt(subScheme -> 1 + (depth > 1 ? countTreeLines(source, subScheme, depth - 1) : 0))
                .sum();
    }
}
