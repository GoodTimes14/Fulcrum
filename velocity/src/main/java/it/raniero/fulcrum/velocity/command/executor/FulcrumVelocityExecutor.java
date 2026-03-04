package it.raniero.fulcrum.velocity.command.executor;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.velocity.server.sender.FulcrumVelocitySource;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FulcrumVelocityExecutor implements SimpleCommand {

    private final FulcrumServer fulcrumServer;

    private final IFulcrumCommand command;

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String label = invocation.alias();

        command.executeCommand(fulcrumServer, new FulcrumVelocitySource(source), label, args);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String label = invocation.alias();

        return command.executeTabCompletion(fulcrumServer, new FulcrumVelocitySource(source), label, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource source = invocation.source();
        return command.scheme().checkPermission(new FulcrumVelocitySource(source));
    }
}
