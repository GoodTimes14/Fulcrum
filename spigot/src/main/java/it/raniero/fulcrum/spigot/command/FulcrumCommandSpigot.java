package it.raniero.fulcrum.spigot.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.utils.MessageUtils;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.spigot.command.executor.FulcrumCommandExecutor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class FulcrumCommandSpigot extends FulcrumCommand {

    @Getter
    private FulcrumCommandExecutor executor;

    public FulcrumCommandSpigot(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public void registerScheme(CommandScheme scheme) {
        super.registerScheme(scheme);
        this.executor =
                new FulcrumCommandExecutor(this, getFulcrum().getPlugin().getFulcrumServer());
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label,  CommandScheme scheme) {
        CommandSender sender = (CommandSender) source.getSourceObject();
        String labelColor = scheme.labelColor() == null ?
                getFulcrum().getMainConfig().get(FulcrumMessagesHolder.class,FulcrumMessagesHolder.DEFAULT_LABEL_COLOR) :
                scheme.labelColor();

        TextComponent commandComponent = new TextComponent(labelColor + "/" + label);
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
        } else {
            parent = scheme;
        }

        for (Argument argument : scheme.arguments().values()) {

            String argumentColor = parent.argumentColor() == null ?
                    getFulcrum().getMainConfig().get(FulcrumMessagesHolder.class,FulcrumMessagesHolder.DEFAULT_ARGUMENT_COLOR) :
                    parent.argumentColor();

            String hoverColor = parent.argumentHoverColor() == null ?
                    getFulcrum().getMainConfig().get(FulcrumMessagesHolder.class,FulcrumMessagesHolder.DEFAULT_ARGUMENT_HOVER_COLOR) :
                    parent.argumentHoverColor();


            TextComponent component = new TextComponent(argumentColor + argument.display());
            ComponentBuilder builder = new ComponentBuilder(hoverColor + argument.description());

            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()));
            commandComponent.addExtra(" ");
            commandComponent.addExtra(component);
        }

        String descriptionColor = parent.descriptionColor() == null ?
                getFulcrum().getMainConfig().get(FulcrumMessagesHolder.class,FulcrumMessagesHolder.DEFAULT_DESCRIPTION_COLOR) :
                parent.descriptionColor();


        String description = scheme.description() == null ? "No description provided." : scheme.description();
        commandComponent.addExtra(ChatColor.DARK_GRAY + " - " + descriptionColor + description);

        if (sender instanceof Player player) {

            player.spigot().sendMessage(commandComponent);

        } else {

            sender.sendMessage(commandComponent.toPlainText());
        }
    }
}
