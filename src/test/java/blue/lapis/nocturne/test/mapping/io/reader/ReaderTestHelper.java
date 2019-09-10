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
        TopLevelClassReference ref_a = new TopLevelClassReference("a");

        assertTrue(mappings.getMappings().containsKey(ref_a));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_a);
        assertEquals(ref_a.toJvmsIdentifier(), mapping.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Example", mapping.getDeobfuscatedName());
    }

    void innerClassTest() {
        TopLevelClassReference ref_a = new TopLevelClassReference("a");
        InnerClassReference ref_b = ref_a.getInnerClass("b");

        assertTrue(mappings.getMappings().containsKey(ref_a));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_a);

        assertTrue(mapping.getInnerClassMappings().containsKey(ref_b));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(ref_b);

        assertEquals("a$b", inner.getReference().toJvmsIdentifier());
        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals(EXAMPLE_PACKAGE + "/Example$Inner", inner.getFullDeobfuscatedName());
    }

    void innerClassWithoutParentMappingTest() {
        TopLevelClassReference ref_b = new TopLevelClassReference("b");
        InnerClassReference ref_a = ref_b.getInnerClass("a");

        assertTrue(mappings.getMappings().containsKey(ref_b));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_b);

        assertTrue(mapping.getInnerClassMappings().containsKey(ref_a));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(ref_a);

        assertEquals("Inner", inner.getDeobfuscatedName());
        assertEquals("b$a", inner.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Another$Inner", inner.getFullDeobfuscatedName());
    }

    void nestedInnerClassWithoutParentMappingTest() {
        TopLevelClassReference ref_b = new TopLevelClassReference("b");
        InnerClassReference ref_a = ref_b.getInnerClass("a");
        InnerClassReference ref_c = ref_a.getInnerClass("c");

        assertTrue(mappings.getMappings().containsKey(ref_b));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_b);

        assertTrue(mapping.getInnerClassMappings().containsKey(ref_a));
        InnerClassMapping inner = mapping.getInnerClassMappings().get(ref_a);
        assertTrue(inner.getInnerClassMappings().containsKey(ref_c));
        InnerClassMapping deeper = inner.getInnerClassMappings().get(ref_c);

        assertEquals("Deeper", deeper.getDeobfuscatedName());
        assertEquals("b$a$c", deeper.getReference().toJvmsIdentifier());
        assertEquals(EXAMPLE_PACKAGE + "/Another$Inner$Deeper", deeper.getFullDeobfuscatedName());
    }

    void fieldTest() {
        TopLevelClassReference ref_a = new TopLevelClassReference("a");
        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(mappings.getMappings().containsKey(ref_a));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_a);

        assertTrue(mapping.getFieldMappings().containsKey(aSig));
        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("someField", fieldMapping.getDeobfuscatedName());
    }

    void fieldInnerClassTest() {
        TopLevelClassReference ref_a = new TopLevelClassReference("a");
        InnerClassReference ref_b = ref_a.getInnerClass("b");
        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(mappings.getMappings().containsKey(ref_a));
        assertTrue(mappings.getMappings().get(ref_a).getInnerClassMappings().containsKey(ref_b));

        ClassMapping<?> mapping = mappings.getMappings().get(ref_a).getInnerClassMappings().get(ref_b);

        assertTrue(mapping.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = mapping.getFieldMappings().get(aSig);
        assertEquals("someInnerField", fieldMapping.getDeobfuscatedName());
    }

    void fieldNestedInnerClassTest() {
        TopLevelClassReference ref_a = new TopLevelClassReference("a");
        InnerClassReference ref_b = ref_a.getInnerClass("b");
        InnerClassReference ref_c = ref_b.getInnerClass("c");

        assertTrue(mappings.getMappings().containsKey(ref_a));
        assertTrue(mappings.getMappings().get(ref_a).getInnerClassMappings().containsKey(ref_b));
        ClassMapping<?> inner = mappings.getMappings().get(ref_a).getInnerClassMappings().get(ref_b);
        assertTrue(inner.getInnerClassMappings().containsKey(ref_c));
        ClassMapping<?> deeper = inner.getInnerClassMappings().get(ref_c);

        FieldSignature aSig = new FieldSignature("a", FieldType.of("I"));

        assertTrue(deeper.getFieldMappings().containsKey(aSig));

        FieldMapping fieldMapping = deeper.getFieldMappings().get(aSig);
        assertEquals("someDeeperField", fieldMapping.getDeobfuscatedName());
    }

    void methodTest() {
        TopLevelClassReference ref_a = new TopLevelClassReference("a");

        assertTrue(mappings.getMappings().containsKey(ref_a));
        ClassMapping<?> mapping = mappings.getMappings().get(ref_a);

        MethodSignature aSig = new MethodSignature("a", MethodDescriptor.of("(ILa;I)La;"));
        assertTrue(mapping.getMethodMappings().containsKey(aSig));

        MethodMapping methodMapping = mapping.getMethodMappings().get(aSig);
        assertEquals("someMethod", methodMapping.getDeobfuscatedName());
        assertArrayEquals(
                new FieldType[]{
                        BaseType.INT,
                        ref_a.getClassType(),
                        BaseType.INT
                },
                methodMapping.getReference().getSignature().getDescriptor().getParamTypes().toArray()
        );
        assertEquals(ref_a.getClassType(), methodMapping.getReference().getSignature().getDescriptor().getReturnType());

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
        TopLevelClassReference ref_a = new TopLevelClassReference("a");
        InnerClassReference ref_c = ref_a.getInnerClass("c");
        //assertEquals("com/example/project/Example$c", ClassMapping.deobfuscate(mappings, ref_c));
    }

}
