package terminal.renderer;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static constants.Constants.*;

public class CompletionEngine {

    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);
    private static final String PATH = System.getenv("PATH");

    public Optional<String> complete(String buffer) {
        // Only complete first token
        if(buffer.contains(" ")) {
            return Optional.empty();
        }

        Optional<String> builtInCommand = completeBuiltIns(buffer);
        if (builtInCommand.isPresent()) return builtInCommand;

        return completePath(buffer);

    }

    private Optional<String> completePath(String buffer) {
        String separator = File.pathSeparator;
        String[] dirs = PATH.split(Pattern.quote(separator));
        System.out.println("--------------------------------");
        System.out.println(Arrays.toString(dirs));
        System.out.println("--------------------------------");
        for(String command: dirs) {
            System.out.println("-------------- Command in path ---------------");
            System.out.println(command);
            System.out.println("--------------------------------");
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
