import handlers.CommandRegistry;
import parsers.Parser;
import parsers.impl.CommandParser;
import dto.ParsedCommand;
import terminal.KeyEvent;
import terminal.KeyType;
import terminal.controller.InputController;
import terminal.factory.TerminalFactory;
import terminal.buffer.InputBuffer;
import terminal.readers.TerminalReader;
import terminal.renderer.Renderer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        TerminalReader terminal = TerminalFactory.createReader();
        InputBuffer buffer = new InputBuffer();
        Renderer renderer = TerminalFactory.createRenderer();
        InputController controller = new InputController(buffer, renderer);
        terminal.enableRawMode();
        try {
            while (true) {
                System.out.print("$ ");
                System.out.flush();

                buffer.clear();
                controller.setHistoryIndex(CommandRegistry.getHistory().size());

                while (true) {
                    KeyEvent event = terminal.readKey();
                    controller.handle(event);
                    if(KeyType.ENTER.equals(event.keyType())) {
                        break;
                    }
                    if(KeyType.EOF.equals(event.keyType())) {
                        return;
                    }
                }
                String input = buffer.content().trim();
                if (input.isEmpty()) continue;
                if (executeAndExit(input)) break;
            }
        } finally {
            terminal.disableRawMode();
        }
    }

    private static boolean executeAndExit(String input) throws IOException {
        CommandRegistry.addToHistory(input);
        List<ParsedCommand> parsedCommands = getParsedCommands(input);
        if(parsedCommands.getFirst().isExit()) return true;
        PrintStream printStream = parsedCommands.getFirst().outputStrategy().get();
        try {
            CommandRegistry.run(parsedCommands, printStream);
        } finally {
            if (printStream != System.out) {
                printStream.close();
            }
        }
        return false;
    }

    private static List<ParsedCommand> getParsedCommands(String input) {
        Parser commandParser = new CommandParser();
        List<ParsedCommand> parsedCommands = new ArrayList<>();
        if(input.contains("|")) {
            String[] commands = input.split("\\|");
            for(String command: commands) {
                ParsedCommand parsedCommand = commandParser.parseCommand(command);
                parsedCommands.add(parsedCommand);
            }
        } else {
            ParsedCommand parsedCommand = commandParser.parseCommand(input);
            parsedCommands.add(parsedCommand);
        }
        return parsedCommands;
    }
}
