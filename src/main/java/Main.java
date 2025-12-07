import utils.DirectoryScanner;
import utils.ParsedCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;

import static utils.CommandParser.parseCommand;
import static utils.Constants.*;

public class Main {
    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;
            if (execute(input)) break;
        }
    }

    private static boolean execute(String input) {
        ParsedCommand parsedCommand = parseCommand(input);
        String cmd = parsedCommand.command();
        List<String> args = parsedCommand.args();
        return switch (cmd) {
            case exit -> true;
            case echo -> {
                System.out.println(String.join(" ", args));
                yield false;
            }
            case type -> {
                handleType(args.getFirst());
                yield false;
            }
            case pwd -> {
                System.out.println(DirectoryScanner.getWorkingDirectory());
                yield false;
            }
            case cd -> {
                DirectoryScanner.changeDirectory(args);
                yield false;
            }
            default -> {
                runExternalCommand(cmd, args);
                yield false;
            }
        };
    }

    private static void handleType(String arg) {
        if (arg.isEmpty()) {
            System.out.println("type: missing operand");
            return;
        }

        // builtin?
        if (BUILTINS.contains(arg)) {
            System.out.println(arg + " is a shell builtin");
            return;
        }

        // PATH search
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(arg, path);

        if (found != null) {
            System.out.println(arg + " is " + found);
        } else {
            System.out.println(arg + ": not found");
        }
    }

    public static void runExternalCommand(String command, List<String> args) {
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(command, path);

        if (found == null) {
            System.out.println(command + ": command not found");
            return;
        }

        try {
            runExternal(found, args);
        } catch (Exception e) {
            System.out.println(command + ": " + e.getMessage());
        }
    }

    private static void runExternal(String execPath, List<String> args) throws Exception {
        String execName = Paths.get(execPath).getFileName().toString();
        List<String> command = new ArrayList<>();
        command.add(execName);
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(execPath).getParentFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}
