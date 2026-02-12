package it.raniero.fulcrum.velocity.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import it.raniero.fulcrum.velocity.command.FulcrumCommandVelocity;

public class MainVelocityCommand extends FulcrumMainCommand {

    private final FulcrumCommandVelocity commandVelocity;

    public MainVelocityCommand(Fulcrum fulcrum) {
        super(fulcrum);

        FulcrumMainCommand command = this;
        this.commandVelocity = new FulcrumCommandVelocity(fulcrum) {
            @Override
            public String plugin() {
                return command.plugin();
            }

            @Override
            public CommandScheme scheme() {
                return command.scheme();
            }
        };
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        commandVelocity.sendCommandUsage(source, label, scheme);
    }
}
