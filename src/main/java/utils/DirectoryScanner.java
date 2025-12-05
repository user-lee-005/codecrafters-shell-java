package utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class DirectoryScanner {
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
        return Paths.get("").toAbsolutePath().toString();
    }
}
