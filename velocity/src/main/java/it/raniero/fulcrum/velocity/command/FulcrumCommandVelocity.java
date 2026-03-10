package it.raniero.fulcrum.velocity.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.FulcrumCommand;

public abstract class FulcrumCommandVelocity extends FulcrumCommand {

    public FulcrumCommandVelocity(Fulcrum fulcrum) {
        super(fulcrum);
    }

    public FulcrumCommandVelocity(Fulcrum fulcrum, boolean registerScheme) {
        super(fulcrum, registerScheme);
    }
}
