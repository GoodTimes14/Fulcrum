package it.raniero.fulcrum.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.CommandContext;
import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.command.exception.FulcrumCommandException;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.command.scheme.argument.impl.GroupedArgument;
import it.raniero.fulcrum.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.server.FulcrumServer;
import it.raniero.fulcrum.utils.CommandUtils;
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
        LinkedHashMap<String, Argument> arguments = commandScheme.arguments();
        Iterator<Map.Entry<String, Argument>> iterator = arguments.entrySet().iterator();

        boolean searchingScheme = true;

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

        if (context.result() == ContextResult.NO_PERMISSION || currentScheme.checkPermission(source)) {
            context.setResult(ContextResult.NO_PERMISSION);
            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.NO_PERMISSION_SOURCE));

        }

        if (iterator.hasNext()) {
            Map.Entry<String, Argument> entry = iterator.next();
            if (entry.getValue().required()) {
                sendCommandHelp(source, label, currentScheme);
                context.setResult(ContextResult.INVALID_ARGUMENTS);
            }
        }

        if (context.result() == ContextResult.OK) {
            currentScheme.commandExecutor().accept(context);
        }
    }

    @Override
    public List<String> executeTabCompletion(FulcrumServer server, FulcrumSource source, String label, String[] args) {
        if (commandScheme == null) {
            throw new FulcrumCommandException(this, "CommandScheme is not registered");
        }

        if (commandScheme.source() != SourceType.ALL && source.sourceType() != commandScheme.source()) {
            // ERROR
            source.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.INVALID_COMMAND_SOURCE));
        }

        LinkedList<String> linkedArgs = new LinkedList<>(Arrays.asList(args));
        CommandScheme currentScheme = getCurrentScheme(linkedArgs);
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
                    new ArrayList<>(currentScheme.subCommands().keySet().stream().filter(
                                    name -> currentScheme.subCommands().get(name).checkPermission(source)).toList())));
        }

        return new ArrayList<>(output);
    }

    private CommandScheme getCurrentScheme(LinkedList<String> args) {
        CommandScheme currentScheme = commandScheme;

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

    public abstract String plugin();

    public abstract void sendCommandHelp(FulcrumSource source, String label, CommandScheme scheme);

    public abstract void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme);
}
