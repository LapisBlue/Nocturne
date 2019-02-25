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

package blue.lapis.nocturne.test.mapping.io.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.io.JarLoader;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Primitive;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.processor.index.model.signature.FieldSignature;
import blue.lapis.nocturne.processor.index.model.signature.MethodSignature;

import java.io.IOException;

/**
 * Unit tests related to the {@link SrgReader}.
 */
class ReaderTestHelper {

    private static final String EXAMPLE_PACKAGE = "com/example/project";

    private MappingContext mappings;

    ReaderTestHelper(MappingContext mappings) {
        this.mappings = mappings;
    }

    static void loadMain() throws IOException {
        new Main(true);
        Main.setLoadedJar(JarLoader.loadJar("test.jar", ReaderTestHelper.class.getResourceAsStream("/test.jar")));
    }

    void classTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");
        assertEquals("a", mapping.getObfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example", mapping.getDeobfuscatedName());
    }

    void innerClassTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        assertTrue(mapping.getInnerClassMappings().containsKey("b"));
        InnerClassMapping inner = mapping.getInnerClassMappings().get("b");

        assertEquals("b", inner.getObfuscatedName());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example$Inner", inner.getFullDeobfuscatedName());
    }

    void innerClassWithoutParentMappingTest() {
        assertTrue(mappings.getMappings().containsKey("b"));
        ClassMapping mapping = mappings.getMappings().get("b");

        assertTrue(mapping.getInnerClassMappings().containsKey("a"));
        InnerClassMapping inner = mapping.getInnerClassMappings().get("a");

        assertEquals("a", inner.getObfuscatedName());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals("b$a", inner.getFullObfuscatedName());
        assertEquals("com/example/project/Another$Inner", inner.getFullDeobfuscatedName());
    }

    void nestedInnerClassWithoutParentMappingTest() {
        assertTrue(mappings.getMappings().containsKey("b"));
        ClassMapping mapping = mappings.getMappings().get("b");

        assertTrue(mapping.getInnerClassMappings().containsKey("a"));
        InnerClassMapping inner = mapping.getInnerClassMappings().get("a");
        assertTrue(inner.getInnerClassMappings().containsKey("c"));
        InnerClassMapping deeper = inner.getInnerClassMappings().get("c");

        assertEquals("c", deeper.getObfuscatedName());
        assertEquals("Deeper", deeper.getDeobfuscatedName());
        assertEquals("b$a$c", deeper.getFullObfuscatedName());
        assertEquals("com/example/project/Another$Inner$Deeper", deeper.getFullDeobfuscatedName());
    }

    void fieldTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        FieldSignature aSig = new FieldSignature("a", Type.fromString("I"));

        assertTrue(mapping.getFieldMappings().containsKey(aSig));
        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("a", fieldMapping.getObfuscatedName());
        assertEquals("someField", fieldMapping.getDeobfuscatedName());
    }

    void fieldInnerClassTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        assertTrue(mappings.getMappings().get("a").getInnerClassMappings().containsKey("b"));

        ClassMapping mapping = mappings.getMappings().get("a").getInnerClassMappings().get("b");

        FieldSignature aSig = new FieldSignature("a", Type.fromString("I"));

        assertTrue(mapping.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("a", fieldMapping.getObfuscatedName());
        assertEquals("someInnerField", fieldMapping.getDeobfuscatedName());
    }

    void fieldNestedInnerClassTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        assertTrue(mappings.getMappings().get("a").getInnerClassMappings().containsKey("b"));
        ClassMapping inner = mappings.getMappings().get("a").getInnerClassMappings().get("b");
        assertTrue(inner.getInnerClassMappings().containsKey("c"));
        ClassMapping deeper = inner.getInnerClassMappings().get("c");

        FieldSignature aSig = new FieldSignature("a", Type.fromString("I"));

        assertTrue(deeper.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = deeper.getFieldMappings().get(aSig);
        assertEquals("a", fieldMapping.getObfuscatedName());
        assertEquals("someDeeperField", fieldMapping.getDeobfuscatedName());
    }

    void methodTest() {
        assertTrue(mappings.getMappings().containsKey("a"));
        ClassMapping mapping = mappings.getMappings().get("a");

        MethodSignature aSig = new MethodSignature("a", MethodDescriptor.fromString("(ILa;I)La;"));
        assertTrue(mapping.getMethodMappings().containsKey(aSig));

        MethodMapping methodMapping = mapping.getMethodMappings().get(aSig);
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

    void partialDeobfuscationTest() {
        assertEquals("com/example/project/Example$c", ClassMapping.deobfuscate(mappings, "a$c"));
    }

}
