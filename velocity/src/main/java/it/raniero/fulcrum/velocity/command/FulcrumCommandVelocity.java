package it.raniero.fulcrum.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class FulcrumCommandVelocity extends FulcrumCommand {

    public FulcrumCommandVelocity(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {
        CommandSource sender = (CommandSource) source.getSourceObject();
        Component commandComponent = Component.text(MessageUtils.tranlateColors("&c" + "/" + label));
        if (!scheme.checkPermission(source)) {
            return;
        }

        CommandScheme parent = scheme.parent();
        if (parent != null) {
            while (parent != null) {
                if (parent.parent() == null) break; // Reached root command

                commandComponent = commandComponent.append(Component.text(" " + parent.label()));
                parent = parent.parent();
            }

            commandComponent = commandComponent.append(Component.text(" " + scheme.label()));
        }

        for (Argument argument : scheme.arguments().values()) {

            Component argumentComponent = Component.text("<" + argument.name() + ">")
                    .hoverEvent(HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text(MessageUtils.tranlateColors("&c" + argument.description()))));
            commandComponent = commandComponent.append(Component.text(" "), argumentComponent);
        }

        String description = scheme.description() == null ? "No description provided." : scheme.description();
        commandComponent =
                commandComponent.append(Component.text(MessageUtils.tranlateColors("&8 - &7" + description)));

        sender.sendMessage(commandComponent);
    }

    @Override
    public CommandScheme scheme() {
        return null;
    }
}
