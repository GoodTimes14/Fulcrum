package it.raniero.fulcrum.bungeecord.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.bungeecord.command.FulcrumCommandBungee;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import lombok.Getter;

public class MainBungeeCommand extends FulcrumMainCommand {

    @Getter
    private final FulcrumCommandBungee bungeeCommand;

    public MainBungeeCommand(Fulcrum fulcrum) {
        super(fulcrum);
        FulcrumMainCommand command = this;
        CommandScheme scheme = command.scheme().label("fulcrumbungee");
        this.bungeeCommand = new FulcrumCommandBungee(fulcrum) {
            @Override
            public String plugin() {
                return command.plugin();
            }

            @Override
            public CommandScheme scheme() {
                return scheme;
            }
        };
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        bungeeCommand.sendCommandUsage(source, label, scheme);
    }
}
