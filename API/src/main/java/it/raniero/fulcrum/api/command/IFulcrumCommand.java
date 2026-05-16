package it.raniero.fulcrum.api.command;

import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.holder.ICommandSchemeHolder;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.List;

/**
 * Represents a Fulcrum command with execution, help, usage, and tab-completion behavior.
 */
public interface IFulcrumCommand extends ICommandSchemeHolder {

    /**
     * Registers a command scheme handled by this command.
     *
     * @param scheme command scheme to register
     */
    void registerScheme(CommandScheme scheme);

    /**
     * Executes the command for a sender on a platform server.
     *
     * @param server server adapter receiving the command
     * @param sender source that executed the command
     * @param label command label used by the source
     * @param args command arguments
     */
    void executeCommand(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    /**
     * Sends usage information for a command scheme.
     *
     * @param source destination source
     * @param label command label to display
     * @param scheme scheme whose usage should be sent
     */
    void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme);

    /**
     * Sends detailed help for a command scheme and parsing result.
     *
     * @param source destination source
     * @param label command label to display
     * @param scheme scheme whose help should be sent
     * @param result context parsing result
     * @param sendPreamble whether to include the help preamble
     */
    void sendCommandHelp(
            FulcrumSource source, String label, CommandScheme scheme, ContextResult result, boolean sendPreamble);

    /**
     * Executes tab completion for the command.
     *
     * @param server server adapter receiving the completion request
     * @param sender source requesting completions
     * @param label command label used by the source
     * @param args current command arguments
     * @return available completions
     */
    List<String> executeTabCompletion(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    /**
     * Gets the plugin identifier that owns this command.
     *
     * @return owning plugin identifier
     */
    String plugin();
}
