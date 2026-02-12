package it.raniero.fulcrum.spigot.command.register;

import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.spigot.FulcrumSpigot;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

@RequiredArgsConstructor
public class SpigotCommandRegister implements ICommandRegister {

    private final FulcrumSpigot fulcrumSpigot;

    private final SimpleCommandMap commandMap;
    private final Map<String, FulcrumCommandSpigot> spigotCommands = new HashMap<>();

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
            commandMap.register(fulcrumCommand.plugin(), fulcrumCommand.getExecutor());
            spigotCommands.put(fulcrumCommand.scheme().label(), fulcrumCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterCommand(String name) {}

    public void unregisterCommands() {
        try {

            Map<String, Command> knownCommands = (Map<String, Command>) getKnownCommands();

            for (FulcrumCommandSpigot command : spigotCommands.values()) {
                knownCommands.remove(command.scheme().label());
                knownCommands.remove(command.plugin() + ":" + command.scheme().label());
                if (command.scheme().aliases() != null
                        && !command.scheme().aliases().isEmpty()) {
                    for (String s : command.scheme().aliases()) {
                        knownCommands.remove(s);
                        knownCommands.remove(command.plugin() + ":" + s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getKnownCommands() {
        try {

            Field commandsField = commandMap.getClass().getDeclaredField("knownCommands");
            commandsField.setAccessible(true);

            return commandsField.get(commandMap);

        } catch (Exception e) {

            try {

                return commandMap
                        .getClass()
                        .getDeclaredMethod("getKnownCommands")
                        .invoke(commandMap);
            } catch (Exception ex) {
                fulcrumSpigot.getLogger().log(Level.SEVERE, "Can't get knownCommands", e);
            }
        }

        return null;
    }
}
