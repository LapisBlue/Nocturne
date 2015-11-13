/*
 * Shroud
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
package blue.lapis.shroud.mapping.model;

import blue.lapis.shroud.mapping.MappingSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a {@link Mapping} for a class.
 */
public abstract class ClassMapping extends Mapping {

    private final Map<String, FieldMapping> fieldMappings = new HashMap<>();
    private final Map<String, MethodMapping> methodMappings = new HashMap<>();
    private final Map<String, InnerClassMapping> innerClassMappings = new HashMap<>();

    /**
     * Constructs a new {@link ClassMapping} with the given parameters.
     *
     * @param obfName The obfuscated name of the class
     * @param deobfName The deobfuscated name of the class
     */
    protected ClassMapping(String obfName, String deobfName) {
        super(obfName, deobfName);
    }

    /**
     * Adds the given {@link FieldMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link FieldMapping} to add
     */
    void addFieldMapping(FieldMapping mapping) {
        fieldMappings.put(mapping.getObfuscatedName(), mapping);
    }

    /**
     * Adds the given {@link MethodMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link MethodMapping} to add
     */
    void addMethodMapping(MethodMapping mapping) {
        methodMappings.put(mapping.getObfuscatedName(), mapping);
    }

    /**
     * Adds the given {@link InnerClassMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link InnerClassMapping} to add
     */
    void addInnerClassMapping(InnerClassMapping mapping) {
        innerClassMappings.put(mapping.getObfuscatedName(), mapping);
    }

}
