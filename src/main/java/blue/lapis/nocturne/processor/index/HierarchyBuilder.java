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
package blue.lapis.nocturne.processor.index;

import static blue.lapis.nocturne.util.helper.StringHelper.resolvePackageName;

import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Primitive;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts and applies a hierarchy to a given {@link JarClassEntry} set.
 */
public class HierarchyBuilder {

    private static final ImmutableSet<IndexedMethod.Signature> IGNORED_METHODS;

    private final ImmutableMap<String, IndexedClass> classes;

    static {
        IGNORED_METHODS = ImmutableSet.<IndexedMethod.Signature>builder()
                .add(new IndexedMethod.Signature("<init>", new MethodDescriptor(new Type(Primitive.VOID, 0))))
                .add(new IndexedMethod.Signature("<clinit>", new MethodDescriptor(new Type(Primitive.VOID, 0))))
                .build();
    }

    public HierarchyBuilder(Set<IndexedClass> classes) {
        this.classes = ImmutableMap.copyOf(classes.stream().collect(Collectors.toMap(IndexedClass::getName, c -> c)));
    }

    public void apply() {
        for (IndexedClass clazz : classes.values()) {
            clazz.getMethods().values().stream()
                    .filter(m -> !IGNORED_METHODS.contains(m.getSignature()))
                    .forEach(method -> {
                        Set<String> bases = getBaseDefinitionClasses(method.getSignature(), clazz);
                        method.getBaseDefinitions().addAll(bases);
                        bases.forEach(base -> classes.get(base).getMethods().get(method.getSignature()).getOverrides()
                                .add(clazz.getName()));
                    });
        }
    }

    private Set<String> getBaseDefinitionClasses(IndexedMethod.Signature sig, IndexedClass clazz) {
        return getBaseDefinitionClasses(sig, clazz, true);
    }

    private Set<String> getBaseDefinitionClasses(IndexedMethod.Signature sig, IndexedClass clazz, boolean returnEmpty) {
        Set<String> bases = new HashSet<>();

        Set<String> parents = Sets.newHashSet(clazz.getInterfaces());
        parents.add(clazz.getSuperclass());

        parents.stream().filter(classes::containsKey).forEach(className -> {
            IndexedClass interfaceClass = classes.get(className);
            if (interfaceClass.getMethods().containsKey(sig)) {
                if (isVisible(interfaceClass.getMethods().get(sig), clazz.getName(), className)) {
                    bases.addAll(getBaseDefinitionClasses(sig, interfaceClass, false));
                }
            }
        });

        return !bases.isEmpty() ? bases : returnEmpty ? Collections.EMPTY_SET : Collections.singleton(clazz.getName());
    }

    private static boolean isVisible(IndexedMethod method, String class1, String class2) {
        switch (method.getVisibility()) {
            case PUBLIC:
            case PROTECTED:
                return true;
            case PACKAGE:
                return resolvePackageName(class1).equals(resolvePackageName(class2));
            default:
                return false;
        }
    }

}
