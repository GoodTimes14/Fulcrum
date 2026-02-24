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
import it.raniero.fulcrum.command.context.CommandContext;
import it.raniero.fulcrum.command.exception.FulcrumCommandException;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import java.util.*;
import lombok.Getter;

public abstract class FulcrumCommand implements IFulcrumCommand {

    @Getter
    private final Fulcrum fulcrum;

    @Getter
    private CommandScheme commandScheme;

    public FulcrumCommand(Fulcrum fulcrum) {
        this.fulcrum = fulcrum;
        registerScheme(scheme());
    }

    @Override
    public void registerScheme(CommandScheme commandScheme) {
        if (this.commandScheme != null) {
            throw new FulcrumCommandException(this, "CommandScheme is already registered");
        }

        this.commandScheme = commandScheme;
        this.commandScheme.parentizeSubCommands();
    }

    @Override
    public void executeCommand(FulcrumServer server, FulcrumSource source, String label, String[] args) {

        if (commandScheme == null) {
            throw new FulcrumCommandException(this, "CommandScheme is not registered");
        }

        if (commandScheme.source() != SourceType.ALL && source.sourceType() != commandScheme.source()) {
            // ERROR
            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.INVALID_COMMAND_SOURCE));
        }

        CommandContext context = new CommandContext(source, new LinkedList<>(), args);
        CommandScheme currentScheme = commandScheme;
        if (!currentScheme.checkPermission(source)) {
            context.setResult(ContextResult.NO_PERMISSION);
        }

        LinkedHashMap<String, Argument> arguments = commandScheme.arguments();
        Iterator<Map.Entry<String, Argument>> iterator = arguments.entrySet().iterator();

        boolean searchingScheme = true;

        if (context.result() == ContextResult.OK) {
            for (int i = 0; i < args.length; i++) {

                String parameter = args[i];
                if (searchingScheme && currentScheme.subCommands().containsKey(parameter)) {
                    currentScheme = currentScheme.subCommands().get(parameter);
                    if (!currentScheme.checkPermission(source)) {
                        context.setResult(ContextResult.NO_PERMISSION);
                        break;
                    }

                    arguments = currentScheme.arguments();
                    iterator = arguments.entrySet().iterator();

                    continue;

                } else {
                    searchingScheme = false;
                }

                if (iterator.hasNext()) {

                    Map.Entry<String, Argument> entry = iterator.next();
                    entry.getValue().compileArgument(i, fulcrum.getConversionManager(), server, context);

                    if (context.result() == ContextResult.INVALID_ARGUMENTS) {

                        source.sendMessage(fulcrum.getMainConfig()
                                .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.INVALID_COMMAND_ARGUMENTS));

                        sendCommandUsage(source, label, currentScheme);
                        break;
                    }
                }
            }
        }

        if (context.result() == ContextResult.NO_PERMISSION || !currentScheme.checkPermission(source)) {
            context.setResult(ContextResult.NO_PERMISSION);
            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.NO_PERMISSION_SOURCE));
            return;
        }

        if (iterator.hasNext()) {
            Map.Entry<String, Argument> entry = iterator.next();
            if (entry.getValue().required()) {
                sendCommandHelp(source, label, currentScheme);
                context.setResult(ContextResult.INVALID_ARGUMENTS);
            }
        }

        if (context.result() == ContextResult.OK) {
            if (currentScheme.commandExecutor() == null) {
                sendCommandHelp(source, label, currentScheme);
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

        if ((commandScheme.source() == null || commandScheme.source() != SourceType.ALL)
                && source.sourceType() != commandScheme.source()) {
            return new ArrayList<>();
        }

        LinkedList<String> linkedArgs = new LinkedList<>(Arrays.asList(args));
        String lastArg = linkedArgs.peekLast();

        CommandScheme currentScheme = getCurrentScheme(linkedArgs, lastArg == null || !lastArg.isEmpty());
        List<Argument> commandArguments =
                new ArrayList<>(currentScheme.arguments().values());

        String lastInput = linkedArgs.isEmpty() ? "" : linkedArgs.getLast();
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

                CommandUtils.filterStringsByInput(lastInput, strings);
                output.addAll(strings);

            } else if (lastArgument instanceof NormalArgument normalArgument) {

                if (normalArgument.type() == server.getPlayerClass() || normalArgument.suggestPlayersInTab()) {
                    output.addAll(server.getOnlinePlayerNames(lastInput));
                }
            }
        }

        if (linkedArgs.size() <= 1) {
            output.addAll(CommandUtils.filterStringsByInput(
                    lastInput,
                    new ArrayList<>(currentScheme.subCommands().keySet().stream()
                            .filter(name ->
                                    currentScheme.subCommands().get(name).checkPermission(source))
                            .toList())));
        }

        return new ArrayList<>(output);
    }

    private CommandScheme getCurrentScheme(LinkedList<String> args, boolean poll) {
        CommandScheme currentScheme = commandScheme;
        if (poll) {
            args.pollLast();
        }

        while (!args.isEmpty()) {
            String parameter = args.peekFirst();
            if (currentScheme.subCommands().containsKey(parameter)) {

                currentScheme = currentScheme.subCommands().get(parameter);
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

    public void sendCommandHelp(FulcrumSource source, String label, CommandScheme scheme) {
        if (!scheme.arguments().isEmpty() || scheme.subCommands().isEmpty()) {
            sendCommandUsage(source, label, scheme);
        }

        if (!scheme.subCommands().isEmpty()) {
            for (CommandScheme subScheme : scheme.subCommands().values()) {
                sendCommandHelp(source, label, subScheme);
            }
        }
    }

    public abstract void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme);
}
