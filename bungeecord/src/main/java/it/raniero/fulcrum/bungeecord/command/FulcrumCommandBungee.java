package it.raniero.fulcrum.bungeecord.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.bungeecord.command.executor.FulcrumBungeeExecutor;
import it.raniero.fulcrum.command.FulcrumCommand;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Getter
public abstract class FulcrumCommandBungee extends FulcrumCommand {

    private FulcrumBungeeExecutor executor;

    public FulcrumCommandBungee(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public void registerScheme(CommandScheme commandScheme) {
        super.registerScheme(commandScheme);

        executor = new FulcrumBungeeExecutor(this, getFulcrum().getPlugin().getFulcrumServer());
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        CommandSender sender = (CommandSender) source.getSourceObject();
        TextComponent commandComponent = new TextComponent(ChatColor.RED + "/" + label);
        if (!scheme.checkPermission(source)) {
            return;
        }

        CommandScheme parent = scheme.parent();
        if (parent != null) {
            while (parent != null) {
                if (parent.parent() == null) break; // Reached root command

                commandComponent.addExtra(" " + parent.label());
                parent = parent.parent();
            }

            commandComponent.addExtra(" " + scheme.label());
        }

        for (Argument argument : scheme.arguments().values()) {

            TextComponent component = new TextComponent("<" + argument.name() + ">");
            ComponentBuilder builder = new ComponentBuilder(ChatColor.RED + argument.description());

            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()));
            commandComponent.addExtra(" ");
            commandComponent.addExtra(component);
        }

        String description = scheme.description() == null ? "No description provided." : scheme.description();
        commandComponent.addExtra(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + description);

        if (sender instanceof ProxiedPlayer player) {

            player.sendMessage(commandComponent);

        } else {
            sender.sendMessage(new TextComponent(commandComponent.toPlainText()));
        }
    }
}
