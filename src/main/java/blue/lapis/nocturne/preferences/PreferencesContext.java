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

package blue.lapis.nocturne.preferences;

import static blue.lapis.nocturne.util.helper.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PreferencesContext {

    public static PreferencesContext loadFrom(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        Properties props = new Properties();
        props.load(Files.newInputStream(file));

        checkNotNull(props, "properties");

        Map<PreferenceType, Object> prefMap = new HashMap<>();
        for (PreferenceType type : PreferenceType.values()) {
            if (props.containsKey(type.getKey())) {
                String valStr = props.getProperty(type.getKey());

                Object val;
                val = deserializeValue(type, valStr);

                prefMap.put(type, val);
            } else {
                prefMap.put(type, type.getDefaultValue());
            }
        }

        return new PreferencesContext(new HashMap<>(prefMap));
    }

    private String serializeValue(Map.Entry<PreferenceType, Object> entry) {
        String valStr;

        if (entry.getKey().getValueType() == String.class) {
            valStr = (String) entry.getValue();
        } else if (entry.getKey().getValueType() == boolean.class) {
            valStr = String.valueOf((boolean) entry.getValue());
        } else if (entry.getKey().getValueType() == int.class) {
            valStr = String.valueOf((int) entry.getValue());
        } else if (entry.getKey().getValueType() == long.class) {
            valStr = String.valueOf((long) entry.getValue());
        } else if (entry.getKey().getValueType() == short.class) {
            valStr = String.valueOf((short) entry.getValue());
        } else if (entry.getKey().getValueType() == byte.class) {
            valStr = String.valueOf((byte) entry.getValue());
        } else if (entry.getKey().getValueType() == char.class) {
            valStr = String.valueOf((char) entry.getValue());
        } else {
            throw new AssertionError("Unsupported value type " + entry.getKey().getValueType().getSimpleName()
                    + " for preference type " + entry.getKey().name());
        }

        return valStr;
    }

    private static Object deserializeValue(PreferenceType type, String valStr) {
        Object val;
        if (type.getValueType() == String.class) {
            val = valStr;
        } else if (type.getValueType() == boolean.class) {
            val = Boolean.valueOf(valStr);
        } else if (type.getValueType() == int.class) {
            val = Integer.valueOf(valStr);
        } else if (type.getValueType() == long.class) {
            val = Integer.valueOf(valStr);
        } else if (type.getValueType() == short.class) {
            val = Integer.valueOf(valStr);
        } else if (type.getValueType() == byte.class) {
            val = Integer.valueOf(valStr);
        } else if (type.getValueType() == char.class) {
            val = Integer.valueOf(valStr);
        } else {
            throw new AssertionError("Unsupported value type " + type.getValueType().getSimpleName()
                    + " for preference type " + type.name());
        }
        return val;
    }

    private final Map<PreferenceType, Object> prefMap;

    private PreferencesContext(Map<PreferenceType, Object> map) {
        this.prefMap = new HashMap<>(checkNotNull(map, "map"));
    }

    public PreferencesContext() {
        this(Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public <T> T getPreference(PreferenceType type) {
        checkNotNull(type, "type");
        return (T) (prefMap.containsKey(type) ? prefMap.get(type) : type.getDefaultValue());
    }

    public <T> void setPreference(PreferenceType type, T val) {
        prefMap.put(checkNotNull(type, "type"), checkNotNull(val, "val"));
    }

    public PreferencesContext copy() {
        return new PreferencesContext(prefMap);
    }

    public void mergeFrom(PreferencesContext ctx) {
        this.prefMap.putAll(ctx.prefMap);
    }

    public void saveTo(Path path) throws IOException {
        Properties props = new Properties();

        for (Map.Entry<PreferenceType, Object> entry : prefMap.entrySet()) {
            String valStr;

            valStr = serializeValue(entry);

            props.setProperty(entry.getKey().getKey(), valStr);
        }

        props.store(Files.newOutputStream(path), null);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PreferencesContext && this.prefMap.equals(((PreferencesContext) other).prefMap);
    }

}
