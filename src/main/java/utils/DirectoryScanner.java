package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static constants.Constants.cd;

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

    public static String getWorkingDirectory() {
        return currentDir;
    }

    public static boolean isDirectory(Path path) {
        if(path == null) {
            return false;
        }
        return Files.isDirectory(path);
    }

    public static void changeDirectory(List<String> args) {
        String arg = args.getFirst();
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

    public static Set<String> findAllExecutablesInPath() {
        String pathList = System.getenv("PATH");
        Set<String> executableNames = new HashSet<>();

        if (pathList == null || pathList.isBlank()) {
            return executableNames;
        }

        String separator = File.pathSeparator;
        String[] dirs = pathList.split(Pattern.quote(separator));

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        // Extensions to consider "executable" on Windows
        // (You could also read the PATHEXT environment variable for better accuracy)
        Set<String> windowsExtensions = Set.of(".exe", ".bat", ".cmd", ".com");

        for (String dir : dirs) {
            Path folder = Paths.get(dir);

            // Skip invalid paths or non-directories
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                continue;
            }

            // Use DirectoryStream to lazily iterate over files (better performance)
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path entry : stream) {
                    // We only care about files, not sub-directories
                    if (!Files.isRegularFile(entry)) continue;

                    if (isWindows) {
                        // On Windows, check if the file ends with a known executable extension
                        String fileName = entry.getFileName().toString().toLowerCase();
                        int lastDot = fileName.lastIndexOf('.');
                        if (lastDot > 0) {
                            String ext = fileName.substring(lastDot);
                            if (windowsExtensions.contains(ext)) {
                                executableNames.add(entry.getFileName().toString());
                            }
                        }
                    } else {
                        // On Linux/Mac, check the executable permission bit
                        if (Files.isExecutable(entry)) {
                            executableNames.add(entry.getFileName().toString());
                        }
                    }
                }
            } catch (IOException e) {
                // Permission denied or IO error on a specific folder; skip and continue
            }
        }

        return executableNames;
    }
}
