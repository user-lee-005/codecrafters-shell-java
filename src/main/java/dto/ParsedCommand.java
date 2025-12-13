package dto;

import handlers.CommandRegistry;
import handlers.ConsoleOutput;
import handlers.FileOutput;
import handlers.OutputProvider;

import java.util.List;
import java.util.Objects;

import static constants.Constants.*;

public record ParsedCommand(String command,
                            List<String> args,
                            String stdOutRedirectFile,
                            String stdErrRedirectFile,
                            boolean redirectError,
                            boolean append) {

    public boolean isExit() {
        return exit.equals(command);
    }

    /**
     * echo is a builtin
     * Your builtin echo writes to stdout, not stderr
     * return Console as PrintStream if the command is built-in
     * */
    public OutputProvider outputStrategy() {
        if (stdOutRedirectFile != null) {
            return new FileOutput(stdOutRedirectFile, append);
        }
        return new ConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedCommand that = (ParsedCommand) o;
        return Objects.equals(command(), that.command()) && Objects.equals(args(), that.args());
    }

    @Override
    public int hashCode() {
        return Objects.hash(command(), args());
    }
}
