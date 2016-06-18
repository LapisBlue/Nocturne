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

import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.MemberType;

import java.util.Optional;

/**
 * Represents a {@link Mapping} for an inner class, i.e. a class parented by
 * another class.
 */
public class InnerClassMapping extends ClassMapping implements IMemberMapping {

    private final ClassMapping parent;

    /**
     * Constructs a new {@link InnerClassMapping} with the given parameters.
     *
     * <p>The name should not include the parent class(es), just the name of the
     * inner class itself.</p>
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the inner class
     * @param deobfName The deobfuscated name of the inner class
     */
    public InnerClassMapping(ClassMapping parent, String obfName, String deobfName) {
        super(obfName, deobfName);
        this.parent = parent;

        parent.addInnerClassMapping(this);
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    /**
     * Returns the full obfuscated name of this inner class.
     *
     * @return The full obfuscated name of this inner class
     */
    @Override
    public String getFullObfuscatedName() {
        return (parent instanceof InnerClassMapping
                ? parent.getFullObfuscatedName()
                : parent.getObfuscatedName())
                + INNER_CLASS_SEPARATOR_CHAR + getObfuscatedName();
    }

    /**
     * Returns the full deobfuscated name of this inner class.
     *
     * @return The full deobfuscated name of this inner class
     */
    @Override
    public String getFullDeobfuscatedName() {
        return (parent instanceof InnerClassMapping
                ? parent.getFullDeobfuscatedName()
                : parent.getDeobfuscatedName())
                + INNER_CLASS_SEPARATOR_CHAR + getDeobfuscatedName();
    }

    @Override
    public MappingContext getContext() {
        return getParent().getContext();
    }

    @Override
    public void setDeobfuscatedName(String deobf) {
        Optional<JarClassEntry> jarClassEntry = Main.getLoadedJar().getClass(getParent().getFullObfuscatedName());
        if (jarClassEntry.isPresent()) {
            jarClassEntry.get().getCurrentInnerClassNames().put(getObfuscatedName(), deobf);
        } else {
            // log and skip
            Main.getLogger().severe("Invalid obfuscated name: " + getParent().getFullObfuscatedName());
            return;
        }

        super.setDeobfuscatedName(deobf);
    }

    @Override
    protected SelectableMember.MemberKey getMemberKey() {
        return new SelectableMember.MemberKey(MemberType.CLASS, getFullObfuscatedName(), null);
    }

}
