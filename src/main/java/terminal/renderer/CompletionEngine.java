package terminal.renderer;

import utils.DirectoryScanner;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static constants.Constants.*;

public class CompletionEngine {

    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);
    private static final Set<String> EXECUTABLES = DirectoryScanner.findAllExecutablesInPath();

    public Optional<String> complete(String buffer, int tabCount) {
        // Only complete first token
        if(buffer.contains(" ")) {
            return Optional.empty();
        }
        Optional<String> builtInCommand = completeBuiltIns(buffer);
        if (builtInCommand.isPresent()) return builtInCommand;
        return completeExecutables(buffer, tabCount);
    }

    private Optional<String> completeExecutables(String buffer, int tabCount) {
        Set<String> matches = new TreeSet<>();
        for (String command : EXECUTABLES) {
            if (command.startsWith(buffer) && !command.equals(buffer)) {
                matches.add(command);
            }
        }
        if (matches.isEmpty()) return Optional.empty();
        if (matches.size() == 1) {
            return Optional.of(matches.iterator().next() + " ");
        }
        if (tabCount == 2) {
            return Optional.of(String.join("  ", matches));
        }
        return Optional.empty();
    }

    private static Optional<String> completeBuiltIns(String buffer) {
        for(String command: BUILTINS) {
            if(command.startsWith(buffer) && !command.equals(buffer)) {
                return Optional.of(command + " ");
            }
        }
        return Optional.empty();
    }
}
