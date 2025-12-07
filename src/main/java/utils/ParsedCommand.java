package utils;

import java.util.List;
import java.util.Objects;

public record ParsedCommand(String command, List<String> args) {
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
