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

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.MappingContext;

import org.cadixdev.bombe.type.reference.TopLevelClassReference;

/**
 * Represents a top-level {@link ClassMapping} (i.e. not an inner class).
 */
public class TopLevelClassMapping extends ClassMapping<TopLevelClassReference> {

    private MappingContext parent;

    /**
     * Constructs a new {@link TopLevelClassMapping} with the given parameters.
     *
     * @param parent The parent {@link MappingContext} of this
     *     {@link TopLevelClassMapping}
     * @param ref A reference to the mapped class
     * @param deobfName The deobfuscated name of the class
     */
    public TopLevelClassMapping(MappingContext parent, TopLevelClassReference ref, String deobfName) {
        super(ref, deobfName);
        this.parent = parent;
    }

    @Override
    public MappingContext getContext() {
        return parent;
    }

    public void setContext(MappingContext context) {
        this.parent = context;
    }

    @Override
    public String getFullDeobfuscatedName() {
        return getDeobfuscatedName();
    }

    @Override
    public void setDeobfuscatedName(String deobfuscatedName) {
        setDeobfuscatedName(deobfuscatedName, true);
    }

    @Override
    public void setDeobfuscatedName(String deobfuscatedName, boolean updateClassViews) {
        super.setDeobfuscatedName(deobfuscatedName, updateClassViews);

        //TODO: moving logic this soon
        /*if (CodeTab.CODE_TABS.containsKey(getObfuscatedName())) {
            CodeTab.CODE_TABS.get(getObfuscatedName())
                    .setText(CLASS_PATH_SEPARATOR_PATTERN.matcher(deobfuscatedName).replaceAll("."));
        }*/

        if (Main.getLoadedJar() != null) {
            Main.getLoadedJar().getCurrentNames().put(ref, deobfuscatedName);
        }
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TopLevelClassMapping)) {
            return false;
        }
        final TopLevelClassMapping that = (TopLevelClassMapping) obj;
        return super.equals(that);
    }

}
