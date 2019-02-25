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

package blue.lapis.nocturne.processor.constantpool.model.structure;

import blue.lapis.nocturne.util.helper.ByteHelper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Represents a Utf8 structure.
 */
public class Utf8Structure extends ConstantStructure {

    private String str;

    public Utf8Structure(byte[] bytes) {
        super(bytes);
        assert bytes.length >= 3;
        int length = ByteHelper.asUshort(bytes[1], bytes[2]);
        assert bytes.length == length + 3;
        byte[] strBytes = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, strBytes, 0, strBytes.length);
        str = new String(strBytes, StandardCharsets.UTF_8);
    }

    public Utf8Structure(String str) {
        super(bytesFromStr(str));
        this.str = str;
    }

    public String asString() {
        return str;
    }

    private static byte[] bytesFromStr(String str) {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.allocate(strBytes.length + 3);
        bb.put(StructureType.UTF_8.getTag());
        bb.putShort((short) strBytes.length);
        bb.put(strBytes);
        return bb.array();
    }

}
