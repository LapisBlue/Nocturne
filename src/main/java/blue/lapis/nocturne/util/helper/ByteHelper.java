/*
 * Nocturne
 * Copyright (c) 2015-2016, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.util.Constants.INT_UNSIGNER;
import static blue.lapis.nocturne.util.Constants.SHORT_UNSIGNER;

import java.nio.ByteBuffer;

/**
 * Static utility class for byte manipulation.
 */
public final class ByteHelper {

    public static int asUshort(byte b1, byte b2) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(new byte[] {b1, b2});
        return ((int) buffer.getShort(0)) & SHORT_UNSIGNER;
    }

    public static int asUshort(short signed) {
        return ((int) signed) & SHORT_UNSIGNER;
    }

    public static long asUint(byte b1, byte b2, byte b3, byte b4) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(new byte[] {b1, b2, b3, b4});
        return ((long) buffer.getInt(0)) & INT_UNSIGNER;
    }

    public static long asUint(int signed) {
        return ((long) signed) & INT_UNSIGNER;
    }

    public static byte[] getBytes(short s) {
        return ByteBuffer.allocate(Short.BYTES).putShort(s).array();
    }

    public static byte[] getBytes(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    public static byte[] readBytes(ByteBuffer src, int toRead) {
        byte[] arr = new byte[toRead];
        src.get(arr);
        return ByteBuffer.allocate(toRead).put(arr).array();
    }

}
