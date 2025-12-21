package parsers.impl;

import dto.ParsedCommand;
import parsers.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static constants.Constants.*;

public class CommandParser implements Parser {
    private final Set<String> redirectOperators = Set.of(redirectOperator1, redirectOperator2, redirectOperator,
            redirectAndAppendOperator, redirectAndAppendOperator1, redirectAndAppendOperator2);
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
        String stdErrRedirectFile = null;
        String stdOutRedirectFile = null;
        boolean redirectError = false;
        boolean append = false;
        for(int i = 0; i < tokens.size(); i ++) {
            if(redirectOperators.contains(tokens.get(i))) {
                if (tokens.get(i).equals(redirectAndAppendOperator)
                        || tokens.get(i).matches(redirectAndAppendOperator1)
                        || tokens.get(i).matches(redirectAndAppendOperator2)) {
                    append = true;
                }
                if (redirectOperator2.equals(tokens.get(i))
                        || tokens.get(i).matches(redirectAndAppendOperator2)) {
                    redirectError = true;
                    stdErrRedirectFile = tokens.get(i + 1);
                } else  {
                    stdOutRedirectFile = tokens.get(i + 1);
                }
                redirectOutputIndex = i;
                break;
            }
        }
        return new ParsedCommand(tokens.getFirst(),
                tokens.subList(1, redirectOutputIndex == -1 ? tokens.size() : redirectOutputIndex),
                stdOutRedirectFile, stdErrRedirectFile, redirectError, append);
    }
}
