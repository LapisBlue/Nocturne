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
package blue.lapis.nocturne.mapping.model;

import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.mapping.MappingContext;

import java.util.List;

/**
 * Represents a mapping for a class member.
 */
public abstract class MemberMapping extends Mapping implements IMemberMapping {

    private final ClassMapping parent;

    /**
     * Constructs a new mapping with the given parameters.
     *
     * @param obfName   The obfuscated name of the mapped member
     * @param deobfName The deobfuscated name of the mapped member
     */
    protected MemberMapping(ClassMapping parent, String obfName, String deobfName) {
        super(obfName, deobfName);
        this.parent = parent;
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    @Override
    public void setDeobfuscatedName(String name) {
        super.setDeobfuscatedName(name);

        List<SelectableMember> memberList = SelectableMember.MEMBERS.get(getMemberKey());
        if (memberList == null) {
            return;
        }
        memberList.forEach(member -> {
            member.setText(name);
            member.setDeobfuscated(!name.equals(member.getName()));
        });
    }

    @Override
    public MappingContext getContext() {
        return getParent().getContext();
    }

}
