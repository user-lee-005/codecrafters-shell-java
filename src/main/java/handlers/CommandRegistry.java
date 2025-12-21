package handlers;

import dto.ParsedCommand;
import utils.DirectoryScanner;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static constants.Constants.*;

public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = Map.of(
            echo, (cmd, _, out) -> out.println(String.join(" ", cmd.args())),
            pwd, (_, _, out) -> out.println(DirectoryScanner.getWorkingDirectory()),
            cd, (cmd, _, _) -> DirectoryScanner.changeDirectory(cmd.args()),
            type, (cmd, _, out) -> handleType(cmd.args().isEmpty() ? "" : cmd.args().getFirst(), out),
            exit, (_, _, _) -> System.exit(0),
            history, (_, _, out) -> handleHistory(out)
    );

    private static final List<String> historyState = new ArrayList<>();

    public static void addToHistory(String input) {
        historyState.add(input);
    }

    public static void run(List<ParsedCommand> commands, PrintStream finalOutput) {
        InputStream previousOutput = System.in;
        List<Process> runningProcesses = new ArrayList<>();
        List<Thread> activeThreads = new ArrayList<>();

        try {
            for (int i = 0; i < commands.size(); i++) {
                ParsedCommand cmd = commands.get(i);
                boolean isLast = (i == commands.size() - 1);
                OutputStream currentOutput;
                InputStream nextInput = null;
                if (isLast) {
                    currentOutput = finalOutput;
                } else {
                    PipedInputStream snk = new PipedInputStream();
                    nextInput = snk;
                    currentOutput = new PipedOutputStream(snk);
                }

                // --- EXECUTION ---
                if (handlers.containsKey(cmd.command())) {
                    if (cmd.stdErrRedirectFile() != null) {
                        try {
                            new FileOutputStream(cmd.stdErrRedirectFile(), cmd.append()).close();
                        } catch (IOException e) {
                            finalOutput.println("Error creating redirection file: " + e.getMessage());
                        }
                    }
                    handleBuiltin(cmd, previousOutput, currentOutput, isLast, activeThreads);
                } else {
                    ProcessBuilder pb = createBuilder(cmd);
                    Process p = pb.start();
                    runningProcesses.add(p);

                    // 1. Handle Input (Feed previous command's output to this process)
                    if (previousOutput != System.in) {
                        // Threaded bridge for input to avoid deadlock
                        bridgeStreamsThreaded(previousOutput, p.getOutputStream(), activeThreads);
                    }

                    // 2. Handle Output
                    if (isLast) {
                        // CRITICAL FIX: Block the Main Thread here!
                        // We do NOT spawn a thread for the final output.
                        // We read it right here, ensuring we don't return until it's done.
                        copyStream(p.getInputStream(), currentOutput);
                    } else {
                        // Intermediate command: Use a thread so we can move to the next loop iteration
                        bridgeStreamsThreaded(p.getInputStream(), currentOutput, activeThreads);
                    }
                }
                if (nextInput != null) {
                    previousOutput = nextInput;
                }
            }
            for (Process p : runningProcesses) p.waitFor();
            for (Thread t : activeThreads) t.join();
        } catch (Exception e) {
            finalOutput.println(e.getMessage());
        }
    }

    private static void handleBuiltin(ParsedCommand cmd, InputStream in, OutputStream out, boolean isLast, List<Thread> threads) {
        CommandHandler handler = handlers.get(cmd.command());

        if (isLast) {
            handler.handle(cmd, in, new PrintStream(out));
        } else {
            Thread t = new Thread(() -> {
                try (PrintStream ps = new PrintStream(out)) {
                    handler.handle(cmd, in, ps);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
            t.start();
            threads.add(t);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        try (in) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                out.flush();
            }
        }
    }

    private static void bridgeStreamsThreaded(InputStream in, OutputStream out, List<Thread> threads) {
        Thread t = new Thread(() -> {
            try (in; out) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                    out.flush();
                }
            } catch (IOException e) {
                // Ignore broken pipes (common in pipelines)
            }
        });
        t.start();
        threads.add(t);
    }

    private static ProcessBuilder createBuilder(ParsedCommand parsedCommand) {
        String path = System.getenv("PATH");
        String found = DirectoryScanner.findExecutable(parsedCommand.command(), path);
        if (found == null) throw new IllegalArgumentException(parsedCommand.command() + ": command not found");

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

        return pb;
    }

    private static void handleType(String arg, PrintStream printStream) {
        if (arg.isEmpty()) { printStream.println("missing operand"); return; }
        if (handlers.containsKey(arg)) { printStream.println(arg + " is a shell builtin"); return; }
        String found = DirectoryScanner.findExecutable(arg, System.getenv("PATH"));
        if (found != null) printStream.println(arg + " is " + found);
        else printStream.println(arg + ": not found");
    }

    private static void handleHistory(PrintStream printStream) {
        if(historyState.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String cmd : historyState) {
            sb.append(String.format("%5d  %s%n", index++, cmd));
        }
        printStream.print(sb);
    }
}