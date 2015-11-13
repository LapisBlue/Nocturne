/*
 * Shroud
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
package blue.lapis.shroud.mapping;

import blue.lapis.shroud.mapping.model.ClassMapping;
import blue.lapis.shroud.mapping.model.TopLevelClassMapping;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of {@link ClassMapping}s.
 */
public class MappingSet {

    private final Map<String, TopLevelClassMapping> mappings = new HashMap<>();

    /**
     * Returns an {@link ImmutableMap} of all {@link ClassMapping}s contained by
     * this {@link MappingSet}.
     *
     * @return An {@link ImmutableMap} of all {@link ClassMapping}s contained by
     *     this {@link MappingSet}
     */
    public ImmutableMap<String, ClassMapping> getMappings() {
        return ImmutableMap.copyOf(mappings);
    }

    /**
     * Adds the given {@link TopLevelClassMapping} to this {@link MappingSet}.
     *
     * @param mapping The {@link TopLevelClassMapping} to add
     */
    public void addMapping(TopLevelClassMapping mapping) {
        mappings.put(mapping.getObfuscatedName(), mapping);
    }

}
