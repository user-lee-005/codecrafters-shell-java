package terminal.factory;

import terminal.readers.TerminalReader;
import terminal.readers.UnixTerminalReader;
import terminal.readers.WindowsTerminalReader;
import terminal.renderer.Renderer;
import terminal.renderer.UnixRenderer;

public class TerminalFactory {
    public static final boolean IS_LINUX =
            System.getProperty("os.name").toLowerCase().contains("linux");

    public static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    public static final boolean IS_OSX =
            System.getProperty("os.name").toLowerCase().contains("mac");

    public static final boolean IS_AIX =
            System.getProperty("os.name").toLowerCase().contains("aix");

    public static TerminalReader createReader() {
        if(IS_WINDOWS) return new WindowsTerminalReader();
        return new UnixTerminalReader();
    }

    public static Renderer createRenderer() {
        return new UnixRenderer();
    }
}
