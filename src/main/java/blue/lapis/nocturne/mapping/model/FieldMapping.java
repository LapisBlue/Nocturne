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

import blue.lapis.nocturne.util.helper.MappingsHelper;

import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.reference.FieldReference;

import javax.annotation.Nullable;

/**
 * Represents a {@link Mapping} for a field.
 */
public class FieldMapping extends MemberMapping<FieldReference> {

    /**
     * Constructs a new {@link FieldMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param ref A reference to the mapped field
     * @param deobfName The deobfuscated name of the field
     */
    public FieldMapping(ClassMapping<?> parent, FieldReference ref, @Nullable String deobfName) {
        super(parent, ref, deobfName);

        parent.addFieldMapping(this);
    }

    /**
     * Returns the deobfuscated {@link FieldType} of this field.
     *
     * @return The deobfuscated {@link FieldType} of this field
     */
    public FieldType getDeobfuscatedType() {
        return MappingsHelper.deobfuscateField(getParent().getContext(), ref.getSignature().getType().orElse(null));
    }

    @Override
    public void setDeobfuscatedName(@Nullable String deobf) {
        super.setDeobfuscatedName(deobf);

        //TODO: moving this logic soon
        /*Main.getLoadedJar().getClass(getParent().getFullObfuscatedName()).get()
                .getCurrentFields().put(sig, getObfuscatedName().equals(getDeobfuscatedName()) ? sig
                : new FieldSignature(getDeobfuscatedName(), sig.getType().orElse(null)));*/
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof FieldMapping;
    }

}
