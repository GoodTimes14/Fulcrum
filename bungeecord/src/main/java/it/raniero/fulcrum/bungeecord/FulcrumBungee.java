package it.raniero.fulcrum.bungeecord;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.bungeecord.command.register.BungeeCordCommandRegister;
import it.raniero.fulcrum.bungeecord.server.FulcrumBungeeCordServer;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class FulcrumBungee extends Plugin implements FulcrumPlugin {

    private final FulcrumServer fulcrumServer = new FulcrumBungeeCordServer();

    private final ICommandRegister commandRegister = new BungeeCordCommandRegister(this);

    private Fulcrum fulcrum;

    @Override
    public void onLoad() {
        fulcrum = new Fulcrum();
    }

    @Override
    public void onEnable() {
        fulcrum.start(this);
        fulcrum.getCommandManager().wrapCommand(new FulcrumMainCommand(fulcrum));
    }

    @Override
    public void onDisable() {
        fulcrum.stop();
    }

    @Override
    public ICommandRegister getCommandRegister() {
        return commandRegister;
    }
}
