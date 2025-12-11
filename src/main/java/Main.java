import handlers.CommandRegistry;
import parsers.Parser;
import parsers.impl.CommandParser;
import utils.DirectoryScanner;
import dto.ParsedCommand;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import static constants.Constants.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;
            if (executeAndExit(input)) break;
        }
    }

    private static boolean executeAndExit(String input) throws IOException {
        Parser commandParser = new CommandParser();
        ParsedCommand parsedCommand = commandParser.parseCommand(input);
        if(parsedCommand.isExit()) return true;
        PrintStream printStream = parsedCommand.outputStrategy().get();
        try {
            CommandRegistry.run(parsedCommand, printStream);
        } finally {
            if (printStream != System.out) {
                printStream.close();
            }
        }
        return false;
    }
}
