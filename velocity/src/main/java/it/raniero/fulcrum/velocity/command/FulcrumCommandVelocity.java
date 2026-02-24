package it.raniero.fulcrum.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.velocity.utils.VelocityUtils;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class FulcrumCommandVelocity extends FulcrumCommand {

    public FulcrumCommandVelocity(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme) {

        CommandSource sender = (CommandSource) source.getSourceObject();
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

        Component commandComponent = VelocityUtils.convertLegacyText(labelColor + "/" + label);
        schemes.remove(0);
        commandComponent = addSubCommandComponents(commandComponent, schemes);
        commandComponent = addArgumentComponents(commandComponent, scheme, parent);
        commandComponent = addDescriptionComponent(commandComponent, scheme, parent);

        sender.sendMessage(commandComponent);
    }

    private Component addSubCommandComponents(Component component, List<CommandScheme> schemes) {
        for (CommandScheme subScheme : schemes) {

            String subCommandColor = subScheme.labelColor() == null
                    ? getFulcrum()
                            .getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_SUBLABEL_COLOR)
                    : subScheme.labelColor();

            String description = subScheme.description() == null ? "N/D" : subScheme.description();

            Component subComponent = VelocityUtils.convertLegacyText(subCommandColor + " " + subScheme.label())
                    .hoverEvent(HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            VelocityUtils.convertLegacyText(subCommandColor + description)));

            component = component.append(subComponent);
        }

        return component;
    }

    private Component addArgumentComponents(Component component, CommandScheme scheme, CommandScheme parent) {
        String argumentColor = parent.argumentColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_COLOR)
                : parent.argumentColor();
        for (Argument argument : scheme.arguments().values()) {

            String hoverColor = parent.argumentHoverColor() == null
                    ? getFulcrum()
                            .getMainConfig()
                            .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_ARGUMENT_HOVER_COLOR)
                    : parent.argumentHoverColor();

            String description = argument.description() == null ? "N/D" : argument.description();

            Component argumentComponent = VelocityUtils.convertLegacyText(argumentColor + " " + argument.display())
                    .hoverEvent(HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT, VelocityUtils.convertLegacyText(hoverColor + description)));

            component = component.append(argumentComponent);
        }

        return component;
    }

    private Component addDescriptionComponent(Component component, CommandScheme scheme, CommandScheme parent) {
        String descriptionColor = parent.descriptionColor() == null
                ? getFulcrum()
                        .getMainConfig()
                        .get(FulcrumMessagesHolder.class, FulcrumMessagesHolder.DEFAULT_DESCRIPTION_COLOR)
                : parent.descriptionColor();

        String description = scheme.description() == null ? "N/D" : scheme.description();

        return component.append(VelocityUtils.convertLegacyText("&8 - " + descriptionColor + description));
    }
}
