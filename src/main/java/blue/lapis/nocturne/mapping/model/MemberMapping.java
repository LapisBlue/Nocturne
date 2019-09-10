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

import org.cadixdev.bombe.type.reference.MemberReference;

import java.util.Objects;

/**
 * Represents a mapping for a class member.
 */
public abstract class MemberMapping<T extends MemberReference<?>> extends Mapping<T> {

    private final ClassMapping<?> parent;

    /**
     * Constructs a new mapping with the given parameters.
     *
     * @param ref A reference to the mapped member
     * @param deobfName The deobfuscated name of the mapped member
     */
    protected MemberMapping(ClassMapping<?> parent, T ref, String deobfName) {
        super(ref, deobfName);
        this.parent = parent;
    }

    public ClassMapping<?> getParent() {
        return parent;
    }

    @Override
    public void setDeobfuscatedName(String name) {
        super.setDeobfuscatedName(name);

        //TODO: moving this logic soon
        /*List<SelectableMember> memberList = SelectableMember.MEMBERS.get(getMemberKey());
        if (memberList == null) {
            return;
        }
        memberList.forEach(member -> {
            member.setText(name);
            member.setDeobfuscated(!name.equals(member.getName()), true);
        });*/
    }

    @Override
    public MappingContext getContext() {
        return getParent().getContext();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MemberMapping)) {
            return false;
        }

        final MemberMapping that = (MemberMapping) obj;
        return Objects.equals(this.parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.parent);
    }

}
