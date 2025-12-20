package terminal.readers;

import terminal.KeyEvent;

import java.io.IOException;

public interface TerminalReader {
    void enableRawMode() throws Exception;
    void disableRawMode() throws Exception;
    KeyEvent readKey() throws IOException;
}
