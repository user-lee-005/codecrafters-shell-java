package handlers;

import dto.ParsedCommand;
import utils.DirectoryScanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static constants.Constants.*;

public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = Map.of(
        echo, (parsedCommand, printStream) -> printStream.println(String.join(" ", parsedCommand.args())),
        pwd, (_, printStream) -> printStream.println(DirectoryScanner.getWorkingDirectory()),
        type, (parsedCommand, printStream) -> handleType(parsedCommand.args().getFirst(), printStream),
        cd, (parsedCommand, _) -> DirectoryScanner.changeDirectory(parsedCommand.args())
    );

    private static void handleType(String arg, PrintStream printStream) {
        if (arg.isEmpty()) {
            printStream.println("type: missing operand");
            return;
        }

        // built-in
        if (handlers.containsKey(arg) || exit.equals(arg)) {
            printStream.println(arg + " is a shell builtin");
            return;
        }

        // PATH search
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(arg, path);

        if (found != null) {
            printStream.println(arg + " is " + found);
        } else {
            printStream.println(arg + ": not found");
        }
    }

    public static void runExternalCommand(String command, List<String> args, PrintStream printStream) {
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(command, path);

        if (found == null) {
            printStream.println(command + ": command not found");
            return;
        }

        try {
            runExternal(found, args, printStream);
        } catch (Exception e) {
            printStream.println(command + ": " + e.getMessage());
        }
    }

    private static void runExternal(String execPath, List<String> args, PrintStream printStream) throws Exception {
        String execName = Paths.get(execPath).getFileName().toString();
        List<String> command = new ArrayList<>();
        command.add(execName);
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(execPath).getParentFile());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                printStream.println(line);
            }
        }
    }

    public static void run(ParsedCommand parsedCommand, PrintStream printStream) {
        CommandHandler handler = handlers.get(parsedCommand.command());
        if(handler != null) {
            handler.handle(parsedCommand, printStream);
        } else {
            runExternalCommand(parsedCommand.command(), parsedCommand.args(), printStream);
        }
    }
}
