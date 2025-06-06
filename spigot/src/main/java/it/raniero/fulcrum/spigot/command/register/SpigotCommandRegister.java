package it.raniero.fulcrum.spigot.command.register;

import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.command.IFulcrumCommand;
import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.spigot.FulcrumSpigot;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SpigotCommandRegister implements ICommandRegister {


    private FulcrumSpigot fulcrumSpigot;
    private final SimpleCommandMap commandMap;


    @SneakyThrows
    public SpigotCommandRegister(FulcrumSpigot fulcrumSpigot) {
        this.fulcrumSpigot = fulcrumSpigot;
        Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);
        commandMap = (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getServer());
    }

    @Override
    public void registerCommand(IFulcrumCommand command) {

        FulcrumCommandSpigot fulcrumCommand = (FulcrumCommandSpigot) command;

        try {
            commandMap.register(fulcrumCommand.plugin(),command.getExecutor());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
