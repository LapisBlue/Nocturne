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

import blue.lapis.nocturne.mapping.MappingContext;

import org.cadixdev.bombe.type.reference.QualifiedReference;

import java.util.Objects;

/**
 * Represents a single obfuscation mapping for a particular member.
 */
public abstract class Mapping<T extends QualifiedReference> {

    protected final T ref;
    protected String deobf;
    protected boolean adHoc;

    /**
     * Constructs a new mapping with the given parameters.
     *
     * @param reference A {@link QualifiedReference reference} to the item
     * @param deobfName The deobfuscated name of the mapped item
     */
    protected Mapping(T reference, String deobfName) {
        this.ref = reference;
        this.deobf = deobfName;
    }

    public void initialize() {
        this.setDeobfuscatedName(getDeobfuscatedName());
    }

    /**
     * Returns a reference to the mapped item
     *
     * @return A reference to the mapped item
     */
    public T getReference() {
        return ref;
    }

    /**
     * Returns the deobfuscated name of this {@link Mapping}.
     *
     * @return The deobfuscated name of this {@link Mapping}
     */
    public String getDeobfuscatedName() {
        return deobf;
    }

    /**
     * Sets the deobfuscated name of this {@link Mapping}.
     *
     * @param name The new deobfuscated name of this {@link Mapping}
     */
    public void setDeobfuscatedName(String name) {
        if (this.deobf.equals(name)) {
            this.setAdHoc(false);
        }
        this.deobf = name;
        getContext().setDirty(true);
    }

    /**
     * Gets whether this mapping is ad hoc, for the purpose of on-demand
     * deobfuscation toggling.
     *
     * @return Whether this mapping is ad hoc
     */
    public boolean isAdHoc() {
        return adHoc;
    }

    /**
     * Sets whether this mapping is ad hoc, for the purpose of on-demand
     * deobfuscation toggling.
     *
     * @param adHoc Whether this mapping is ad hoc
     */
    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc;
    }

    /**
     * Gets the {@link MappingContext} which owns this {@link Mapping}.
     *
     * @return The {@link MappingContext} which owns this {@link Mapping}
     */
    public abstract MappingContext getContext();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mapping)) {
            return false;
        }
        final Mapping that = (Mapping) obj;
        return Objects.equals(this.ref, that.ref)
                && Objects.equals(this.deobf, that.deobf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ref, this.deobf);
    }

}
