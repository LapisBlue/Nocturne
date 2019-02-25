/*
 * Nocturne
 * Copyright (c) 2015-2019, Lapis <https://github.com/LapisBlue>
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

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Represents common operating systems.
 */
public enum OperatingSystem implements Predicate<String> {

    OSX("mac"),
    LINUX("nix", "nux") {
        private final String home = System.getenv().getOrDefault("HOME", System.getProperty("user.home", "~"));

        private final String dataHome = System.getenv().getOrDefault("XDG_DATA_HOME", home + "/.local/share");
        private final String configHome = System.getenv().getOrDefault("XDG_CONFIG_HOME", home + "/.config");
        private final String cacheHome = System.getenv().getOrDefault("XDG_CACHE_HOME", home + "/.cache");

        @Override
        public String getConfigFolder() {
            return configHome;
        }

        @Override
        public String getDataFolder() {
            return dataHome;
        }

        @Override
        public String getCacheFolder() {
            return cacheHome;
        }
    },
    WINDOWS("win"),
    UNKNOWN;

    private final String[] partials;

    OperatingSystem(String... partials) {
        this.partials = partials;
    }

    @Override
    public boolean test(String s) {
        return Arrays.stream(this.partials)
                .anyMatch(s::contains);
    }

    /**
     * Returns the path to the config home, as defined by the XDG Base Directory specification.
     *
     * <p>The config home defines the base directory relative to which user specific configuration
     * files should be stored. On Linux, this can be modified by the environment variable
     * {@code $XDG_CONFIG_HOME}. On OS X, this is ~/Library/Application Support. On Windows,
     * this is %APPDATA%.
     *
     * @return The path to the config home, as defined by the XDG Base Directory specification.
     */
    public String getConfigFolder() {
        switch (this) {
            case OSX:
                return System.getProperty("user.home") + "/Library/Application Support";
            case WINDOWS:
                return System.getenv("APPDATA");
            case LINUX:
                throw new AssertionError();
            case UNKNOWN:
            default:
                return System.getProperty("user.home");
        }
    }

    /**
     * Returns the path to the data home, as defined by the XDG Base Directory specification.
     *
     * <p>The data home defines the base directory relative to which user specific data
     * files should be stored. On Linux, this can be modified by the environment variable
     * {@code $XDG_DATA_HOME}. On other operating systems, this method is equivalent to
     * {@code getConfigFolder()}.
     *
     * @return The path to the data home, as defined by the XDG Base Directory specification.
     */
    public String getDataFolder() {
        return getConfigFolder();
    }

    /**
     * Returns the path to the cache home, as defined by the XDG Base Directory specification.
     *
     * <p>The cache home defines the base directory relative to which user specific
     * <i>non-essential</i> data files should be stored. On Linux, this can be modified
     * by the environment variable {@code $XDG_CACHE_HOME}. On other operating systems,
     * this method is equivalent to {@code getConfigFolder()}.
     *
     * @return The path to the cache home, as defined by the XDG Base Directory specification.
     */
    public String getCacheFolder() {
        return getConfigFolder();
    }

    /**
     * Gets the operating system currently running on the user's system.
     *
     * @return The current {@link OperatingSystem}.
     */
    public static OperatingSystem getOs() {
        final String osName = System.getProperty("os.name").toLowerCase();

        return Arrays.stream(values())
                .filter(os -> os.test(osName))
                .findFirst().orElse(OperatingSystem.UNKNOWN);
    }
}
