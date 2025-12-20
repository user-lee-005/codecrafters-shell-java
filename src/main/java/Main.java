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
