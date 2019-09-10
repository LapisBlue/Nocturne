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

package blue.lapis.nocturne.jar.model;

import blue.lapis.nocturne.jar.model.hierarchy.Hierarchy;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyNode;
import blue.lapis.nocturne.util.helper.ReferenceHelper;

import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.QualifiedReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a set of {@link JarClassEntry JarClassEntries} loaded from a JAR
 * file.
 */
public class ClassSet {

    private final String setName;
    private final Map<ClassReference, JarClassEntry> classMap = new HashMap<>();
    private final Map<ClassReference, String> names = new HashMap<>();

    /**
     * Constructs a new {@link ClassSet} from the given {@link JarClassEntry}
     * {@link Set}.
     *
     * @param classes The {@link JarClassEntry JarClassEntries} to populate the
     *                new {@link ClassSet} with
     */
    public ClassSet(String setName, Set<JarClassEntry> classes) {
        this.setName = setName;
        classes.forEach(cl -> {
            //TODO: move
            /*if (looksDeobfuscated(cl.getName())) {
                cl.setDeobfuscated(true);
            }*/

            classMap.put(cl.getReference(), cl);
            getCurrentNames().put(cl.getReference(), ReferenceHelper.getDisplayName(cl.getReference(), null));
        });
    }

    /**
     * Returns the name of this {@link ClassSet}.
     *
     * @return The name of this {@link ClassSet}
     */
    public String getName() {
        return setName;
    }

    /**
     * Returns an unmodifiable {@link Set} of all classes contained by this
     * {@link ClassSet}.
     *
     * @return An unmodifiable {@link Set} of all classes contained by this
     * {@link ClassSet}
     */
    public Set<JarClassEntry> getClasses() {
        return Collections.unmodifiableSet(new HashSet<>(classMap.values()));
    }

    /**
     * Returns an unmodifiable {@link Set} of all obfuscated classes contained
     * by this {@link ClassSet}.
     *
     * @return An unmodifiable {@link Set} of all obfuscated classes contained
     * by this {@link ClassSet}
     */
    public Set<JarClassEntry> getObfuscatedClasses() {
        return Collections.unmodifiableSet(getClasses().stream()
                .filter(c -> !c.isDeobfuscated())
                .collect(Collectors.toSet()));
    }

    /**
     * Returns an unmodifiable {@link Set} of all deobfuscated classes contained
     * by this {@link ClassSet}.
     *
     * @return An unmodifiable {@link Set} of all deobfuscated classes contained
     * by this {@link ClassSet}
     */
    public Set<JarClassEntry> getDeobfuscatedClasses() {
        return Collections.unmodifiableSet(getClasses().stream().filter(JarClassEntry::isDeobfuscated)
                .collect(Collectors.toSet()));
    }

    /**
     * Returns the {@link JarClassEntry} for the given FQ name, if it exists.
     *
     * @return The {@link JarClassEntry} for the given FQ name, or
     * {@link Optional#empty()} if it does not exist in this
     * {@link ClassSet}
     */
    public Optional<JarClassEntry> getClass(ClassReference reference) {
        return classMap.containsKey(reference) ? Optional.of(classMap.get(reference)) : Optional.empty();
    }

    /**
     * Returns a {@link HierarchyNode} representing the structure of
     * obfuscated classes contained by this {@link ClassSet}.
     *
     * @return A {@link HierarchyNode} representing the structure of
     * obfuscated classes contained by this {@link ClassSet}
     */
    public Hierarchy getObfuscatedHierarchy() {
        return generateHierarchy(getObfuscatedClasses(), false);
    }

    /**
     * Returns a {@link HierarchyNode} representing the structure of
     * deobfuscated classes contained by this {@link ClassSet}.
     *
     * @return A {@link HierarchyNode} representing the structure of
     * deobfuscated classes contained by this {@link ClassSet}
     */
    public Hierarchy getDeobfuscatedHierarchy() {
        return generateHierarchy(getDeobfuscatedClasses(), true);
    }

    /**
     * Generates a {@link HierarchyNode} from the given {@link JarClassEntry}
     * {@link Set}.
     *
     * @param entrySet The {@link JarClassEntry} {@link Set} to generate the
     *                 {@link HierarchyNode} from
     * @return The generated {@link HierarchyNode}
     */
    private Hierarchy generateHierarchy(Set<JarClassEntry> entrySet, boolean deobfuscate) {
        return Hierarchy.fromSet(entrySet.stream()
                .filter(e -> e.getReference().getType() != QualifiedReference.Type.INNER_CLASS)
                //.map(deobfuscate ? JarClassEntry::getDeobfuscatedName : JarClassEntry::getName)
                .collect(Collectors.toSet()), deobfuscate);
    }

    public Map<ClassReference, String> getCurrentNames() {
        return names;
    }

}
