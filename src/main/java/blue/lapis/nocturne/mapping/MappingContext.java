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
package blue.lapis.nocturne.mapping;

import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of {@link ClassMapping}s.
 */
public class MappingContext {

    private final Map<String, TopLevelClassMapping> mappings = new HashMap<>();

    private boolean dirty;

    /**
     * Returns an {@link ImmutableMap} of all {@link TopLevelClassMapping}s contained by
     * this {@link MappingContext}.
     *
     * @return An {@link ImmutableMap} of all {@link TopLevelClassMapping}s contained by
     *     this {@link MappingContext}
     */
    public ImmutableMap<String, TopLevelClassMapping> getMappings() {
        return ImmutableMap.copyOf(mappings);
    }

    /**
     * Adds the given {@link TopLevelClassMapping} to this {@link MappingContext}.
     *
     * @param mapping The {@link TopLevelClassMapping} to add
     */
    public void addMapping(TopLevelClassMapping mapping) {
        mappings.put(mapping.getObfuscatedName(), mapping);
        setDirty(true);
    }

    //TODO: probably add a removeMapping method at some point

    /**
     * Merges the given {@link MappingContext} into the current one.
     *
     * <p>Note that mappings from the provided set will take precedence over
     * existing ones if they are already present.</p>
     *
     * @param context The {@link MappingContext} to merge
     */
    public void merge(MappingContext context) {
        this.mappings.putAll(context.getMappings());
        if (!context.getMappings().isEmpty()) {
            setDirty(true);
        }
    }

    /**
     * Immediately clears all mappings from this {@link MappingContext}.
     */
    public void clear() {
        this.mappings.clear();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
