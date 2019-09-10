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

package blue.lapis.nocturne.mapping.model;

import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.helper.HierarchyHelper;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.MethodParameterReference;
import org.cadixdev.bombe.type.reference.MethodReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends MemberMapping<MethodReference> {

    private final Map<MethodParameterReference, MethodParameterMapping> paramMappings = new HashMap<>();

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param ref A reference to the mapped method
     * @param deobfName The deobfuscated name of the method
     * @param propagate Whether to propagate this mapping to super- and
     *     sub-classes
     */
    public MethodMapping(ClassMapping<?> parent, MethodReference ref, String deobfName, boolean propagate) {
        super(parent, ref, deobfName);
        parent.addMethodMapping(this, propagate);
    }

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param ref A reference to the mapped method
     * @param deobfName The deobfuscated name of the method
     */
    public MethodMapping(ClassMapping<?> parent, MethodReference ref, String deobfName) {
        this(parent, ref, deobfName, true);
    }

    public void initialize(boolean propagate) {
        this.setDeobfuscatedName(getDeobfuscatedName(), propagate);
    }

    /**
     * Gets a clone of the {@link MethodParameterMapping}s.
     *
     * @return A clone of the {@link MethodParameterMapping}s
     */
    public Map<MethodParameterReference, MethodParameterMapping> getParamMappings() {
        return Collections.unmodifiableMap(this.paramMappings);
    }

    /**
     * Adds the given {@link MethodParameterMapping} to this {@link ClassMapping}.
     *
     * @param mapping   The {@link MethodParameterMapping} to add
     * @param propagate Whether to propagate this mapping to super- and
     *                  sub-classes
     */
    void addParamMapping(MethodParameterMapping mapping, boolean propagate) {
        mapping.initialize(propagate);
        paramMappings.put(mapping.getReference(), mapping);
    }

    public void removeParamMapping(MethodParameterReference paramRef) {
        paramMappings.remove(paramRef);
    }

    /**
     * Returns the deobfuscated {@link MethodDescriptor} of this method.
     *
     * @return The deobfuscated {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getDeobfuscatedDescriptor() {
        return MappingsHelper.deobfuscate(getParent().getContext(), getReference().getSignature().getDescriptor());
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String deobf, boolean propagate) {
        super.setDeobfuscatedName(deobf);

        if (propagate && !IndexedClass.INDEXED_CLASSES.isEmpty()) {
            for (ClassReference clazz : HierarchyHelper.getClassesInHierarchy(ref.getOwningClass(), ref.getSignature())) {
                if (clazz.equals(ref.getOwningClass())) {
                    continue;
                }

                //TODO: moving this logic soon
                /*ClassMapping<?> cm = MappingsHelper.getOrCreateClassMapping(getContext(), clazz);
                if (cm.getMethodMappings().containsKey(ref.getSignature())) {
                    cm.getMethodMappings().get(ref.getSignature()).setDeobfuscatedName(deobf, false);
                } else {
                    new MethodMapping(cm, ref, deobf, false);
                }*/
            }
        }

        //TODO: moving this logic soon
        /*Main.getLoadedJar().getClass(getParent().getFullObfuscatedName()).get()
                .getCurrentMethods().put(sig, getObfuscatedName().equals(getDeobfuscatedName()) ? sig
                : new MethodSignature(getDeobfuscatedName(), sig.getDescriptor()));*/
    }

    @Override
    public String toString() {
        return "{"
                + "ref=" + this.getReference() + ";"
                + "deobfName=" + this.getDeobfuscatedName() + ";"
                + "}";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MethodMapping)) {
            return false;
        }
        final MethodMapping that = (MethodMapping) obj;
        return Objects.equals(this.ref, that.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.ref);
    }

}
