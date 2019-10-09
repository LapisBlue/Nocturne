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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum PreferenceType {

    COLORBLIND_MODE(boolean.class, "accessibility.colorblind", false);

    private final Class<?> valType;
    private final String key;
    private final Object def;

    PreferenceType(Class<?> valType, String key, Object def) {
        this.valType = valType;
        this.key = key;
        this.def = def;
        register();
    }

    public Class<?> getValueType() {
        return valType;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return def;
    }

    private void register() {
        if (keyMap == null) {
            keyMap = new HashMap<>();
        }

        keyMap.put(this.key, this);
    }

    private static Map<String, PreferenceType> keyMap;

    private static Optional<PreferenceType> fromKey(String key) {
        return Optional.ofNullable(keyMap.get(key));
    }

}
