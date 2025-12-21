package handlers;

import dto.ParsedCommand;

import java.io.InputStream;
import java.io.PrintStream;

@FunctionalInterface
interface CommandHandler {
    void handle(ParsedCommand command, InputStream input, PrintStream output);
}