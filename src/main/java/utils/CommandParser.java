package utils;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    private static List<String> tokenizeShellCommand(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\' && !inSingleQuotes) {
                escaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                continue;
            }

            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                continue; // don't append quote char
            }

            if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                if (!current.isEmpty()) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (escaped) current.append('\\');
        if (!current.isEmpty()) result.add(current.toString());

        return result;
    }


    public static ParsedCommand parseCommand(String command) {
        List<String> tokens = tokenizeShellCommand(command);
        return new ParsedCommand(tokens.getFirst(), tokens.subList(1, tokens.size()));
    }
}
