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
package blue.lapis.nocturne.test.mapping.model.attribute;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import blue.lapis.nocturne.mapping.model.attribute.MethodSignature;
import blue.lapis.nocturne.mapping.model.attribute.Primitive;
import blue.lapis.nocturne.mapping.model.attribute.Type;

import org.junit.Test;

/**
 * Unit tests related to the {@link MethodSignature} class.
 */
public class MethodSignatureTest {

    @Test
    public void testPrimitiveSignature() {
        String sig = "(IDZ)V";
        MethodSignature ms = new MethodSignature(sig);
        assertArrayEquals(
                new Type[] {
                        new Type(Primitive.INT, 0),
                        new Type(Primitive.DOUBLE, 0),
                        new Type(Primitive.BOOLEAN, 0)
                },
                ms.getParamTypes());
        assertEquals(new Type(Primitive.VOID, 0), ms.getReturnType());
    }

    @Test
    public void testArraySignature() {
        String sig = "(I[[D[Z)V";
        MethodSignature ms = new MethodSignature(sig);
        assertArrayEquals(
                new Type[] {
                        new Type(Primitive.INT, 0),
                        new Type(Primitive.DOUBLE, 2),
                        new Type(Primitive.BOOLEAN, 1)
                },
                ms.getParamTypes());
        assertEquals(new Type(Primitive.VOID, 0), ms.getReturnType());
    }

    @Test
    public void testClassSignature() {
        String c1 = "java/util/List";
        String c2 = "java/lang/String";
        String sig = "(BL" + c1 + ";I)L" + c2 + ";";
        MethodSignature ms = new MethodSignature(sig);
        assertArrayEquals(new Type[] {new Type(Primitive.BYTE, 0), new Type(c1, 0), new Type(Primitive.INT, 0)},
                ms.getParamTypes());
        assertEquals(new Type(c2, 0), ms.getReturnType());
    }

}
