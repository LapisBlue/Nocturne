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
package blue.lapis.nocturne.test.mapping.io.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Primitive;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;

import jdk.nashorn.api.scripting.URLReader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;

/**
 * Unit tests related to the {@link SrgReader}.
 */
public class SrgReaderTest {

    private static final String EXAMPLE_PACKAGE = "com/example/project";

    private static MappingContext mappings;

    @BeforeClass
    public static void initialize() {
        SrgReader reader
                = new SrgReader(new BufferedReader(new URLReader(ClassLoader.getSystemResource("example.srg"))));
        mappings = reader.read();
    }

    @Test
    public void classTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");
        assertEquals("a", mapping.getObfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example", mapping.getDeobfuscatedName());
    }

    @Test
    public void innerClassTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        assertTrue(mapping.getInnerClassMappings().containsKey("b"));
        InnerClassMapping inner = mapping.getInnerClassMappings().get("b");

        assertEquals("b", inner.getObfuscatedName());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example$Inner", inner.getFullDeobfuscatedName());
    }

    @Test
    public void innerClassWithoutParentMappingTest() {
        assertTrue(mappings.getMappings().containsKey("b"));
        ClassMapping mapping = mappings.getMappings().get("b");

        assertTrue(mapping.getInnerClassMappings().containsKey("a"));
        InnerClassMapping inner = mapping.getInnerClassMappings().get("a");

        assertEquals("a", inner.getObfuscatedName());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals("b$a", inner.getFullObfuscatedName());
        assertEquals("b$Inner", inner.getFullDeobfuscatedName());
    }

    @Test
    public void fieldTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        assertTrue(mapping.getFieldMappings().containsKey("a"));
        FieldMapping fieldMapping = mapping.getFieldMappings().get("a");
        assertEquals("a", fieldMapping.getObfuscatedName());
        assertEquals("someField", fieldMapping.getDeobfuscatedName());
    }

    @Test
    public void fieldInnerClassTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        assertTrue(mappings.getMappings().get("a").getInnerClassMappings().containsKey("b"));

        ClassMapping mapping = mappings.getMappings().get("a").getInnerClassMappings().get("b");
        assertTrue(mapping.getFieldMappings().containsKey("a"));

        FieldMapping fieldMapping = mapping.getFieldMappings().get("a");
        assertEquals("a", fieldMapping.getObfuscatedName());
        assertEquals("someInnerField", fieldMapping.getDeobfuscatedName());
    }

    @Test
    public void methodTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        assertTrue(mapping.getMethodMappings().containsKey("a(ILa;I)La;"));

        MethodMapping methodMapping = mapping.getMethodMappings().get("a(ILa;I)La;");
        assertEquals("a", methodMapping.getObfuscatedName());
        assertEquals("someMethod", methodMapping.getDeobfuscatedName());
        assertArrayEquals(
                new Type[]{
                        new Type(Primitive.INT, 0),
                        new Type("a", 0),
                        new Type(Primitive.INT, 0)
                },
                methodMapping.getObfuscatedDescriptor().getParamTypes());
        assertEquals(new Type("a", 0), methodMapping.getObfuscatedDescriptor().getReturnType());

        MethodDescriptor deobfSig = methodMapping.getDeobfuscatedDescriptor();
        assertArrayEquals(
                new Type[]{
                        new Type(Primitive.INT, 0),
                        new Type(EXAMPLE_PACKAGE + "/Example", 0),
                        new Type(Primitive.INT, 0)
                },
                deobfSig.getParamTypes()
        );
        assertEquals(new Type(EXAMPLE_PACKAGE + "/Example", 0), deobfSig.getReturnType());
    }

    @Test
    public void partialDeobfuscationTest() {
        assertEquals("com/example/project/Example$c", ClassMapping.deobfuscate(mappings, "a$c"));
    }

}
