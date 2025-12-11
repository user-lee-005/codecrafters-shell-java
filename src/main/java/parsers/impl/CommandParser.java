package parsers.impl;

import dto.ParsedCommand;
import parsers.Parser;

import java.util.ArrayList;
import java.util.List;

import static constants.Constants.redirectOperator;
import static constants.Constants.redirectOperator1;

public class CommandParser implements Parser {
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

            if (c == '\\') {
                if (inSingleQuotes) {
                    current.append('\\');
                    continue;
                }

                if (inDoubleQuotes) {
                    if (i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '"' || next == '$' || next == '`' || next == '\\') {
                            current.append(next);
                            i++;
                            continue;
                        }
                    }
                    current.append('\\');
                    continue;
                }

                escaped = true;
                continue;
            }


            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                continue;
            }

            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                continue;
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


    @Override
    public ParsedCommand parseCommand(String command) {
        List<String> tokens = tokenizeShellCommand(command);
        int redirectOutputIndex = -1;
        String redirectFile = null;
        if(tokens.contains(redirectOperator) || tokens.contains(redirectOperator1)) {
            redirectOutputIndex = tokens.indexOf(redirectOperator);
            if(redirectOutputIndex == -1) redirectOutputIndex = tokens.indexOf(redirectOperator1);
            redirectFile = tokens.get(redirectOutputIndex + 1);
        }
        return new ParsedCommand(tokens.getFirst(), tokens.subList(1, redirectOutputIndex == -1 ? tokens.size() : redirectOutputIndex), redirectFile);
    }
}
