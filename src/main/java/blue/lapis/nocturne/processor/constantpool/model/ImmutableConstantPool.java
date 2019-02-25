/*
 * Nocturne
 * Copyright (c) 2015-2018, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.processor.constantpool.model;

import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;

import java.util.List;

/**
 * Represents the immutable constant pool of a class.
 */
public class ImmutableConstantPool extends ConstantPool {

    /**
     * Instantiates a new {@link ImmutableConstantPool} with the given contents.
     *
     * @param contents The contents of the new {@link ConstantPool}
     * @param length   The length of the constant pool in bytes
     */
    public ImmutableConstantPool(List<ConstantStructure> contents, int length) {
        super(contents, length);
    }

    /**
     * <strong>This method is not supported for {@link ImmutableConstantPool}s.
     * </strong>
     *
     * <p>Returns the structure at the given index of this
     * <strong>1-indexed</strong> constant pool.</p>
     *
     * @param index     The index of the structure to retrieve
     * @param structure The replacement structure
     * @throws UnsupportedOperationException Always
     * @throws IndexOutOfBoundsException     If the given index is less than 1 or
     *                                       greater than the value returned by {@link ConstantPool#size()}
     */
    @Override
    public void set(int index, ConstantStructure structure)
            throws UnsupportedOperationException, IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }


    /**
     * Adds the structure to the end of this constant pool.
     *
     * @param structure The replacement structure
     * @throws IndexOutOfBoundsException If the given index is less than 1 or
     *                                   greater than the value returned by {@link ConstantPool#size()}
     */
    @Override
    public void add(ConstantStructure structure) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

}
