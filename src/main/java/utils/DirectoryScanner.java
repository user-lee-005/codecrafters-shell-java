package utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static utils.Constants.cd;

public class DirectoryScanner {
    private static String currentDir = Paths.get("").toAbsolutePath().toString();
    public static String findExecutable(String command, String pathList) {
        if (pathList == null || pathList.isBlank()) return null;

        String separator = File.pathSeparator;
        String[] dirs = pathList.split(Pattern.quote(separator));

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        // Windows extensions to try
        String[] windowsExt = {".exe", ".bat", ".cmd", ".com"};

        for (String dir : dirs) {
            Path folder = Paths.get(dir);

            if (!Files.isDirectory(folder)) continue;

            if (isWindows) {
                // Try all Windows executable extensions
                for (String ext : windowsExt) {
                    Path target = folder.resolve(command + ext);
                    if (Files.exists(target) && Files.isRegularFile(target)) {
                        return target.toAbsolutePath().toString();
                    }
                }

                // If command itself has extension
                Path direct = folder.resolve(command);
                if (Files.exists(direct) && Files.isRegularFile(direct)) {
                    return direct.toAbsolutePath().toString();
                }

            } else {
                // Linux / macOS
                Path target = folder.resolve(command);
                if (Files.exists(target) && Files.isRegularFile(target) && Files.isExecutable(target)) {
                    return target.toAbsolutePath().toString();
                }
            }
        }

        return null;
    }

    public static String findExecutableIfPresentInPath(String path, String command) {
        String separator = File.pathSeparator;
        for(String dir: path.split(Pattern.quote(separator))) {
            Path folder = Paths.get(dir);
            if(!Files.isDirectory(folder)) continue;
            Path target = folder.resolve(command);
            if(Files.exists(target) && isExecutableFile(target)) {
                return target.toString();
            }
        }
        return null;
    }

    public static boolean isExecutableFile(Path path) {
        return Files.isExecutable(path);
    }

    public static String getWorkingDirectory() {
        return currentDir;
    }

    public static boolean isDirectory(Path path) {
        if(path == null) {
            return false;
        }
        return Files.isDirectory(path);
    }

    public static void changeDirectory(String arg) {
        Path resolved = resolvePath(arg, currentDir);
        changeCurrentDirectory(arg, resolved);
    }

    private static Path resolvePath(String arg, String currentDir) {
        Path base = Paths.get(currentDir);
        if (arg.startsWith("~")) {
            return Paths.get(System.getenv("HOME"))
                    .resolve(arg.substring(1))
                    .normalize();
        }

        if (arg.startsWith("/")) {
            return Paths.get(arg).normalize();
        }

        return base.resolve(arg).normalize();
    }

    private static void changeCurrentDirectory(String arg, Path path) {
        if(path == null) {
            currentDir = Paths.get("/").getRoot().toAbsolutePath().toString();
        } else {
            if(isDirectory(path)) {
                currentDir = path.toAbsolutePath().toString();
            } else {
                System.out.println(cd + ": " + arg + ": No such file or directory");
            }
        }
    }
}
