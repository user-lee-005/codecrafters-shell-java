import utils.DirectoryScanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final Set<String> BUILTINS = Set.of("echo", "exit", "type");

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
        String[] parts = input.split("\\s+", 2);
        String cmd = parts[0];
        String arg = parts.length > 1 ? parts[1].trim() : "";

        return switch (cmd) {
            case "exit" -> true;
            case "echo" -> {
                System.out.println(arg);
                yield false;
            }
            case "type" -> {
                handleType(arg);
                yield false;
            }
            default -> {
                runExternalCommand(cmd, arg);
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

    public static void runExternalCommand(String command, String arg) {
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(command, path);
        if (found == null) {
            System.out.println(command + ": command not found");
            return;
        }

        String[] args = arg == null || arg.isBlank()
                ? new String[0]
                : arg.split("\\s+");
        try {
            runExternal(found, args);
        } catch (Exception e) {
            System.out.println(command + ": " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private static void runExternal(String execPath, String... args) throws Exception {
        String execName = Paths.get(execPath).getFileName().toString();
        List<String> command = new ArrayList<>();
        command.add(execName);
        command.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(execPath).getParentFile());
        pb.redirectErrorStream(true);    // merge stderr into stdout

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
    }

}
