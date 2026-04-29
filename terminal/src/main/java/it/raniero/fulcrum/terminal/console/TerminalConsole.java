package it.raniero.fulcrum.terminal.console;

import it.raniero.fulcrum.terminal.command.register.TerminalCommandRegister;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;

public final class TerminalConsole {

    private TerminalConsole() {
        throw new IllegalStateException("Utility class");
    }

    public static ParsedInput parse(String input, Parser parser) {
        ParsedLine parsedLine;
        try {
            parsedLine = parser.parse(input, input.length(), Parser.ParseContext.ACCEPT_LINE);
        } catch (Exception ex) {
            return null;
        }

        List<String> words = parsedLine.words();
        if (words.isEmpty()) {
            return null;
        }

        String label = normalizeLabel(words.get(0));
        if (label.isBlank()) {
            return null;
        }

        String[] args = words.size() > 1 ? words.subList(1, words.size()).toArray(new String[0]) : new String[0];
        return new ParsedInput(label, args);
    }

    public static String[] toCommandArgs(List<String> words) {
        List<String> args = new ArrayList<>();
        if (words.size() > 1) {
            args.addAll(words.subList(1, words.size()));
        }
        return args.toArray(new String[0]);
    }

    public static String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.startsWith("/") ? label.substring(1).toLowerCase() : label.toLowerCase();
    }

    public record ParsedInput(String label, String[] args) {}

    @RequiredArgsConstructor
    public static class CommandCompleter implements Completer {

        private final TerminalCommandRegister register;

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<String> words = line.words();

            if (words.isEmpty()) {
                addRootSuggestions(register.getLabelsAndAliases(), "", false, candidates);
                return;
            }

            String firstWord = words.get(0);
            boolean slash = firstWord.startsWith("/");
            String normalizedLabel = normalizeLabel(firstWord);

            if (line.wordIndex() == 0) {
                String query = normalizeLabel(line.word());
                addRootSuggestions(register.getLabelsAndAliases(), query, slash, candidates);
                return;
            }

            String[] commandArgs = toCommandArgs(words);
            List<String> suggestions = register.suggest(normalizedLabel, commandArgs);
            for (String suggestion : suggestions) {
                candidates.add(new Candidate(suggestion));
            }
        }

        private static void addRootSuggestions(
                Set<String> labels, String query, boolean slash, List<Candidate> candidates) {
            String normalizedQuery = query.toLowerCase();
            for (String label : labels) {
                if (!label.startsWith(normalizedQuery)) {
                    continue;
                }
                candidates.add(new Candidate(slash ? "/" + label : label));
            }
        }
    }
}
