package it.raniero.fulcrum.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.CommandContext;
import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.command.exception.FulcrumCommandException;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class FulcrumCommand implements IFulcrumCommand {


    @Getter
    private final Fulcrum fulcrum;

    @Getter
    private CommandScheme commandScheme;


    @Override
    public void registerScheme(CommandScheme commandScheme) {
        if (this.commandScheme != null) {
            throw new FulcrumCommandException(this, "CommandScheme is already registered");
        }

        this.commandScheme = commandScheme;
    }

    @Override
    public void executeCommand(FulcrumServer server, FulcrumSource source,String label, String[] args) {

        if (commandScheme == null) {
            throw new FulcrumCommandException(this,"CommandScheme is not registered");
        }

        if (commandScheme.source() != SourceType.ALL && source.sourceType() != commandScheme.source()) {
            //ERROR
        }

        CommandContext context = new CommandContext(source,new LinkedList<>(),args);
        CommandScheme currentScheme = commandScheme;
        LinkedHashMap<String, Argument> arguments = commandScheme.arguments();
        Iterator<Map.Entry<String,Argument>> iterator = arguments.entrySet().iterator();

        boolean searchingScheme = true;


        for (int i = 0; i < args.length; i++) {

            String parameter = args[i];
            if (searchingScheme && currentScheme.subCommands().containsKey(parameter)) {
                currentScheme = currentScheme.subCommands().get(parameter);
                arguments = currentScheme.arguments();
                iterator =  arguments.entrySet().iterator();

                source.sendMessage("Found subcommand " + currentScheme.label());
                continue;

            } else {
                searchingScheme = false;
            }

            if (iterator.hasNext()) {

                Map.Entry<String,Argument> entry = iterator.next();
                entry.getValue().compileArgument(i,fulcrum.getConversionManager(),server,context);

                if(context.result() == ContextResult.INVALID_ARGUMENTS) {
                    //SEND ERROR MESSAGE
                    break;
                }
            }
        }

        if (context.result() == ContextResult.OK) {
            currentScheme.commandExecutor().accept(context);
        }

    }

    public abstract String plugin();

    public abstract void sendCommandUsage(FulcrumSource source,String label,CommandScheme scheme);

}
