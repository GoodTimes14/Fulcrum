package it.raniero.fulcrum.spigot;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.FulcrumPlugin;
import it.raniero.fulcrum.server.FulcrumServer;
import it.raniero.fulcrum.spigot.server.FulcrumServerSpigot;
import it.raniero.fulcrum.utils.StartupProperties;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class FulcrumSpigot extends JavaPlugin implements FulcrumPlugin {


    @Getter
    private final FulcrumServer fulcrumServer = new FulcrumServerSpigot();;

    private Fulcrum fulcrum;

    @Override
    public void onLoad() {
        fulcrum = new Fulcrum();
    }

    @Override
    public void onEnable() {
        fulcrum.start(this);
    }

    @Override
    public void onDisable() {
        fulcrum.stop();
    }
}
