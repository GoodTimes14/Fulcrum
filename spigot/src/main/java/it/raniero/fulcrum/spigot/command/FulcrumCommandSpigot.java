package it.raniero.fulcrum.spigot.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.spigot.command.executor.FulcrumCommandExecutor;
import lombok.Getter;

public abstract class FulcrumCommandSpigot extends FulcrumCommand {

    @Getter
    private FulcrumCommandExecutor executor;

    public FulcrumCommandSpigot(Fulcrum fulcrum) {
        super(fulcrum);

    }


    @Override
    public void registerScheme(CommandScheme scheme) {
        super.registerScheme(scheme);
        this.executor = new FulcrumCommandExecutor(this,getFulcrum().getPlugin().getFulcrumServer());
        getFulcrum().getPlugin().getCommmandRegister().registerCommand(this);
    }
    
    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {

    }
}
