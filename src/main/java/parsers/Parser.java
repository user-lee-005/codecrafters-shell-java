package parsers;

import dto.ParsedCommand;

public interface Parser {
    ParsedCommand parseCommand(String command);
}
