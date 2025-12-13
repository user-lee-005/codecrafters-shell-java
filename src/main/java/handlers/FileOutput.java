package handlers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class FileOutput implements OutputProvider {
    private final String redirectFile;
    private final boolean append;
    public FileOutput(String redirectFile, boolean append) {
        this.redirectFile = redirectFile;
        this.append = append;
    }

    @Override
    public PrintStream get() throws IOException {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(redirectFile, append)), true);
    }
}
