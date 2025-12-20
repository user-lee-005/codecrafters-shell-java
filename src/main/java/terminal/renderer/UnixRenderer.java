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
}
