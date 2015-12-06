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
package blue.lapis.nocturne.transform.constpool.structure;

/**
 * Represents a structure in the constant pool.
 */
public abstract class ConstantStructure {

    private final byte[] bytes;
    private final StructureType type;

    protected ConstantStructure(byte[] bytes) {
        assert bytes.length > 0;
        this.bytes = bytes;
        this.type = StructureType.fromTag(bytes[0]);
        assert bytes.length == type.getLength() + 1;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public StructureType getType() {
        return type;
    }

    public static ConstantStructure createConstantStructure(byte[] bytes) {
        assert bytes.length > 0;
        StructureType type = StructureType.fromTag(bytes[0]);
        switch (type) {
            case CLASS: {
                return new ClassStructure(bytes);
            }
            case FIELDREF: {
                return new FieldrefStructure(bytes);
            }
            case INTERFACE_METHODREF: {
                return new InterfaceMethodrefStructure(bytes);
            }
            case METHODREF: {
                return new MethodrefStructure(bytes);
            }
            case NAME_AND_TYPE: {
                return new NameAndTypeStructure(bytes);
            }
            case UTF_8: {
                return new Utf8Structure(bytes);
            }
            default: {
                return new IrrelevantStructure(bytes);
            }
        }
    }

}
