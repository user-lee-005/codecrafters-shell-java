package terminal.renderer;

import java.util.Optional;
import java.util.Set;

import static constants.Constants.*;

public class CompletionEngine {

    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);

    public Optional<String> complete(String buffer) {
        // Only complete first token
        if(buffer.contains(" ")) {
            return Optional.empty();
        }

        for(String command: BUILTINS) {
            if(command.startsWith(buffer) && !command.equals(buffer)) {
                return Optional.of(command + " ");
            }
        }

        return Optional.empty();
    }
}
