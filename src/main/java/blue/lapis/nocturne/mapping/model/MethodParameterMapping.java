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

import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.MemberType;

/**
 * Represents a {@link Mapping} for arguments.
 */
public class MethodParameterMapping extends Mapping {

    private final SelectableMember.MemberKey memberKey;
    private final MethodMapping parent;
    private final int index;

    /**
     * Constructs a new {@link MethodParameterMapping} with the given parameters.
     *
     * @param parent The parent method mapping
     * @param index The index of the argument
     * @param deobfName The deobfuscated name of the mapped argument
     * @param propagate Whether to propagate this mapping to super- and
     *     sub-classes
     */
    public MethodParameterMapping(MethodMapping parent, int index, String deobfName, boolean propagate) {
        super(deobfName, deobfName);
        this.memberKey = new SelectableMember.MemberKey(MemberType.ARG, "", ""); // TODO: Use actual values
        this.parent = parent;
        this.index = index;

        this.parent.addParamMapping(this, propagate);
    }

    public void initialize(boolean propagate) {
        this.setDeobfuscatedName(getDeobfuscatedName(), propagate);
    }

    /**
     * Gets the index of the mapped argument.
     *
     * @return The index
     */
    public int getIndex() {
        return index;
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
    protected SelectableMember.MemberKey getMemberKey() {
        return this.memberKey;
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String deobf, boolean propagate) {
        super.setDeobfuscatedName(deobf);

        // TODO: propagate
    }
}
