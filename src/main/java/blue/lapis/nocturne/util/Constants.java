package blue.lapis.nocturne.util;

import java.util.regex.Pattern;

/**
 * Utility class for storing constant values.
 */
public class Constants {

    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final Pattern INNER_CLASS_SEPARATOR_PATTERN
            = Pattern.compile(INNER_CLASS_SEPARATOR_CHAR + "", Pattern.LITERAL);

    public static final char CLASS_PATH_SEPARATOR_CHAR = '/';
    public static final Pattern CLASS_PATH_SEPARATOR_PATTERN
            = Pattern.compile(CLASS_PATH_SEPARATOR_CHAR + "", Pattern.LITERAL);
}
