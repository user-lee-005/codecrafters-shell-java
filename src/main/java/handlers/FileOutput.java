package handlers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class FileOutput implements OutputProvider {
    private final String redirectFile;
    public FileOutput(String redirectFile) {
        this.redirectFile = redirectFile;
    }

    @Override
    public PrintStream get() throws IOException {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(redirectFile)), true);
    }
}
