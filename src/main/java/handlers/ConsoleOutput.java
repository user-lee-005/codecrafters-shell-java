package handlers;

import java.io.PrintStream;

public class ConsoleOutput implements OutputProvider {
    @Override
    public PrintStream get() {
        return System.out;
    }
}
