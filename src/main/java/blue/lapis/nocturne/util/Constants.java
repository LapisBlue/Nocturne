/*
 * Nocturne
 * Copyright (c) 2015, Lapis <https://github.com/LapisBlue>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package blue.lapis.nocturne.util;

import blue.lapis.nocturne.Main;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import java.util.regex.Pattern;

/**
 * Utility class for storing constant values.
 */
public final class Constants {

    public static final String VERSION;

    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final Pattern INNER_CLASS_SEPARATOR_PATTERN
            = Pattern.compile(INNER_CLASS_SEPARATOR_CHAR + "", Pattern.LITERAL);

    public static final char CLASS_PATH_SEPARATOR_CHAR = '/';
    public static final Pattern CLASS_PATH_SEPARATOR_PATTERN
            = Pattern.compile(CLASS_PATH_SEPARATOR_CHAR + "", Pattern.LITERAL);

    public static final String CLASS_FILE_NAME_TAIL = ".class";

    public static final int CLASS_FORMAT_CONSTANT_POOL_OFFSET = 8; // byte offset of the CP per the class file format
    public static final int SHORT_UNSIGNER = (int) Math.pow(2, Short.BYTES * 8) - 1;
    public static final long INT_UNSIGNER = (long) Math.pow(2, Integer.BYTES * 8) - 1;

    public static final String MEMBER_PREFIX = "%NOCTURNE+";
    public static final String MEMBER_DELIMITER = "-";
    public static final String MEMBER_SUFFIX = "%";
    public static final Pattern MEMBER_REGEX = Pattern.compile(Pattern.quote(MEMBER_PREFIX) + "(.+?)"
            + Pattern.quote(MEMBER_DELIMITER) + "(.+?)(?:" + Pattern.quote(MEMBER_DELIMITER) + "(.+?))*"
            + Pattern.quote(MEMBER_SUFFIX));

    public static final Pattern STRING_LITERAL_REGEX = Pattern.compile("(?:^|[^\\\\])(\".+?[^\\\\]\")");
    public static final Pattern CHAR_LITERAL_REGEX = Pattern.compile("'.'");
    public static final Pattern KEYWORD_REGEX = Pattern.compile("(?:^|[^0-z\\\\$_])"
            + "(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|double|do|else|enum|"
            + "extends|false|final|finally|float|for|goto|if|implements|import|instanceof|interface|int|long|native|"
            + "new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|"
            + "throws|throw|transient|true|try|void|volatile|while)"
            + "(?=$|[^0-z\\\\$_])");

    /**
     * Regular expression to match the types contained by a method descriptor.
     */
    // side-note: I'm really proud of this thing. I wrote it in like 2 minutes and it works exactly how I want it to.
    public static final Pattern TYPE_SEQUENCE_REGEX = Pattern.compile("(\\[*(?:(?:L.+?;)|.))");

    public static final ImmutableMap<String, Object> FF_OPTIONS = ImmutableMap.<String, Object>builder()
            .put("rsy", "1") // hide synthetic class members
            .put("ind", "    ") // set indentation string
            .build();

    static {
        VERSION = MoreObjects.firstNonNull(Main.class.getPackage().getImplementationVersion(), "UNKNOWN");
    }

    private Constants() {
    }
}
