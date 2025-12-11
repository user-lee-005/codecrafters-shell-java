package handlers;

import dto.ParsedCommand;

import java.io.PrintStream;

public interface CommandHandler {
    void handle(ParsedCommand parsedCommand, PrintStream printStream);
}
