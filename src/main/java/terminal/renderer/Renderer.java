package terminal.renderer;

public interface Renderer {
    void printChar(char c);
    void eraseChar();
    void printString(String string);
    void newLine();
    void clearLine();
}
