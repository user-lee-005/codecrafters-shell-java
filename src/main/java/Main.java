import utils.DirectoryScanner;

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
                System.out.println(cmd + ": command not found");
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
}
