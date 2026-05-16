package it.raniero.fulcrum.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.command.scheme.argument.impl.GroupedArgument;
import it.raniero.fulcrum.api.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.api.utils.CommandUtils;
import it.raniero.fulcrum.api.utils.MessageUtils;
import it.raniero.fulcrum.command.context.CommandContext;
import it.raniero.fulcrum.command.exception.FulcrumCommandException;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import java.util.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class FulcrumCommand implements IFulcrumCommand {

    @Getter
    private final Fulcrum fulcrum;

    @Getter
    private CommandScheme commandScheme;

    public FulcrumCommand(Fulcrum fulcrum) {
        this(fulcrum, true);
    }

    public FulcrumCommand(Fulcrum fulcrum, boolean registerScheme) {
        this.fulcrum = fulcrum;
        if (registerScheme) {
            registerScheme(scheme());
        }
    }

    @Override
    public void registerScheme(CommandScheme commandScheme) {
        this.commandScheme = commandScheme;
        this.commandScheme.parentizeSubCommands();
    }

    @Override
    public void executeCommand(FulcrumServer server, FulcrumSource source, String label, String[] args) {

        if (commandScheme == null) {
            throw new FulcrumCommandException(this, "CommandScheme is not registered");
        }

        if (commandScheme.source() != null
                && commandScheme.source() != SourceType.ALL
                && source.sourceType() != commandScheme.source()) {
            // ERROR
            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.INVALID_COMMAND_SOURCE));
            return;
        }

        // Searches for the current Command scheme first
        LinkedList<String> linkedArgs = new LinkedList<>(Arrays.asList(args));
        int preSearchSize = linkedArgs.size();

        CommandContext context = new CommandContext(source, new LinkedList<>(), args, label, server);
        CommandScheme currentScheme = getCurrentScheme(context, linkedArgs, false);
        if (!currentScheme.checkPermission(source)) {
            context.result(ContextResult.NO_PERMISSION);
        }

        // then, if everything is right, it tries to compile the arguments
        if (context.result() == ContextResult.OK) {
            int argumentStartIndex = preSearchSize - linkedArgs.size();
            compileArguments(context, currentScheme, linkedArgs, argumentStartIndex);
        }

        if (context.result() == ContextResult.INVALID_ARGUMENTS) {

            context.source()
                    .sendMessage(fulcrum.getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.INVALID_COMMAND_ARGUMENTS));

            sendCommandUsage(context.source(), context.label(), currentScheme);
        }

        if (context.result() == ContextResult.NO_PERMISSION) {

            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.NO_PERMISSION_SOURCE));
            return;
        }

        if (context.result() == ContextResult.OK) {
            if (currentScheme.commandExecutor() == null) {
                sendCommandHelp(source, label, currentScheme, context.result(), true);
            } else {
                currentScheme.commandExecutor().accept(context);
            }
        }
    }

    @Override
    public List<String> executeTabCompletion(FulcrumServer server, FulcrumSource source, String label, String[] args) {
        if (commandScheme == null) {
            throw new FulcrumCommandException(this, "CommandScheme is not registered");
        }

        if (commandScheme.source() != null
                && commandScheme.source() != SourceType.ALL
                && source.sourceType() != commandScheme.source()) {
            return new ArrayList<>();
        }
        LinkedList<String> linkedArgs = new LinkedList<>(Arrays.asList(args));
        int currentArgsSize = linkedArgs.size();
        String lastArg = linkedArgs.peekLast();

        CommandContext context = new CommandContext(source, new LinkedList<>(), args, label, server);
        CommandScheme currentScheme = getCurrentScheme(context, linkedArgs, lastArg == null || !lastArg.isEmpty());
        if (context.result() == ContextResult.NO_PERMISSION) {
            return new ArrayList<>();
        }

        int argumentStartIndex = currentArgsSize - linkedArgs.size();
        compileArguments(context, currentScheme, linkedArgs, argumentStartIndex);
        List<Argument> commandArguments =
                new ArrayList<>(currentScheme.arguments().values());

        Argument lastArgument = commandArguments.isEmpty() ? null : commandArguments.get(commandArguments.size() - 1);
        Set<String> output = new HashSet<>();

        if (!currentScheme.checkPermission(source)) {
            return new ArrayList<>();
        }

        if (!commandArguments.isEmpty() && linkedArgs.size() <= commandArguments.size()) {
            if (lastArgument instanceof GroupedArgument groupedArgument) {

                List<String> strings = new ArrayList<>(Arrays.stream(groupedArgument.values())
                        .map(Object::toString)
                        .toList());

                CommandUtils.filterStringsByInput(lastArg, strings);
                output.addAll(strings);

            } else if (lastArgument instanceof NormalArgument normalArgument) {

                if (normalArgument.type() == server.getPlayerClass() || normalArgument.suggestPlayersInTab()) {
                    output.addAll(server.getOnlinePlayerNames(lastArg));
                }
            }
        }

        if (currentScheme.tabExecutor() != null) {
            List<String> completions = currentScheme.tabExecutor().apply(context);
            if (completions != null) {
                output.addAll(completions);
            }
        }

        if (linkedArgs.size() <= 1) {
            output.addAll(CommandUtils.filterStringsByInput(
                    lastArg,
                    new ArrayList<>(currentScheme.subCommands().keySet().stream()
                            .filter(name ->
                                    currentScheme.subCommands().get(name).checkPermission(source))
                            .toList())));
        }

        return new ArrayList<>(output);
    }

    private void compileArguments(
            CommandContext context, CommandScheme currentScheme, LinkedList<String> args, int index) {

        Iterator<Map.Entry<String, Argument>> iterator =
                currentScheme.arguments().entrySet().iterator();

        for (int i = 0; i < args.size(); i++) {
            if (iterator.hasNext()) {
                Map.Entry<String, Argument> entry = iterator.next();
                entry.getValue().compileArgument(i + index, fulcrum.getConversionManager(), context.server(), context);
            }
        }

        if (iterator.hasNext()) {
            Map.Entry<String, Argument> entry = iterator.next();
            if (entry.getValue().required()) {
                context.result(ContextResult.INVALID_ARGUMENTS);
            }
        }
    }

    private CommandScheme getCurrentScheme(CommandContext context, LinkedList<String> args, boolean poll) {
        CommandScheme currentScheme = commandScheme;
        if (!currentScheme.checkPermission(context.source())) {
            context.result(ContextResult.NO_PERMISSION);
            return currentScheme;
        }

        if (poll) {
            args.pollLast();
        }

        while (!args.isEmpty()) {
            String parameter = args.peekFirst();
            if (currentScheme.subCommands().containsKey(parameter)) {

                currentScheme = currentScheme.subCommands().get(parameter);
                if (!currentScheme.checkPermission(context.source())) {
                    context.result(ContextResult.NO_PERMISSION);
                    break;
                }

                args.pollFirst();

            } else {
                break;
            }
        }

        return currentScheme;
    }

    public List<CommandScheme> getSchemeParents(CommandScheme scheme) {
        List<CommandScheme> schemes = new ArrayList<>();
        CommandScheme parent = scheme.parent();
        if (parent != null) {
            schemes.add(parent);
            while (parent != null) {

                if (parent.parent() == null) break; // Root Command
                parent = parent.parent();

                schemes.add(0, parent);
            }

            schemes.add(scheme);
        }

        return schemes;
    }

    private CommandScheme getSchemeParent(CommandScheme scheme) {
        CommandScheme out = scheme;
        while (out.parent() != null) {
            out = out.parent();
        }

        return out;
    }

    public void sendCommandHelp(
            FulcrumSource source, String label, CommandScheme scheme, ContextResult result, boolean sendPreamble) {
        String preamble = fulcrum.getMainConfig()
                .get(
                        FulcrumMessagesHolder.class,
                        result == ContextResult.INVALID_ARGUMENTS
                                ? FulcrumMessagesHolder.INVALID_COMMAND_ARGUMENTS
                                : FulcrumMessagesHolder.COMMAND_HELP_PREAMBLE);

        CommandScheme parent = getSchemeParent(scheme);

        if (sendPreamble) {
            String labelColor = parent.labelColor() == null
                    ? fulcrum.getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_LABEL_COLOR)
                    : parent.labelColor();

            String argumentColor = parent.argumentColor() == null
                    ? fulcrum.getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_COLOR)
                    : parent.argumentColor();

            String descriptionColor = parent.descriptionColor() == null
                    ? fulcrum.getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_DESCRIPTION_COLOR)
                    : parent.descriptionColor();

            source.sendMessage(preamble.replace("%label_color%", labelColor)
                    .replace("%argument_color%", argumentColor)
                    .replace("%description_color%", descriptionColor)
                    .replace("%label%", label));
        }

        if (!scheme.arguments().isEmpty() || scheme.subCommands().isEmpty()) {
            sendCommandUsage(source, label, scheme);
        }

        if (!scheme.subCommands().isEmpty()) {
            for (CommandScheme subScheme : scheme.subCommands().values()) {
                sendCommandHelp(source, label, subScheme, result, false);
            }
        }
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        if (!scheme.checkPermission(source)) {
            return;
        }

        List<CommandScheme> schemes = getSchemeParents(scheme);
        CommandScheme parent = !schemes.isEmpty() ? schemes.get(0) : scheme;
        String labelColor = parent.labelColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_LABEL_COLOR)
                : parent.labelColor();

        Component commandComponent = MessageUtils.convertLegacyText(labelColor + "/" + label);
        if (!schemes.isEmpty()) {
            schemes.remove(0);
        }

        commandComponent = addSubCommandComponents(commandComponent, schemes, parent);
        commandComponent = addArgumentComponents(commandComponent, scheme, parent);
        commandComponent = addDescriptionComponent(commandComponent, scheme, parent);

        source.sendMessage(commandComponent);
    }

    private Component addSubCommandComponents(Component component, List<CommandScheme> schemes, CommandScheme parent) {

        String defaultLabelColor = parent.labelColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_SUBLABEL_COLOR)
                : parent.labelColor();

        for (CommandScheme subScheme : schemes) {

            String subCommandColor = subScheme.labelColor() == null ? defaultLabelColor : subScheme.labelColor();

            String description = subScheme.description() == null ? "N/D" : subScheme.description();

            Component subComponent = MessageUtils.convertLegacyText(subCommandColor + " " + subScheme.label())
                    .hoverEvent(HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            MessageUtils.convertLegacyText(subCommandColor + description)));

            component = component.append(subComponent);
        }

        return component;
    }

    private Component addArgumentComponents(Component component, CommandScheme scheme, CommandScheme parent) {
        String argumentColor = parent.argumentColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_COLOR)
                : parent.argumentColor();
        for (Argument argument : scheme.arguments().values()) {

            String hoverColor = parent.argumentHoverColor() == null
                    ? getFulcrum()
                            .getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_HOVER_COLOR)
                    : parent.argumentHoverColor();

            String description = argument.description() == null ? "N/D" : argument.description();

            Component argumentComponent = MessageUtils.convertLegacyText(argumentColor + " " + argument.display())
                    .hoverEvent(HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT, MessageUtils.convertLegacyText(hoverColor + description)));

            component = component.append(argumentComponent);
        }

        return component;
    }

    private Component addDescriptionComponent(Component component, CommandScheme scheme, CommandScheme parent) {
        String descriptionColor = parent.descriptionColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_DESCRIPTION_COLOR)
                : parent.descriptionColor();

        String description = scheme.description() == null ? "N/D" : scheme.description();

        return component.append(MessageUtils.convertLegacyText("&8 - " + descriptionColor + description));
    }
}
