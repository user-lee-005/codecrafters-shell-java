package dto;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean isEndOfFile = false;

    public void insert(String word) {
        TrieNode current = this;
        for(char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfFile = true;
    }

    public String getAutoCompletedWord(String prefix) {
        TrieNode current = this;
        for(char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if(current == null) return "";
        }

        StringBuilder fullWord = new StringBuilder(prefix);
        while (current.children.size() == 1 && !current.isEndOfFile) {
            char nextChar = current.children.keySet().iterator().next();
            fullWord.append(nextChar);
            current = current.children.get(nextChar);
        }

        if (current.isEndOfFile && current.children.isEmpty()) {
            return fullWord.append(" ").toString();
        }
        return fullWord.toString();
    }
}
