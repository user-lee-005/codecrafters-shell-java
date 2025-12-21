package terminal.renderer;

public class UnixRenderer implements Renderer {
    @Override
    public void printChar(char c) {
        System.out.print(c);
        System.out.flush();
    }

    @Override
    public void eraseChar() {
        System.out.print("\b \b");
        System.out.flush();
    }

    @Override
    public void printString(String string) {
        System.out.print(string);
        System.out.flush();
    }

    @Override
    public void newLine() {
        System.out.print("\n");
        System.out.flush();
    }

    @Override
    public void clearLine() {
        // Clear entire line
        System.out.print("\u001B[3K");
        // Move cursor to column 0
        System.out.print("\u001B[3G");
        System.out.flush();
    }
}
