package it.raniero.fulcrum.spigot.command;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.utils.MessageUtils;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.spigot.command.executor.FulcrumCommandExecutor;
import it.raniero.fulcrum.spigot.utils.SpigotUtils;
import java.util.List;
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
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        CommandSender sender = (CommandSender) source.getSourceObject();
        if (!scheme.checkPermission(source)) {
            return;
        }

        List<CommandScheme> schemes = getSchemeParents(scheme);
        CommandScheme parent = !schemes.isEmpty() ? schemes.get(0) : scheme;
        String labelColor = parent.labelColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_LABEL_COLOR)
                : parent.labelColor();

        TextComponent commandComponent = new TextComponent(labelColor + "/" + label);
        schemes.remove(0);
        addSubCommandComponents(commandComponent, schemes);
        addArgumentComponents(commandComponent, scheme, parent);
        addDescriptionComponent(commandComponent, scheme, parent);

        if (sender instanceof Player player) {

            player.spigot().sendMessage(SpigotUtils.translateComponent(commandComponent));

        } else {

            sender.sendMessage(MessageUtils.translateColors(commandComponent.toPlainText()));
        }
    }

    private void addSubCommandComponents(TextComponent component, List<CommandScheme> schemes) {
        for (CommandScheme subScheme : schemes) {

            String subCommandColor = subScheme.labelColor() == null
                    ? getFulcrum()
                            .getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_SUBLABEL_COLOR)
                    : subScheme.labelColor();

            TextComponent subComponent = new TextComponent(subCommandColor + " " + subScheme.label());

            String description = subScheme.description() == null ? "N/D" : subScheme.description();
            ComponentBuilder builder = new ComponentBuilder(subCommandColor + description);
            subComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()));

            component.addExtra(subComponent);
        }
    }

    private void addDescriptionComponent(TextComponent component, CommandScheme scheme, CommandScheme parent) {
        String descriptionColor = parent.descriptionColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_DESCRIPTION_COLOR)
                : parent.descriptionColor();

        String description = scheme.description() == null ? "N/D" : scheme.description();
        component.addExtra(ChatColor.DARK_GRAY + " - " + descriptionColor + description);
    }

    private void addArgumentComponents(TextComponent component, CommandScheme scheme, CommandScheme parent) {
        String argumentColor = parent.argumentColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_COLOR)
                : parent.argumentColor();

        component.addExtra(argumentColor);
        for (Argument argument : scheme.arguments().values()) {

            String hoverColor = parent.argumentHoverColor() == null
                    ? getFulcrum()
                            .getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_HOVER_COLOR)
                    : parent.argumentHoverColor();

            TextComponent argumentComponent = new TextComponent(argument.display());
            String description = argument.description() == null ? "N/D" : argument.description();

            ComponentBuilder builder = new ComponentBuilder(hoverColor + description);

            argumentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()));
            component.addExtra(" ");
            component.addExtra(argumentComponent);
        }
    }
}
