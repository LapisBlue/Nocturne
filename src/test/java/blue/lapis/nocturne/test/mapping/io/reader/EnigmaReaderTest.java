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

package blue.lapis.nocturne.test.mapping.io.reader;

import static blue.lapis.nocturne.test.mapping.io.reader.ReaderTestHelper.loadMain;

import blue.lapis.nocturne.mapping.io.reader.EnigmaReader;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;

import jdk.nashorn.api.scripting.URLReader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Unit tests related to the {@link SrgReader}.
 */
public class EnigmaReaderTest {

    private static ReaderTestHelper helper;

    @BeforeClass
    public static void initialize() throws IOException {
        loadMain();
        EnigmaReader reader
                = new EnigmaReader(new BufferedReader(new URLReader(
                        ClassLoader.getSystemResource("mappings/example.eng"))));
        helper = new ReaderTestHelper(reader.read());
    }

    @Test
    public void classTest() {
        helper.classTest();
    }

    @Test
    public void innerClassTest() {
        helper.innerClassTest();
    }

    @Test
    public void innerClassWithoutParentMappingTest() {
        helper.innerClassWithoutParentMappingTest();
    }

    @Test
    public void nestedInnerClassWithoutParentMappingTest() {
        helper.nestedInnerClassWithoutParentMappingTest();
    }

    @Test
    public void fieldTest() {
        helper.fieldTest();
    }

    @Test
    public void fieldInnerClassTest() {
        helper.fieldInnerClassTest();
    }

    @Test
    public void fieldNestedInnerClassTest() {
        helper.fieldNestedInnerClassTest();
    }

    @Test
    public void methodTest() {
        helper.methodTest();
    }

    @Test
    public void partialDeobfuscationTest() {
        helper.partialDeobfuscationTest();
    }

}
