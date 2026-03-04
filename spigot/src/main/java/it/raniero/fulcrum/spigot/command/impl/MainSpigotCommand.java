package it.raniero.fulcrum.spigot.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import lombok.Getter;

public class MainSpigotCommand extends FulcrumMainCommand {

    @Getter
    private final FulcrumCommandSpigot spigotCommand;

    public MainSpigotCommand(Fulcrum fulcrum) {
        super(fulcrum);

        FulcrumMainCommand command = this;
        this.spigotCommand = new FulcrumCommandSpigot(fulcrum) {
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
        spigotCommand.sendCommandUsage(source, label, scheme);
    }
}
