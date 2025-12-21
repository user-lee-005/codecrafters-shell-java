package handlers;

import dto.ParsedCommand;
import utils.DirectoryScanner;

import java.io.*;
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

    public static void runExternalCommand(List<ParsedCommand> parsedCommands, PrintStream printStream) {
        try {
            List<ProcessBuilder> builders = createProcessBuilders(parsedCommands);
            executePipeline(builders, printStream);
        } catch (IllegalArgumentException e) {
            printStream.println(e.getMessage());
        } catch (Exception e) {
            printStream.println("Execution Error: " + e.getMessage());
        }
    }

    private static List<ProcessBuilder> createProcessBuilders(List<ParsedCommand> parsedCommands) {
        List<ProcessBuilder> builders = new ArrayList<>();
        String path = System.getenv("PATH");
        for (ParsedCommand parsedCommand : parsedCommands) {
            String command = parsedCommand.command();
            String found = DirectoryScanner.findExecutable(command, path);
            if (found == null) {
                throw new IllegalArgumentException(command + ": command not found");
            }

            String execName = Paths.get(found).getFileName().toString();
            List<String> cmdArgs = new ArrayList<>();
            cmdArgs.add(execName);
            cmdArgs.addAll(parsedCommand.args());

            ProcessBuilder pb = new ProcessBuilder(cmdArgs);
            pb.directory(new File(found).getParentFile());

            if (parsedCommand.stdErrRedirectFile() != null) {
                pb.redirectError(parsedCommand.append() ?
                        ProcessBuilder.Redirect.appendTo(new File(parsedCommand.stdErrRedirectFile())) :
                        ProcessBuilder.Redirect.to(new File(parsedCommand.stdErrRedirectFile()))
                );
            } else {
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            }

            builders.add(pb);
        }
        return builders;
    }

    private static void executePipeline(List<ProcessBuilder> builders, PrintStream printStream) throws IOException, InterruptedException {
        if (builders.isEmpty()) return;

        // 1. Configure the "Input" of the Pipeline (First Process)
        // The first command reads from the User's Keyboard (INHERIT)
        builders.getFirst().redirectInput(ProcessBuilder.Redirect.INHERIT);

        // 2. Configure the "Output" of the Pipeline (Last Process)
        // The last command MUST pipe its output to Java so we can read it and print it
        builders.getLast().redirectOutput(ProcessBuilder.Redirect.PIPE);

        // 3. Start the Pipeline
        // Java automatically connects the pipes between intermediate processes (0->1, 1->2)
        List<Process> processes = ProcessBuilder.startPipeline(builders);

        // 4. Read Output from the LAST process only
        Process lastProcess = processes.getLast();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(lastProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                printStream.println(line);
            }
        }

        // 5. Clean up: Wait for all processes to finish
        for (Process p : processes) {
            p.waitFor();
        }
    }
    public static void run(List<ParsedCommand> parsedCommands, PrintStream printStream) {
        CommandHandler handler = handlers.get(parsedCommands.getFirst().command());
        if(handler != null) {
            if (parsedCommands.getFirst().stdErrRedirectFile() != null) {
                try {
                    new FileOutputStream(parsedCommands.getFirst().stdErrRedirectFile(),
                            parsedCommands.getFirst().append()).close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            handler.handle(parsedCommands.getFirst(), printStream);
            return;
        }
        runExternalCommand(parsedCommands, printStream);
    }
}
