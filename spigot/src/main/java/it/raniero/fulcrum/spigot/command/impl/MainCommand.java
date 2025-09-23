package it.raniero.fulcrum.spigot.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MainCommand extends FulcrumCommandSpigot {

    public MainCommand(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public String plugin() {
        return "fulcrum";
    }

    public void provaCommand(ICommandContext context) {

        String ciao = context.argument(0, String.class).orElse("defaultsss");

        context.source().sendMessage("provaaaaaa: " + ciao);
    }

    public void mainCommand(ICommandContext context) {

        Bukkit.broadcastMessage("suuuca");
        Player dayum = context.argument(0, Player.class).orElse(null);
        context.source().sendMessage("Testtttt " + dayum);
    }

    @Override
    public CommandScheme scheme() {
        return CommandScheme.builder()
                .label("fulcrum")
                .source(SourceType.ALL)
                .description("Main Fulcrum command")
                .commandExecutor(this::mainCommand)
                .subCommand(CommandScheme.builder()
                        .label("prova")
                        .commandExecutor(this::provaCommand)
                        .argument(NormalArgument.builder()
                                .name("cazzo")
                                .description("Prova argument")
                                .required(true)
                                .type(String.class)
                                .required(true)
                                .suggestPlayersInTab(true)
                                .build())
                        .subCommand(CommandScheme.builder()
                                .label("gaddayum")
                                .commandExecutor(this::provaCommand)
                                .argument(NormalArgument.builder()
                                        .name("cccp")
                                        .description("sssr")
                                        .required(true)
                                        .type(String.class)
                                        .required(true)
                                        .suggestPlayersInTab(true)
                                        .build())
                                .build())
                        .build())
                .argument(NormalArgument.builder()
                        .name("sium")
                        .type(Player.class)
                        .required(true)
                        .description("Sium argument")
                        .build())
                .build();
    }
}
