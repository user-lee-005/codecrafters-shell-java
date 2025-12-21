package terminal.readers;

import terminal.KeyEvent;
import terminal.KeyType;

import java.io.IOException;

public class UnixTerminalReader implements TerminalReader {
    private static String savedConfig;

    @Override
    public void enableRawMode() throws Exception {
        savedConfig = exec("stty -g");
        exec("stty -icanon -echo");
    }

    @Override
    public void disableRawMode() throws Exception {
        if(savedConfig != null) {
            exec("stty " + savedConfig );
        }
    }

    @Override
    public KeyEvent readKey() throws IOException {
        int c = System.in.read();
        return switch (c) {
            case -1 -> new KeyEvent(KeyType.EOF, '\0');
            case '\t' -> new KeyEvent(KeyType.TAB, '\0');
            case '\n', '\r' -> new KeyEvent(KeyType.ENTER, '\0');
            case 127, 8 -> new KeyEvent(KeyType.BACKSPACE, '\0');
            case 27 -> {
                int next1 = System.in.read();
                int next2 = System.in.read();
                if (next1 == '[') {
                    if (next2 == 'A') yield new KeyEvent(KeyType.UPARROW, '\0');
                    if (next2 == 'B') yield new KeyEvent(KeyType.DOWNARROW, '\0');
                }
                yield new KeyEvent(KeyType.UNKNOWN, '\0');
            }
            default -> {
                if(c >= 32 && c <= 126) {
                    yield new KeyEvent(KeyType.CHAR, (char) c);
                }
                yield new KeyEvent(KeyType.UNKNOWN, '\0');
            }
        };
    }

    private String exec(String cmd) throws Exception {
        Process process = new ProcessBuilder("sh", "-c", cmd)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();

        byte[] data = process.getInputStream().readAllBytes();
        process.waitFor();
        return new String(data).trim();
    }
}
