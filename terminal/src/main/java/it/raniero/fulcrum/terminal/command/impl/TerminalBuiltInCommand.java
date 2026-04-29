package it.raniero.fulcrum.terminal.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.terminal.FulcrumTerminal;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jline.utils.InfoCmp;

public class TerminalBuiltInCommand extends FulcrumCommand {

    private final AtomicBoolean running;
    private final CommandScheme scheme;

    private TerminalBuiltInCommand(Fulcrum fulcrum, AtomicBoolean running, CommandScheme scheme) {
        super(fulcrum, false);
        this.running = running;
        this.scheme = scheme;
        registerScheme(scheme);
    }

    public static TerminalBuiltInCommand clear(Fulcrum fulcrum) {
        return new TerminalBuiltInCommand(
                fulcrum,
                null,
                CommandScheme.builder()
                        .label("clear")
                        .description("Clears the terminal")
                        .source(SourceType.CONSOLE)
                        .commandExecutor(ctx -> {
                            FulcrumTerminal ft = (FulcrumTerminal) fulcrum.getPlugin();
                            if (ft.getCurrentTerminal() != null) {
                                ft.getCurrentTerminal().puts(InfoCmp.Capability.clear_screen);
                                ft.getCurrentTerminal().flush();
                            }
                        })
                        .build());
    }

    public static TerminalBuiltInCommand exit(Fulcrum fulcrum, AtomicBoolean running) {
        return new TerminalBuiltInCommand(
                fulcrum,
                running,
                CommandScheme.builder()
                        .label("exit")
                        .description("Close the terminal application")
                        .source(SourceType.CONSOLE)
                        .commandExecutor(ctx -> {
                            ctx.source().sendMessage("&aShutting down...");
                            running.set(false);
                        })
                        .build());
    }

    public static TerminalBuiltInCommand echo(Fulcrum fulcrum) {
        return new TerminalBuiltInCommand(
                fulcrum,
                null,
                CommandScheme.builder()
                        .label("echo")
                        .description("Close the terminal application")
                        .source(SourceType.CONSOLE)
                        .argument(NormalArgument.builder()
                                .name("message")
                                .type(String[].class)
                                .description("The message you want to echo")
                                .required(true)
                                .build())
                        .commandExecutor(ctx -> {
                            String message = ctx.argument(0, String[].class)
                                    .map(arr -> String.join(" ", arr))
                                    .orElse(null);
                            if (message == null) {
                                ctx.result(ContextResult.INVALID_ARGUMENTS);
                                return;
                            }

                            ctx.source().sendMessage(message);
                        })
                        .build());
    }

    @Override
    public String plugin() {
        return "fulcrum";
    }

    @Override
    public CommandScheme scheme() {
        return scheme;
    }
}
