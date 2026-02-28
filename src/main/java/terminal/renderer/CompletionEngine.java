package terminal.renderer;

import dto.TrieNode;
import utils.DirectoryScanner;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static constants.Constants.*;

public class CompletionEngine {

    private static final Set<String> BUILTINS = Set.of(echo, exit, type, pwd, cd);
    private static final Set<String> EXECUTABLES = DirectoryScanner.findAllExecutablesInPath();

    public Optional<String> complete(String buffer, int tabCount) {
        String[] tokens = buffer.split(" ");
        boolean completeCommand = tokens.length == 1 && !buffer.endsWith(" ");
        if(completeCommand) {
            Optional<String> builtInCommand = completeBuiltIns(buffer);
            if (builtInCommand.isPresent()) return builtInCommand;
            return completeExecutables(buffer, tabCount);
        }
        return completeFileNames(tokens);
    }

    private Optional<String> completeFileNames(String[] tokens) {
        if (tokens == null || tokens.length != 2) {
            return Optional.empty();
        }
        String filePrefix = tokens[1];
        TrieNode filesPresentInCwd = DirectoryScanner.findAllFilesInTheCurrentDirectory();
        String fileName = filesPresentInCwd.getAutoCompletedWord(filePrefix);
        return fileName.isEmpty() ? Optional.empty() : Optional.of(fileName);
    }

    private Optional<String> completeExecutables(String buffer, int tabCount) {
        Set<String> matches = new TreeSet<>();
        for (String command : EXECUTABLES) {
            if (command.startsWith(buffer) && !command.equals(buffer)) {
                matches.add(command);
            }
        }
        if (matches.isEmpty()) return Optional.empty();
        if (matches.size() == 1) {
            return Optional.of(matches.iterator().next() + " ");
        }
        if (tabCount == 2) {
            return Optional.of(String.join("  ", matches));
        } else {
            Optional<String> longestCommonPrefix = getLongestCommonPrefix(buffer, matches);
            if (longestCommonPrefix.isPresent()) return longestCommonPrefix;
        }
        return Optional.empty();
    }

    private static Optional<String> getLongestCommonPrefix(String buffer, Set<String> matches) {
        Iterator<String> stringIterator = matches.iterator();
        String longestCommonPrefix = stringIterator.next();
        while (stringIterator.hasNext()) {
            String next = stringIterator.next();
            while (next.indexOf(longestCommonPrefix) != 0) {
                longestCommonPrefix = longestCommonPrefix.substring(0, longestCommonPrefix.length() - 1);
            }
        }
        return (longestCommonPrefix.isEmpty() || longestCommonPrefix.equals(buffer))
                ? Optional.empty()
                : Optional.of(longestCommonPrefix);
    }

    private static Optional<String> completeBuiltIns(String buffer) {
        for(String command: BUILTINS) {
            if(command.startsWith(buffer) && !command.equals(buffer)) {
                return Optional.of(command + " ");
            }
        }
        return Optional.empty();
    }
}
