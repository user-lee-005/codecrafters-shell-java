package terminal.readers;

import terminal.KeyEvent;
import terminal.KeyType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WindowsTerminalReader implements TerminalReader {

    private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void enableRawMode() {
        // no-op
    }

    @Override
    public void disableRawMode() {
        // no-op
    }

    @Override
    public KeyEvent readKey() throws IOException {
        int c = System.in.read();
        return switch (c) {
            case -1 -> new KeyEvent(KeyType.EOF, '\0');
            case '\t' -> new KeyEvent(KeyType.TAB, '\0');
            case '\n', '\r' -> new KeyEvent(KeyType.ENTER, '\0');
            case 127, 8 -> new KeyEvent(KeyType.BACKSPACE, '\0');
            default -> {
                if(c >= 32 && c <= 126) {
                    yield new KeyEvent(KeyType.CHAR, (char) c);
                }
                yield new KeyEvent(KeyType.UNKNOWN, '\0');
            }
        };
    }
}

