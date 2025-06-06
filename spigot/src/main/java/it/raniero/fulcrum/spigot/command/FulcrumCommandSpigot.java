package it.raniero.fulcrum.spigot.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.command.scheme.CommandScheme;

public abstract class FulcrumCommandSpigot extends FulcrumCommand {

    public FulcrumCommandSpigot(Fulcrum fulcrum, CommandScheme commandScheme) {
        super(fulcrum, commandScheme);
    }



}
