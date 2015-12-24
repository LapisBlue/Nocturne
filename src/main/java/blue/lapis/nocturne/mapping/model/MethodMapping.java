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

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;

import blue.lapis.nocturne.gui.text.SelectableMember;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.util.MemberType;

import java.util.List;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends Mapping implements ClassComponent {

    private final ClassMapping parent;
    private final MethodDescriptor sig;

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the method
     * @param deobfName The deobfuscated name of the method
     * @param signature The (obfuscated) {@link MethodDescriptor} of the method
     */
    public MethodMapping(ClassMapping parent, String obfName, String deobfName, MethodDescriptor signature) {
        super(obfName, deobfName);
        this.parent = parent;
        this.sig = signature;

        parent.addMethodMapping(this);
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    /**
     * Returns the {@link MethodDescriptor} of this method.
     *
     * @return The {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getSignature() {
        return sig;
    }

    /**
     * Returns the deobfuscated {@link MethodDescriptor} of this method.
     *
     * @return The deobfuscated {@link MethodDescriptor} of this method
     */
    public MethodDescriptor getDeobfuscatedSignature() {
        return getSignature().deobfuscate(getParent().getContext());
    }

    @Override
    public void setDeobfuscatedName(String name) {
        super.setDeobfuscatedName(name);

        List<SelectableMember> memberList = SelectableMember.MEMBERS.get(getMemberKey());
        if (memberList == null) {
            return;
        }
        memberList.forEach(member -> member.setText(name));
    }

    @Override
    protected SelectableMember.MemberKey getMemberKey() {
        return new SelectableMember.MemberKey(MemberType.METHOD, getQualifiedName(), getSignature().toString());
    }

    private String getQualifiedName() {
        return (getParent() instanceof InnerClassMapping
                ? ((InnerClassMapping) getParent()).getFullObfuscatedName()
                : getParent().getObfuscatedName())
                + CLASS_PATH_SEPARATOR_CHAR + getObfuscatedName();
    }

}
