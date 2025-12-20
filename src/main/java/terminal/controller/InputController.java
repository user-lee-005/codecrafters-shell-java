package terminal.controller;

import terminal.KeyEvent;
import terminal.buffer.InputBuffer;
import terminal.renderer.CompletionEngine;
import terminal.renderer.Renderer;

public class InputController {

    private final InputBuffer buffer;
    private final Renderer renderer;
    private final CompletionEngine completionEngine = new CompletionEngine();

    public InputController(InputBuffer buffer, Renderer renderer) {
        this.buffer = buffer;
        this.renderer = renderer;
    }

    public void handle(KeyEvent event) {
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
                renderer.newLine();
                // execution happens outside
            }

            case TAB -> handleTab();

            default -> {
                // ignore
            }
        }
    }

    private void handleTab() {
        String content = buffer.content();

        completionEngine.complete(content).ifPresent(completion -> {
            String suffix = completion.substring(content.length());

            buffer.append(suffix);

            renderer.printString(suffix);
        });
    }
}
