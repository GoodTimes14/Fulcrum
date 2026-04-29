package it.raniero.fulcrum.terminal.command.register;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.terminal.server.sender.TerminalSource;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TerminalCommandRegister implements ICommandRegister {

    private final Fulcrum fulcrum;
    private final FulcrumServer fulcrumServer;
    private final TerminalSource terminalSource;

    private final Map<String, IFulcrumCommand> commandsByAlias = new HashMap<>();
    private final Map<String, IFulcrumCommand> commandsByLabel = new HashMap<>();
    private final Map<String, List<String>> registeredAliasesByLabel = new HashMap<>();

    @Override
    public void registerCommand(IFulcrumCommand command) {
        String label = command.scheme().label().toLowerCase();
        List<String> aliases = getAliases(command);

        commandsByLabel.put(label, command);
        for (String alias : aliases) {
            commandsByAlias.put(alias, command);
        }
        registeredAliasesByLabel.put(label, aliases);
    }

    @Override
    public void wrapCommand(IFulcrumCommand command) {
        registerCommand(command);
    }

    @Override
    public void unregisterCommand(String name) {
        if (name == null) {
            return;
        }

        String label = name.toLowerCase();
        IFulcrumCommand command = commandsByLabel.remove(label);

        if (command == null) {
            command = commandsByAlias.remove(label);
            if (command == null) {
                return;
            }
            label = command.scheme().label().toLowerCase();
            commandsByLabel.remove(label);
        }

        List<String> aliases = registeredAliasesByLabel.remove(label);
        if (aliases != null) {
            for (String alias : aliases) {
                commandsByAlias.remove(alias);
            }
        }
    }

    @Override
    public void unregisterCommands() {
        commandsByAlias.clear();
        commandsByLabel.clear();
        registeredAliasesByLabel.clear();
    }

    public IFulcrumCommand getCommand(String labelOrAlias) {
        if (labelOrAlias == null) {
            return null;
        }
        return commandsByAlias.get(labelOrAlias.toLowerCase());
    }

    public Set<String> getLabelsAndAliases() {
        return new LinkedHashSet<>(commandsByAlias.keySet());
    }

    public void executeCommand(String label, String[] args) {
        IFulcrumCommand command = getCommand(label);
        if (command == null) {
            terminalSource.sendMessage(fulcrum.getMainConfig()
                    .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.TERMINAL_UNKNOWN_COMMAND));

            return;
        }

        command.executeCommand(fulcrumServer, terminalSource, label, args);
    }

    public List<String> suggest(String label, String[] args) {
        IFulcrumCommand command = getCommand(label);
        if (command == null) {
            return new ArrayList<>();
        }

        return command.executeTabCompletion(fulcrumServer, terminalSource, label, args);
    }

    private List<String> getAliases(IFulcrumCommand command) {
        List<String> aliases = new ArrayList<>();
        aliases.add(command.scheme().label().toLowerCase());

        if (command.scheme().aliases() != null) {
            for (String alias : command.scheme().aliases()) {
                if (alias != null && !alias.isBlank()) {
                    aliases.add(alias.toLowerCase());
                }
            }
        }

        return aliases;
    }
}
