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

import org.cadixdev.bombe.type.reference.MethodParameterReference;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents a {@link Mapping} for arguments.
 */
public class MethodParameterMapping extends Mapping<MethodParameterReference> {

    private final MethodMapping parent;

    /**
     * Constructs a new {@link MethodParameterMapping} with the given parameters.
     *
     * @param parent The parent method mapping
     * @param ref A reference to the mapped parameter
     * @param deobf The deobfuscated name of the descriptor, if any
     * @param propagate Whether to propagate this mapping to super- and
     *                  sub-classes
     */
    public MethodParameterMapping(MethodMapping parent, MethodParameterReference ref, @Nullable String deobf,
            boolean propagate) {
        super(ref, deobf != null ? deobf : "param_" + ref.getParameterIndex());
        this.parent = parent;

        this.parent.addParamMapping(this, propagate);
    }

    public void initialize(boolean propagate) {
        this.setDeobfuscatedName(getDeobfuscatedName(), propagate);
    }

    /**
     * Gets the parent method mapping of this argument mapping.
     *
     * @return The parent mapping
     */
    public MethodMapping getParent() {
        return this.parent;
    }

    @Override
    public MappingContext getContext() {
        return this.getParent().getContext();
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String deobf, boolean propagate) {
        super.setDeobfuscatedName(deobf);

        // TODO: propagate
    }

    @Override
    public String toString() {
        return "{"
                + "ref=" + this.getReference() + ";"
                + "deobfName=" + this.getDeobfuscatedName() + ";"
                + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.ref);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodParameterMapping)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final MethodParameterMapping that = (MethodParameterMapping) obj;
        return Objects.equals(this.ref, that.ref);
    }

}
