package terminal.buffer;

public class InputBuffer {
    private final StringBuilder buffer = new StringBuilder();

    public void append(char c) {
        buffer.append(c);
    }

    public void append(String s) {
        buffer.append(s);
    }

    public void backspace() {
        if(!buffer.isEmpty()) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
    }

    public String content() {
        return buffer.toString();
    }

    public int length() {
        return buffer.length();
    }

    public void clear() {
        buffer.setLength(0);
    }
}
