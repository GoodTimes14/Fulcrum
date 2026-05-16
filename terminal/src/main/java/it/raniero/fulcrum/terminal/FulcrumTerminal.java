package it.raniero.fulcrum.terminal;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.FulcrumAPI;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.command.common.FulcrumMainCommand;
import it.raniero.fulcrum.terminal.command.impl.TerminalBuiltInCommand;
import it.raniero.fulcrum.terminal.command.register.TerminalCommandRegister;
import it.raniero.fulcrum.terminal.console.TerminalConsole;
import it.raniero.fulcrum.terminal.server.FulcrumTerminalServer;
import it.raniero.fulcrum.terminal.server.sender.TerminalSource;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

@Getter
public class FulcrumTerminal implements FulcrumPlugin {

    private final Fulcrum fulcrum;
    private final FulcrumServer fulcrumServer;
    private final TerminalCommandRegister commandRegister;
    private final TerminalSource terminalSource;
    private final File dataFolder;
    private final Logger logger;
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Setter
    private Terminal currentTerminal;

    public FulcrumTerminal() {
        this(Paths.get("terminal-data").toFile(), Logger.getLogger("FulcrumTerminal"), System.out);
    }

    public FulcrumTerminal(File dataFolder, Logger logger, PrintStream outputStream) {
        this.fulcrum = new Fulcrum();
        this.fulcrumServer = new FulcrumTerminalServer();
        this.terminalSource = new TerminalSource(outputStream);
        this.commandRegister = new TerminalCommandRegister(fulcrum, fulcrumServer, terminalSource);
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void start() throws IOException {
        ensureDataFolderExists();

        fulcrum.start(this);
        fulcrum.getCommandManager().registerCommand(new FulcrumMainCommand(fulcrum));
        fulcrum.getCommandManager().registerCommand(TerminalBuiltInCommand.exit(fulcrum, running));
        fulcrum.getCommandManager().registerCommand(TerminalBuiltInCommand.clear(fulcrum));
        fulcrum.getCommandManager().registerCommand(TerminalBuiltInCommand.echo(fulcrum));

        DefaultParser parser = new DefaultParser();

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            setCurrentTerminal(terminal);

            Completer completer = new TerminalConsole.CommandCompleter(commandRegister);
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(parser)
                    .completer(completer)
                    .build();

            while (running.get()) {
                String input;
                try {
                    input = lineReader.readLine("> ");
                } catch (UserInterruptException e) {
                    continue;
                } catch (EndOfFileException e) {
                    break;
                }

                if (input == null) {
                    break;
                }

                String trimmed = input.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                TerminalConsole.ParsedInput parsedInput = TerminalConsole.parse(trimmed, parser);
                if (parsedInput == null) {
                    terminalSource.sendMessage("&cInvalid command syntax.");
                    continue;
                }

                commandRegister.executeCommand(parsedInput.label(), parsedInput.args());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            fulcrum.stop();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during shutdown", ex);
        }
    }

    @Override
    public FulcrumAPI getFulcrum() {
        return fulcrum;
    }

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOError(new IOException("Unable to create terminal data folder"));
        }
    }
}
