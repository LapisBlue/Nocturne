/*
 * Nocturne
 * Copyright (c) 2015-2016, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.mapping.model;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends MemberMapping {

    private final MethodDescriptor descriptor;
    private final IndexedMethod.Signature sig;

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the method
     * @param deobfName The deobfuscated name of the method
     * @param descriptor The (obfuscated) {@link MethodDescriptor descriptor} of
     *     the method
     * @param propagate Whether to propagate this mapping to super- and
     *     sub-classes
     */
    public MethodMapping(ClassMapping parent, String obfName, String deobfName, MethodDescriptor descriptor,
                         boolean propagate) {
        super(parent, obfName, deobfName);
        this.descriptor = descriptor;
        this.sig = new IndexedMethod.Signature(getObfuscatedName(), descriptor);

        parent.addMethodMapping(this, propagate);
    }

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the method
     * @param deobfName The deobfuscated name of the method
     * @param descriptor The (obfuscated) {@link MethodDescriptor descriptor} of
     *     the method
     */
    public MethodMapping(ClassMapping parent, String obfName, String deobfName, MethodDescriptor descriptor) {
        this(parent, obfName, deobfName, descriptor, true);
    }

    public void initialize(boolean propagate) {
        this.setDeobfuscatedName(getDeobfuscatedName(), propagate);
    }

    /**
     * Returns the {@link MethodDescriptor} of this method.
     *
     * @return The {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getObfuscatedDescriptor() {
        return descriptor;
    }

    /**
     * Returns the deobfuscated {@link MethodDescriptor} of this method.
     *
     * @return The deobfuscated {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getDeobfuscatedDescriptor() {
        return getObfuscatedDescriptor().deobfuscate(getParent().getContext());
    }

    @Override
    protected SelectableMember.MemberKey getMemberKey() {
        return new SelectableMember.MemberKey(MemberType.METHOD, getQualifiedName(),
                getObfuscatedDescriptor().toString());
    }

    private String getQualifiedName() {
        return getParent().getFullObfuscatedName() + CLASS_PATH_SEPARATOR_CHAR + getObfuscatedName();
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String deobf, boolean propagate) {
        super.setDeobfuscatedName(deobf);

        if (propagate && !IndexedClass.INDEXED_CLASSES.isEmpty()) {
            for (String clazz : getClassesInHierarchy(getParent().getFullObfuscatedName(), sig)) {
                if (clazz.equals(getParent().getObfuscatedName())) {
                    continue;
                }

                ClassMapping cm = MappingsHelper.getOrCreateClassMapping(getContext(), clazz);
                if (cm.getMethodMappings().containsKey(getObfuscatedName())) {
                    cm.getMethodMappings().get(getObfuscatedName()).setDeobfuscatedName(deobf, false);
                } else {
                    new MethodMapping(cm, getObfuscatedName(), deobf, getObfuscatedDescriptor(), false);
                }
            }
        }

        Main.getLoadedJar().getClass(getParent().getFullObfuscatedName()).get()
                .getCurrentMethodNames().put(sig, new IndexedMethod.Signature(deobf, descriptor));
    }

    public static Set<String> getClassesInHierarchy(String parentClass, IndexedMethod.Signature signature) {
        Set<String> classSet = new HashSet<>();

        IndexedMethod method = IndexedClass.INDEXED_CLASSES
                .get(parentClass)
                .getMethods().get(signature);

        Set<String> bases = method.getBaseDefinitions();
        if (bases.isEmpty()) {
            bases = Sets.newHashSet(parentClass);
        }

        for (String base : bases) {
            IndexedClass index = IndexedClass.INDEXED_CLASSES.get(base);
            classSet.addAll(Lists.newArrayList(index.getMethods().get(signature).getOverrides()));
            classSet.add(base);
        }

        return classSet;
    }

}
