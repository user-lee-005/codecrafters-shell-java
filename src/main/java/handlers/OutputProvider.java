package handlers;

import java.io.IOException;
import java.io.PrintStream;

public interface OutputProvider {
    PrintStream get() throws IOException;
}
