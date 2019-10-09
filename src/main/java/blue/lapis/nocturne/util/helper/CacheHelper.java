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

package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.util.helper.FilesystemHelper.getNocturneDirectory;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.util.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Static utility class for managing {@link Properties} files.
 */
public final class CacheHelper {

    private static final String CACHE_FILE_NAME = "cache.properties";

    private final Path cacheFile = getNocturneDirectory().resolve(CACHE_FILE_NAME);
    private final Properties cacheProps = new Properties();

    public CacheHelper() {
        try {
            if (!Files.exists(cacheFile)) {
                Files.createDirectories(cacheFile.getParent());
                Files.createFile(cacheFile);
                Main.getLogger().info("Created new cache file");
            }
            cacheProps.load(Files.newInputStream(cacheFile));
            Main.getLogger().info("Loaded cache file from " + cacheFile.toAbsolutePath().toString());

            // set keys
            CacheKey.getKeys().stream().filter(key -> !cacheProps.containsKey(key.getKey()))
                    .forEach(key -> cacheProps.setProperty(key.getKey(), key.getDefaultValue()));
            store();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getProperty(CacheKey key) {
        if (!cacheProps.containsKey(key.getKey())) {
            cacheProps.setProperty(key.getKey(), key.getDefaultValue());
        }
        return cacheProps.getProperty(key.getKey());
    }

    public void setProperty(CacheKey key, String value) {
        cacheProps.setProperty(key.getKey(), value);
        try {
            store();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store property to disk", ex);
        }
    }

    public void store() throws IOException {
        final String description = "Global configuration file for Nocturne";
        cacheProps.store(Files.newOutputStream(cacheFile), description);
    }

    public static class CacheKey {

        private static final List<CacheKey> KEYS = new ArrayList<>();

        public static final CacheKey LOCALE = new CacheKey("locale", "en_US");
        public static final CacheKey LAST_JAR_DIRECTORY = new CacheKey("lastJarDir", "");
        public static final CacheKey LAST_MAPPINGS_DIRECTORY = new CacheKey("lastMappingsDir", "");
        public static final CacheKey LAST_MAPPING_LOAD_FORMAT = new CacheKey("lastMappingLoadFormat", "");
        public static final CacheKey LAST_MAPPING_SAVE_FORMAT = new CacheKey("lastMappingSaveFormat", "");

        private final String key;
        private final String defaultValue;

        private CacheKey(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
            KEYS.add(this);
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public static List<CacheKey> getKeys() {
            return KEYS;
        }

    }
}
