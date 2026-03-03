package it.raniero.fulcrum.spigot;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import it.raniero.fulcrum.spigot.command.register.SpigotCommandRegister;
import it.raniero.fulcrum.spigot.server.FulcrumServerSpigot;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FulcrumSpigot extends JavaPlugin implements FulcrumPlugin {

    private final FulcrumServer fulcrumServer = new FulcrumServerSpigot();

    private SpigotCommandRegister commandRegister;

    private Fulcrum fulcrum;

    private BukkitAudiences adventure;

    @Override
    public void onLoad() {
        fulcrum = new Fulcrum();
        commandRegister = new SpigotCommandRegister(this);
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        fulcrum.start(this);

        fulcrum.getCommandManager().wrapCommand(new FulcrumMainCommand(fulcrum));
    }

    @Override
    public void onDisable() {
        fulcrum.stop();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public ICommandRegister getCommandRegister() {
        return commandRegister;
    }
}
