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

import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.model.attribute.MethodSignature;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends Mapping implements ClassComponent {

    private final ClassMapping parent;
    private final MethodSignature sig;

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the method
     * @param deobfName The deobfuscated name of the method
     * @param signature The (obfuscated) {@link MethodSignature} of the method
     */
    public MethodMapping(ClassMapping parent, String obfName, String deobfName, MethodSignature signature) {
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
     * Returns the {@link MethodSignature} of this method.
     *
     * @return The {@link MethodSignature} of this method
     */
    public MethodSignature getSignature() {
        return sig;
    }

    /**
     * Returns the deobfuscated {@link MethodSignature} of this method.
     *
     * @param mappingSet The {@link MappingSet} to use for obtaining
     *     deobfuscation mappings
     * @return The deobfuscated {@link MethodSignature} of this method
     */
    public MethodSignature getDeobfuscatedSignature(MappingSet mappingSet) {
        return getSignature().deobfuscate(mappingSet);
    }

}
