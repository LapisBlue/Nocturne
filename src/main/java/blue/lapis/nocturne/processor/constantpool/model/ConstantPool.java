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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Represents the constant pool of a class.
 */
public class ConstantPool {

    private final List<ConstantStructure> contents;
    private int length;

    /**
     * Instantiates a new {@link ConstantPool} with the given contents.
     *
     * @param contents The contents of the new {@link ConstantPool}
     * @param length   The length of the constant pool in bytes
     */
    public ConstantPool(List<ConstantStructure> contents, int length) {
        this.contents = Lists.newArrayList(contents);
        this.length = length;
    }

    /**
     * Returns the number of structures contained by this {@link ConstantPool}.
     *
     * <p>This method should not be confused with {@link ConstantPool#length()}.
     * </p>
     *
     * @return The number of structures contained by this {@link ConstantPool}
     */
    public int size() {
        return contents.size();
    }

    /**
     * Returns the length of this {@link ConstantPool} in bytes (including the
     * two leading bytes denoting the entry count).
     *
     * <p>This method should not be confused with {@link ConstantPool#size()}.
     * </p>
     *
     * @return The length of this {@link ConstantPool} in bytes
     */
    public int length() {
        return length;
    }

    /**
     * Returns the structure at the given index of this
     * <strong>1-indexed</strong> constant pool.
     *
     * @param index The index of the structure to retrieve
     * @return The retrieved structure
     * @throws IndexOutOfBoundsException If the given index is less than 1 or
     *                                   greater than the value returned by {@link ConstantPool#size()}
     */
    public ConstantStructure get(int index) throws IndexOutOfBoundsException {
        if (index < 1 || index > size()) {
            throw new IndexOutOfBoundsException("Constant pool index " + index + " out-of-bounds");
        }
        return contents.get(index - 1);
    }

    /**
     * Sets the structure at the given index of this <strong>1-indexed</strong>
     * constant pool.
     *
     * @param index     The index of the structure to set
     * @param structure The replacement structure
     * @throws IndexOutOfBoundsException If the given index is less than 1 or
     *                                   greater than the value returned by {@link ConstantPool#size()}
     */
    public void set(int index, ConstantStructure structure) throws IndexOutOfBoundsException {
        if (index < 1 || index > size()) {
            throw new IndexOutOfBoundsException("Constant pool index " + index + " out-of-bounds");
        }
        length += structure.getBytes().length - contents.get(index - 1).getBytes().length;
        contents.set(index - 1, structure);
    }

    /**
     * Adds the structure to the end of this constant pool.
     *
     * @param structure The replacement structure
     * @throws IndexOutOfBoundsException If the given index is less than 1 or
     *                                   greater than the value returned by {@link ConstantPool#size()}
     */
    public void add(ConstantStructure structure) throws IndexOutOfBoundsException {
        length += structure.getBytes().length;
        contents.add(structure);
    }

    /**
     * Returns the current contents of the constant pool.
     *
     * @return The current contents of the constant pool
     */
    public ImmutableList<ConstantStructure> getContents() {
        return ImmutableList.copyOf(contents);
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(length());
        buffer.putShort((short) (size() + 1));
        getContents().forEach(cs -> buffer.put(cs.getBytes()));
        return buffer.array();
    }

}
