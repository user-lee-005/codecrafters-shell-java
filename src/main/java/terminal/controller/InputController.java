package terminal.controller;

import handlers.CommandRegistry;
import terminal.KeyEvent;
import terminal.KeyType;
import terminal.buffer.InputBuffer;
import terminal.renderer.CompletionEngine;
import terminal.renderer.Renderer;

import java.util.List;
import java.util.Optional;

public class InputController {

    private final InputBuffer buffer;
    private final Renderer renderer;
    private final CompletionEngine completionEngine = new CompletionEngine();
    private int tabCount = 0;
    private int historyIndex = 0;

    public InputController(InputBuffer buffer, Renderer renderer) {
        this.buffer = buffer;
        this.renderer = renderer;
    }

    public void setHistoryIndex(int index) {
        historyIndex = index;
    }

    public void handle(KeyEvent event) {
        if(KeyType.TAB.equals(event.keyType())) tabCount ++;
        else tabCount = 0;
        switch (event.keyType()) {
            case CHAR -> {
                buffer.append(event.value());
                renderer.printChar(event.value());
            }

            case BACKSPACE -> {
                if (buffer.length() > 0) {
                    buffer.backspace();
                    renderer.eraseChar();
                }
            }

            case ENTER -> {
                List<String> history = CommandRegistry.getHistory();
                if(historyIndex > 0 && historyIndex < history.size()) buffer.append(history.get(historyIndex));
                renderer.newLine();
            }

            case TAB -> handleTab();

            case UPARROW, DOWNARROW -> handleArrows(event);

            default -> {
                // ignore
            }
        }
    }

    private void handleArrows(KeyEvent event) {
        List<String> history = CommandRegistry.getHistory();
        if (history.isEmpty()) return;
        if(KeyType.UPARROW.equals(event.keyType())) {
            if (historyIndex > 0) {
                historyIndex--;
                renderer.printString("\r$ " + history.get(historyIndex) + "\033[0K");
            }
        } else {
            if (historyIndex < history.size()) {
                historyIndex++;
                if (!(historyIndex >= history.size())) {
                    renderer.printString("\r$ " + history.get(historyIndex) + "\033[0K");
                }
            }
        }
    }

    private void handleTab() {
        String content = buffer.content();
        Optional<String> completedString = completionEngine.complete(content, tabCount);
        if(completedString.isEmpty()) {
            renderer.printString("\u0007");
            return;
        }

        completedString.ifPresent(completion -> {
            if(tabCount < 2) {
                String suffix = completion.substring(content.length());
                buffer.append(suffix);
                renderer.printString(suffix);
            } else {
                renderer.newLine();
                renderer.printString(completion);
                renderer.newLine();
                renderer.printString("$ " + buffer.content());
            }
        });
    }
}
