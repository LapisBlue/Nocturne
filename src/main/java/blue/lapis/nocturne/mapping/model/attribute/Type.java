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
package blue.lapis.nocturne.mapping.model.attribute;

import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.io.reader.MappingsReader;

import com.google.common.base.Preconditions;

/**
 * Represents a data type.
 */
public class Type {

    private final Primitive prim;
    private final String clazz;

    /**
     * Constructs a new {@link Type} of the given {@link Primitive} type.
     *
     * @param primitive The {@link Primitive} representing the new {@link Type}
     */
    public Type(Primitive primitive) {
        Preconditions.checkArgument(primitive != null);

        this.prim = primitive;
        this.clazz = null;
    }

    /**
     * Constructs a new {@link Type} of the given class name.
     *
     * @param className The class name representing the new {@link Type}
     */
    public Type(String className) {
        Preconditions.checkArgument(className != null);

        this.clazz = className;
        this.prim = null;
    }

    /**
     * Returns whether this {@link Type} is primitive.
     *
     * @return Whether this {@link Type} is primitive
     */
    public boolean isPrimitive() {
        return prim != null;
    }

    /**
     * Gets this {@link Type} as a {@link Primitive}.
     *
     * @return The {@link Primitive} corresponding to this {@link Type}
     * @throws IllegalStateException If this {@link Type} is not primitive
     */
    public Primitive asPrimitive() throws IllegalStateException {
        Preconditions.checkState(prim != null, "Cannot get class type as primitive");
        return prim;
    }

    /**
     * Gets the name of the class represented by this {@link Type}.
     *
     * @return The name of the class represented by this {@link Type}
     * @throws IllegalStateException If this {@link Type} is primitive
     */
    public String getClassName() throws IllegalStateException {
        Preconditions.checkState(clazz != null, "Cannot get primitive type as class");
        return clazz;
    }

    /**
     * Attempts to get the deobfuscated name of the class represented by this
     * {@link Type}.
     *
     * @return The deobfuscated name of the class represented by this {@link Type}
     * @throws IllegalStateException If this {@link Type} is primitive
     */
    private String getDeobfuscatedClassName(MappingSet mappingSet) throws IllegalStateException {
        Preconditions.checkState(!isPrimitive(), "Cannot get primitive type as class");

        return MappingsReader.getOrCreateClassMapping(mappingSet, getClassName()).getDeobfuscatedName();
    }

    /**
     * Attempts to deobfuscate this {@link Type}.
     *
     * @param mappingSet The {@link MappingSet} to obtain the deobfuscated name
     *     from
     * @return The deobfuscated {@link Type}
     */
    public Type deobfuscate(MappingSet mappingSet) {
        return prim != null ? this : new Type(getDeobfuscatedClassName(mappingSet));
    }

    @Override
    public String toString() {
        if (isPrimitive()) {
            return "Type:Primitive(" + asPrimitive().name() + ")";
        } else {
            return "Type:Class(" + getClassName() + ")";
        }
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Type)) {
            return false;
        }

        Type type = (Type) otherObject;
        if (this.isPrimitive() != type.isPrimitive()) {
            return false;
        }

        if (this.isPrimitive()) {
            return this.asPrimitive() == type.asPrimitive();
        } else {
            return this.getClassName().equals(type.getClassName());
        }
    }

}
