package it.raniero.fulcrum.spigot;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.FulcrumPlugin;
import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.server.FulcrumServer;
import it.raniero.fulcrum.spigot.command.impl.MainCommand;
import it.raniero.fulcrum.spigot.command.register.SpigotCommandRegister;
import it.raniero.fulcrum.spigot.server.FulcrumServerSpigot;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FulcrumSpigot extends JavaPlugin implements FulcrumPlugin {

    private final FulcrumServer fulcrumServer = new FulcrumServerSpigot();
    ;

    private SpigotCommandRegister commandRegister;

    private Fulcrum fulcrum;

    @Override
    public void onLoad() {
        fulcrum = new Fulcrum();
        commandRegister = new SpigotCommandRegister(this);
    }

    @Override
    public void onEnable() {
        fulcrum.start(this);
        fulcrum.getCommandManager().registerCommand(new MainCommand(fulcrum));
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
