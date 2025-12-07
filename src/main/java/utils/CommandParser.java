package utils;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    private static List<String> tokenizeShellCommand(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '\'' && !inSingleQuotes) {
                inSingleQuotes = true;
            } else if (c == '\'') {
                inSingleQuotes = false;
            } else if (Character.isWhitespace(c) && !inSingleQuotes) {
                if (!current.isEmpty()) {
                    result.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (escaped) current.append('\\');

        if (!current.isEmpty()) {
            result.add(current.toString());
        }

        return result;
    }

    public static ParsedCommand parseCommand(String command) {
        List<String> tokens = tokenizeShellCommand(command);
        return new ParsedCommand(tokens.getFirst(), tokens.subList(1, tokens.size()));
    }
}
