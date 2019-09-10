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
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import org.cadixdev.bombe.type.BaseType;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.reference.FieldReference;
import org.cadixdev.bombe.type.reference.InnerClassReference;
import org.cadixdev.bombe.type.reference.TopLevelClassReference;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

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
        TopLevelClassReference refA = new TopLevelClassReference("a");

        assertTrue(mappings.getMappings().containsKey(refA));
        ClassMapping<?> mapping = mappings.getMappings().get(refA);
        assertEquals(refA.toJvmsIdentifier(), mapping.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Example", mapping.getDeobfuscatedName());
    }

    void innerClassTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");
        InnerClassReference refB = refA.getInnerClass("b");

        assertTrue(mappings.getMappings().containsKey(refA));
        ClassMapping<?> mapping = mappings.getMappings().get(refA);

        assertTrue(mapping.getInnerClassMappings().containsKey(refB));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(refB);

        assertEquals("a$b", inner.getReference().toJvmsIdentifier());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example$Inner", inner.getFullDeobfuscatedName());
    }

    void innerClassWithoutParentMappingTest() {
        TopLevelClassReference refB = new TopLevelClassReference("b");
        InnerClassReference refA = refB.getInnerClass("a");

        assertTrue(mappings.getMappings().containsKey(refB));
        ClassMapping<?> mapping = mappings.getMappings().get(refB);

        assertTrue(mapping.getInnerClassMappings().containsKey(refA));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(refA);

        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals("b$a", inner.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Another$Inner", inner.getFullDeobfuscatedName());
    }

    void nestedInnerClassWithoutParentMappingTest() {
        TopLevelClassReference refB = new TopLevelClassReference("b");
        InnerClassReference refA = refB.getInnerClass("a");
        InnerClassReference refC = refA.getInnerClass("c");

        assertTrue(mappings.getMappings().containsKey(refB));
        ClassMapping<?> mapping = mappings.getMappings().get(refB);

        assertTrue(mapping.getInnerClassMappings().containsKey(refA));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(refA);
        assertTrue(inner.getInnerClassMappings().containsKey(refC));
        InnerClassMapping deeper = inner.getInnerClassMappings().get(refC);

        assertEquals("Deeper", deeper.getDeobfuscatedName());
        assertEquals("b$a$c", deeper.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Another$Inner$Deeper", deeper.getFullDeobfuscatedName());
    }

    void fieldTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");
        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(mappings.getMappings().containsKey(refA));
        ClassMapping<?> mapping = mappings.getMappings().get(refA);

        assertTrue(mapping.getFieldMappings().containsKey(aSig));
        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("someField", fieldMapping.getDeobfuscatedName());
    }

    void fieldInnerClassTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");
        InnerClassReference refB = refA.getInnerClass("b");
        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(mappings.getMappings().containsKey(refA));
        assertTrue(mappings.getMappings().get(refA).getInnerClassMappings().containsKey(refB));

        ClassMapping<?> mapping = mappings.getMappings().get(refA).getInnerClassMappings().get(refB);

        assertTrue(mapping.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("someInnerField", fieldMapping.getDeobfuscatedName());
    }

    void fieldNestedInnerClassTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");
        InnerClassReference refB = refA.getInnerClass("b");
        InnerClassReference refC = refB.getInnerClass("c");

        assertTrue(mappings.getMappings().containsKey(refA));
        assertTrue(mappings.getMappings().get(refA).getInnerClassMappings().containsKey(refB));
        ClassMapping<?> inner = mappings.getMappings().get(refA).getInnerClassMappings().get(refB);
        assertTrue(inner.getInnerClassMappings().containsKey(refC));
        ClassMapping<?> deeper = inner.getInnerClassMappings().get(refC);

        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(deeper.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = deeper.getFieldMappings().get(aSig);
        assertEquals("someDeeperField", fieldMapping.getDeobfuscatedName());
    }

    void methodTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");

        assertTrue(mappings.getMappings().containsKey(refA));
        ClassMapping<?> mapping = mappings.getMappings().get(refA);

        MethodSignature aSig = new MethodSignature("a", MethodDescriptor.of("(ILa;I)La;"));
        assertTrue(mapping.getMethodMappings().containsKey(aSig));

        MethodMapping methodMapping = mapping.getMethodMappings().get(aSig);
        assertEquals("someMethod", methodMapping.getDeobfuscatedName());
        assertArrayEquals(
                new FieldType[]{
                        BaseType.INT,
                        refA.getClassType(),
                        BaseType.INT
                },
                methodMapping.getReference().getSignature().getDescriptor().getParamTypes().toArray()
        );
        assertEquals(refA.getClassType(), methodMapping.getReference().getSignature().getDescriptor().getReturnType());

        MethodDescriptor deobfSig = methodMapping.getDeobfuscatedDescriptor();
        assertArrayEquals(
                new FieldType[]{
                        BaseType.INT,
                        new ObjectType(EXAMPLE_PACKAGE + "/Example"),
                        BaseType.INT
                },
                deobfSig.getParamTypes().toArray()
        );
        assertEquals(new ObjectType(EXAMPLE_PACKAGE + "/Example"), deobfSig.getReturnType());
    }

    void partialDeobfuscationTest() {
        TopLevelClassReference refA = new TopLevelClassReference("a");
        InnerClassReference refC = refA.getInnerClass("c");
        //assertEquals("com/example/project/Example$c", ClassMapping.deobfuscate(mappings, refC));
    }

}
