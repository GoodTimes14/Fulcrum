package it.raniero.fulcrum.bungeecord.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.bungeecord.command.executor.FulcrumBungeeExecutor;
import it.raniero.fulcrum.command.FulcrumCommand;
import lombok.Getter;

@Getter
public abstract class FulcrumCommandBungee extends FulcrumCommand {

    private FulcrumBungeeExecutor executor;

    public FulcrumCommandBungee(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public void registerScheme(CommandScheme commandScheme) {
        super.registerScheme(commandScheme);

        executor = new FulcrumBungeeExecutor(this, getFulcrum().getPlugin().getFulcrumServer());
    }
}
