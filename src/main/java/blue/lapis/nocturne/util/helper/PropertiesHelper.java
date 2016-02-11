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
package blue.lapis.nocturne.util.helper;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.util.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Static utility class for managing the global {@link Properties}.
 */
public final class PropertiesHelper {

    private final String propertiesFileName = "global.properties";

    private final File globalPropertiesFile
            = new File(getNocturneDirectory(), propertiesFileName);
    private final Properties globalProperties = new Properties();

    public PropertiesHelper() {
        try {
            if (!Files.exists(globalPropertiesFile.toPath())) {
                Files.createDirectories(globalPropertiesFile.getParentFile().toPath());
                Files.createFile(globalPropertiesFile.toPath());
                Main.getLogger().info("Created new global properties file");
            }
            globalProperties.load(new FileInputStream(globalPropertiesFile));
            Main.getLogger().info("Loaded global properties file from " + globalPropertiesFile.getAbsolutePath());

            // set keys
            Key.getKeys().stream().filter(key -> !globalProperties.containsKey(key.getKey()))
                    .forEach(key -> globalProperties.setProperty(key.getKey(), key.getDefaultValue()));
            store();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getProperty(Key key) {
        if (!globalProperties.containsKey(key.getKey())) {
            globalProperties.setProperty(key.getKey(), key.getDefaultValue());
        }
        return globalProperties.getProperty(key.getKey());
    }

    public void setProperty(Key key, String value) {
        globalProperties.setProperty(key.getKey(), value);
        try {
            store();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store property to disk", ex);
        }
    }

    public void store() throws IOException {
        final String description = "Global configuration file for Nocturne";
        globalProperties.store(new FileOutputStream(globalPropertiesFile), description);
    }

    private File getNocturneDirectory() {
        String appdata = OperatingSystem.getOs().getAppDataFolder();
        if (OperatingSystem.getOs() == OperatingSystem.LINUX) {
            return new File(appdata, ".config" + File.separator + "nocturne");
        } else {
            return new File(appdata, "Nocturne");
        }
    }

    public static class Key {

        private static final List<Key> KEYS = new ArrayList<>();

        public static final Key LOCALE = new Key("locale", "en_US");
        public static final Key LAST_JAR_DIRECTORY = new Key("lastJarDir", "");
        public static final Key LAST_MAPPINGS_DIRECTORY = new Key("lastMappingsDir", "");

        private final String key;
        private final String defaultValue;

        private Key(String key, String defaultValue) {
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

        public static List<Key> getKeys() {
            return KEYS;
        }

    }
}
