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
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.signature.FieldSignature;

/**
 * Represents a {@link Mapping} for a field.
 */
public class FieldMapping extends MemberMapping {

    private final ClassMapping parent;
    private final FieldSignature sig;

    /**
     * Constructs a new {@link FieldMapping} with the given parameters.
     *
     * @param parent    The parent {@link ClassMapping}
     * @param sig       The obfuscated signature of the field
     * @param deobfName The deobfuscated name of the field
     */
    public FieldMapping(ClassMapping parent, FieldSignature sig, String deobfName) {
        super(parent, sig.getName(), deobfName);
        this.parent = parent;
        this.sig = sig;

        parent.addFieldMapping(this);
    }

    /**
     * Returns the {@link FieldType} of this field.
     *
     * @return The {@link FieldType} of this field
     */
    public FieldType getObfuscatedType() {
        return sig.getType().get(); // TODO: Nocturne's reader guarantees the type is present
    }

    /**
     * Returns the deobfuscated {@link FieldType} of this field.
     *
     * @return The deobfuscated {@link FieldType} of this field
     */
    public FieldType getDeobfuscatedType() {
        return MappingsHelper.deobfuscate(getParent().getContext(), getObfuscatedType());
    }

    @Override
    public void setDeobfuscatedName(String deobf) {
        super.setDeobfuscatedName(deobf);

        Main.getLoadedJar().getClass(getParent().getFullObfuscatedName()).get()
                .getCurrentFields().put(sig, getObfuscatedName().equals(getDeobfuscatedName()) ? sig
                : new FieldSignature(getDeobfuscatedName(), sig.getType().orElse(null))); // TODO: Handle better
    }

    @Override
    public FieldSignature getSignature() {
        return sig;
    }

    @Override
    protected SelectableMember.MemberKey getMemberKey() {
        return new SelectableMember.MemberKey(MemberType.FIELD, getQualifiedName(), sig.getType().toString());
    }

    private String getQualifiedName() {
        return (getParent() instanceof InnerClassMapping
                ? getParent().getFullObfuscatedName()
                : getParent().getObfuscatedName())
                + CLASS_PATH_SEPARATOR_CHAR + getObfuscatedName();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof FieldMapping;
    }

}
