package terminal.renderer;

import utils.DirectoryScanner;

import java.util.Optional;
import java.util.Set;

import static constants.Constants.*;

public class CompletionEngine {

    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);
    private static final Set<String> EXECUTABLES = DirectoryScanner.findAllExecutablesInPath();

    public Optional<String> complete(String buffer) {
        // Only complete first token
        if(buffer.contains(" ")) {
            return Optional.empty();
        }

        Optional<String> builtInCommand = completeBuiltIns(buffer);
        if (builtInCommand.isPresent()) return builtInCommand;

        return completeExecutables(buffer);

    }

    private Optional<String> completeExecutables(String buffer) {
        for(String command: EXECUTABLES) {
            if(command.startsWith(buffer) && !command.equals(buffer)) {
                return Optional.of(command + " ");
            }
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
