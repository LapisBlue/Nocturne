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
package blue.lapis.nocturne.mapping.model.attribute;

/**
 * Represents a method signature consisting of parameter {@link Type}s and a
 * return {@link Type}.
 */
public class MethodSignature {

    private final Type returnType;
    private final Type[] paramTypes;

    /**
     * Constructs a new {@link MethodSignature} with the given return and
     * parameter {@link Type}s.
     *
     * @param returnType The return {@link Type} of the new
     *     {@link MethodSignature}
     * @param paramTypes The parameter {@link Type}s of the new
     *     {@link MethodSignature}
     */
    public MethodSignature(Type returnType, Type... paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    /**
     * Gets the return {@link Type} of this {@link MethodSignature}.
     *
     * @return The return {@link Type} of this {@link MethodSignature}
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Gets the parameter {@link Type}s of this {@link MethodSignature}.
     *
     * @return The parameter {@link Type}s of this {@link MethodSignature}
     */
    public Type[] getParamTypes() {
        return paramTypes;
    }

    /**
     * Attempts to deobfuscate this {@link MethodSignature}.
     *
     * @return The deobfuscated {@link MethodSignature}
     */
    public MethodSignature deobfuscate() {
        Type[] deobfParams = new Type[getParamTypes().length];
        for (int i = 0; i < getParamTypes().length; i++) {
            deobfParams[i] = getParamTypes()[i].deobfuscate();
        }

        return new MethodSignature(getReturnType().deobfuscate(), deobfParams);
    }

}
