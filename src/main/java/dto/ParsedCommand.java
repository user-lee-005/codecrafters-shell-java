package dto;

import handlers.ConsoleOutput;
import handlers.FileOutput;
import handlers.OutputProvider;

import java.util.List;
import java.util.Objects;

import static constants.Constants.exit;

public record ParsedCommand(String command, List<String> args, String redirectFile) {

    public boolean isExit() {
        return exit.equals(command);
    }

    public OutputProvider outputStrategy() {
        if(redirectFile == null) {
            return new ConsoleOutput();
        }
        return new FileOutput(redirectFile);
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
