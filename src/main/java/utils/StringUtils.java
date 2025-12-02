package utils;

import java.util.Objects;

public class StringUtils {
    public static boolean equals(String source, String target) {
        return Objects.equals(source, target);
    }

    public static boolean startsWith(String actualString, String prefix) {
        return actualString.startsWith(prefix);
    }
}
