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
package blue.lapis.nocturne.analysis.model;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a set of {@link JarClassEntry JarClassEntries} loaded from a JAR
 * file.
 */
public class ClassSet {

    private Map<String, JarClassEntry> classMap = new HashMap<>();

    /**
     * Constructs a new {@link ClassSet} from the given {@link JarClassEntry}
     * {@link Set}.
     *
     * @param classes The {@link JarClassEntry JarClassEntries} to populate the
     *     new {@link ClassSet} with
     */
    public ClassSet(Set<JarClassEntry> classes) {
        classes.forEach(cl -> classMap.put(cl.getName(), cl));
    }

    /**
     * Returns an {@link ImmutableSet} of all classes contained by this
     * this {@link ClassSet}.
     *
     * @return An {@link ImmutableSet} of all classes contained by this
     *     this {@link ClassSet}
     */
    public ImmutableSet<JarClassEntry> getClasses() {
        return ImmutableSet.copyOf(classMap.values());
    }

    /**
     * Returns an {@link ImmutableSet} of all obfuscated classes contained by
     * this {@link ClassSet}.
     *
     * @return An {@link ImmutableSet} of all obfuscated classes contained by
     *     this {@link ClassSet}
     */
    public ImmutableSet<JarClassEntry> getObfuscatedClasses() {
        return ImmutableSet.copyOf(getClasses().stream().filter(c -> !c.isDeobfuscated()).collect(Collectors.toSet()));
    }

    /**
     * Returns an {@link ImmutableSet} of all deobfuscated classes contained by
     * this {@link ClassSet}.
     *
     * @return An {@link ImmutableSet} of all deobfuscated classes contained by
     *     this {@link ClassSet}
     */
    public ImmutableSet<JarClassEntry> getDebfuscatedClasses() {
        return ImmutableSet.copyOf(getClasses().stream().filter(JarClassEntry::isDeobfuscated)
                .collect(Collectors.toSet()));
    }

}