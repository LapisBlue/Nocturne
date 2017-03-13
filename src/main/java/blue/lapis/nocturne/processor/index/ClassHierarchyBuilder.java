/*
 * Nocturne
 * Copyright (c) 2015-2017, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.processor.index;

import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;
import blue.lapis.nocturne.util.helper.HierarchyHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hierarchy builder for indexed classes.
 */
public class ClassHierarchyBuilder {

    private final ImmutableMap<String, IndexedClass> classes;

    public ClassHierarchyBuilder(Set<IndexedClass> classes) {
        this.classes = ImmutableMap.copyOf(classes.stream().collect(Collectors.toMap(IndexedClass::getName, c -> c)));
    }

    public void buildHierarchies() {
        buildClassHierarchies();
        buildMethodHierarchies();
        classes.values().forEach(c -> {
            c.finalizeHierarchy();
            c.getMethods().values().forEach(IndexedMethod::finalizeHierarchy);
        });
    }

    private void buildClassHierarchies() {
        for (IndexedClass clazz : classes.values()) {
            if (!clazz.getHierarchy().isEmpty()) {
                Set<IndexedClass> toAdd = new HashSet<>();
                for (IndexedClass child : clazz.getHierarchy()) {
                    toAdd.addAll(child.getHierarchy());
                }
                clazz.getHierarchy().addAll(toAdd);
            } else {
                Set<IndexedClass> parents = getParents(clazz);
                clazz.getHierarchy().addAll(parents);
            }
            clazz.getHierarchy().forEach(c -> c.getHierarchy().add(clazz));
        }
    }

    private Set<IndexedClass> getParents(IndexedClass clazz) {
        return getParents(clazz, true);
    }

    private Set<IndexedClass> getParents(IndexedClass clazz, boolean returnEmpty) {
        Set<IndexedClass> parents = new HashSet<>();

        Set<String> parentNames = Sets.newHashSet(clazz.getInterfaces());
        parentNames.add(clazz.getSuperclass());
        Set<IndexedClass> directParents = parentNames.stream().filter(classes::containsKey)
                .map(classes::get).collect(Collectors.toSet());
        parents.addAll(directParents);

        for (IndexedClass ic : directParents) {
            parents.addAll(getParents(ic, false));
        }

        return !parents.isEmpty() ? parents : returnEmpty ? new HashSet<>() : Sets.newHashSet(clazz);
    }

    private void buildMethodHierarchies() {
        for (IndexedClass clazz : classes.values()) {
            clazz.getMethods().values().stream()
                    .filter(method -> method.getVisibility() != IndexedMethod.Visibility.PRIVATE)
                    .forEach(method -> {
                        method.getHierarchy().addAll(clazz.getHierarchy().stream()
                                .filter(c -> c.getMethods().containsKey(method.getSignature())
                                && HierarchyHelper.isVisible(
                                        clazz.getName(), c.getName(),
                                        c.getMethods().get(method.getSignature()).getVisibility()))
                                .collect(Collectors.toSet()));
                    });
        }
    }

}
