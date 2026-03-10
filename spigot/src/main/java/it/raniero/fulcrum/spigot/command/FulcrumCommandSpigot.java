package it.raniero.fulcrum.spigot.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.spigot.command.executor.FulcrumCommandExecutor;
import lombok.Getter;

public abstract class FulcrumCommandSpigot extends FulcrumCommand {

    @Getter
    private FulcrumCommandExecutor executor;

    public FulcrumCommandSpigot(Fulcrum fulcrum) {
        super(fulcrum);
    }

    public FulcrumCommandSpigot(Fulcrum fulcrum, boolean registerScheme) {
        super(fulcrum, registerScheme);
    }

    @Override
    public void registerScheme(CommandScheme scheme) {
        super.registerScheme(scheme);
        this.executor =
                new FulcrumCommandExecutor(this, getFulcrum().getPlugin().getFulcrumServer());
    }
}
