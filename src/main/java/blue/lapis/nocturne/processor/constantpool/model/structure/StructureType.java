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

package blue.lapis.nocturne.processor.constantpool.model.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a particular structure type contained by the constant pool.
 */
public enum StructureType {

    UTF_8(0x01, -1),
    INTEGER(0x03, 4),
    FLOAT(0x04, 4),
    LONG(0x05, 8),
    DOUBLE(0x06, 8),
    CLASS(0x07, 2),
    STRING(0x08, 2),
    FIELDREF(0x09, 4),
    METHODREF(0x0A, 4),
    INTERFACE_METHODREF(0x0B, 4),
    NAME_AND_TYPE(0x0C, 4),
    METHOD_HANDLE(0x0F, 3),
    METHOD_TYPE(0x10, 2),
    INVOKE_DYNAMIC(0x12, 4),
    DUMMY(0xFF, -1);

    private static Map<Byte, StructureType> types;

    private final byte tag;
    private final int length;

    StructureType(int tag, int length) {
        this.tag = (byte) tag;
        this.length = length;
        register((byte) tag);
    }

    /**
     * Adds this {@link StructureType} to the registry with the given tag.
     *
     * @param tag The tag to associated with this {@link StructureType}
     */
    private void register(byte tag) {
        if (types == null) {
            types = new HashMap<>();
        }
        types.put(tag, this);
    }

    /**
     * Gets the tag internally associated with this structure type.
     *
     * @return The tag internally associated with this structure type
     */
    public byte getTag() {
        return this.tag;
    }

    /**
     * Gets the expected length of structures of this type.
     *
     * @return The expected length of structures of this type
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Gets the {@link StructureType} associated with the given byte tag.
     *
     * @param tag The tag to get a {@link StructureType} for
     * @return The {@link StructureType} associated with the given byte tag
     */
    public static StructureType fromTag(byte tag) {
        if (!types.containsKey(tag)) {
            throw new IllegalArgumentException("No such constant structure with tag " + tag);
        }
        return types.get(tag);
    }

}
