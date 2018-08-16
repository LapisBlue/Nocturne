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

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.HierarchyHelper;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends MemberMapping {

    //TODO: this needs to have integers as keys. it doesn't make sense with strings.
    private final Map<String, MethodParameterMapping> argumentMappings = new HashMap<>();
    private final SelectableMember.MemberKey memberKey;
    private final MethodSignature sig;

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent    The parent {@link ClassMapping}
     * @param sig       The obfuscated signature of the method
     * @param deobfName The deobfuscated name of the method
     * @param propagate Whether to propagate this mapping to super- and
     *                  sub-classes
     */
    public MethodMapping(ClassMapping parent, MethodSignature sig, String deobfName, boolean propagate) {
        super(parent, sig.getName(), deobfName);
        this.sig = sig;
        memberKey = new SelectableMember.MemberKey(MemberType.METHOD, getQualifiedName(),
                getObfuscatedDescriptor().toString());
        parent.addMethodMapping(this, propagate);
    }

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent    The parent {@link ClassMapping}
     * @param sig       The obfuscated signature of the method
     * @param deobfName The deobfuscated name of the method
     */
    public MethodMapping(ClassMapping parent, MethodSignature sig, String deobfName) {
        this(parent, sig, deobfName, true);
    }

    public void initialize(boolean propagate) {
        this.setDeobfuscatedName(getDeobfuscatedName(), propagate);
    }

    /**
     * Gets a clone of the {@link MethodParameterMapping}s.
     *
     * @return A clone of the {@link MethodParameterMapping}s
     */
    public ImmutableMap<String, MethodParameterMapping> getParamMappings() {
        return ImmutableMap.copyOf(this.argumentMappings);
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
        argumentMappings.put(mapping.getObfuscatedName(), mapping);
    }

    public void removeParamMapping(String name) {
        argumentMappings.remove(name);
    }

    /**
     * Returns the {@link MethodDescriptor} of this method.
     *
     * @return The {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getObfuscatedDescriptor() {
        return sig.getDescriptor();
    }

    /**
     * Returns the deobfuscated {@link MethodDescriptor} of this method.
     *
     * @return The deobfuscated {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getDeobfuscatedDescriptor() {
        return MappingsHelper.deobfuscate(getParent().getContext(), getObfuscatedDescriptor());
    }

    @Override
    public MethodSignature getSignature() {
        return sig;
    }

    @Override
    protected SelectableMember.MemberKey getMemberKey() {
        return memberKey;
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
            for (String clazz : HierarchyHelper.getClassesInHierarchy(getParent().getFullObfuscatedName(), sig)) {
                if (clazz.equals(getParent().getObfuscatedName())) {
                    continue;
                }

                ClassMapping cm = MappingsHelper.getOrCreateClassMapping(getContext(), clazz);
                if (cm.getMethodMappings().containsKey(getSignature())) {
                    cm.getMethodMappings().get(getSignature()).setDeobfuscatedName(deobf, false);
                } else {
                    new MethodMapping(cm, getSignature(), deobf, false);
                }
            }
        }

        Main.getLoadedJar().getClass(getParent().getFullObfuscatedName()).get()
                .getCurrentMethods().put(sig, getObfuscatedName().equals(getDeobfuscatedName()) ? sig
                : new MethodSignature(getDeobfuscatedName(), sig.getDescriptor()));
    }

    @Override
    protected MoreObjects.ToStringHelper buildToString() {
        return super.buildToString()
                .add("signature", this.sig);
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
        return Objects.equals(this.sig, that.sig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.sig);
    }

}
