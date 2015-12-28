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
package blue.lapis.nocturne.util.helper;

import blue.lapis.nocturne.util.Constants;

import java.nio.ByteBuffer;

/**
 * Static utility class for byte manipulation.
 */
public final class ByteHelper {

    public static int asUshort(byte b1, byte b2) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(new byte[] {b1, b2});
        //noinspection PointlessBitwiseExpression - IntellIJ is wrong
        return ((int) buffer.getShort(0)) & Constants.SHORT_UNSIGNER;
    }

    public static long asUint(byte b1, byte b2, byte b3, byte b4) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(new byte[] {b1, b2, b3, b4});
        //noinspection PointlessBitwiseExpression - IntellIJ is wrong
        return ((long) buffer.getInt(0)) & Constants.INT_UNSIGNER;
    }

}
