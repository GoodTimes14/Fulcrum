package it.raniero.fulcrum.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.CommandContext;
import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class FulcrumCommand implements IFulcrumCommand {


    private final Fulcrum fulcrum;

    @Getter
    private final CommandScheme commandScheme;



    @Override
    public void executeCommand(FulcrumServer server, FulcrumSource source, String[] args) {

        if(commandScheme.source() != SourceType.ALL && source.sourceType() != commandScheme.source()) {
            //ERROR
        }

        CommandContext context = new CommandContext(source,new ArrayList<>(),args);
        CommandScheme currentScheme = commandScheme;
        LinkedHashMap<String, Argument> arguments = commandScheme.arguments();
        Iterator<Map.Entry<String,Argument>> iterator = arguments.sequencedEntrySet().iterator();

        boolean searchingScheme = true;


        for (int i = 0; i < args.length; i++) {

            String parameter = args[i];
            if (searchingScheme && commandScheme.subCommands().containsKey(parameter)) {
                currentScheme = currentScheme.subCommands().get(parameter);
                arguments = commandScheme.arguments();
                iterator = arguments.sequencedEntrySet().iterator();

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
            commandScheme.commandExecutor().accept(context);
        }

    }

    public abstract String plugin();

}
