package it.raniero.fulcrum.spigot.command.register;

import it.raniero.fulcrum.command.IFulcrumCommand;
import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import java.lang.reflect.Field;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

public class SpigotCommandRegister implements ICommandRegister {

    private final SimpleCommandMap commandMap;

    @SneakyThrows
    public SpigotCommandRegister() {
        Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);
        commandMap = (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getServer());
    }

    @Override
    public void registerCommand(IFulcrumCommand command) {

        FulcrumCommandSpigot fulcrumCommand = (FulcrumCommandSpigot) command;

        try {
            commandMap.register(fulcrumCommand.plugin(), fulcrumCommand.getExecutor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
