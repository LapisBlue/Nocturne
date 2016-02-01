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
package blue.lapis.nocturne.processor.index;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUshort;
import static com.google.common.base.Preconditions.checkArgument;

import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.processor.ClassProcessor;
import blue.lapis.nocturne.processor.constantpool.ConstantPoolReader;
import blue.lapis.nocturne.processor.constantpool.model.ConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.structure.ClassStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.Utf8Structure;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.MethodSignature;
import blue.lapis.nocturne.processor.index.model.MethodVisibility;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an index of select information from a given class.
 */
public class ClassIndexer extends ClassProcessor {

    public ClassIndexer(String className, byte[] bytes) {
        super(className, bytes);
    }

    /**
     * Indexes the class file and returns an {@link IndexedClass} object
     * representing it.
     *
     * @return The created index of the class
     */
    public IndexedClass index() {
        ImmutableConstantPool pool = new ConstantPoolReader(getClassName(), getOriginalBytes()).read(); // get the pool

        ByteBuffer buffer = ByteBuffer.wrap(bytes); // create a buffer for the bytecode
        buffer.position(CLASS_FORMAT_CONSTANT_POOL_OFFSET + pool.length() + 4); // position the buffer

        String superClass = getClassNameFromIndex(pool, asUshort(buffer.getShort())); // read the superclass name

        int interfaceCount = asUshort(buffer.getShort()); // read the interface count
        List<String> interfaces = new ArrayList<>();
        for (int i = 0; i < interfaceCount; i++) {
            interfaces.add(getClassNameFromIndex(pool, buffer.getShort())); // read each interface name
        }

        skipFields(buffer);

        List<MethodSignature> methods = readMethods(buffer, pool);

        return new IndexedClass(getClassName(), pool, superClass, interfaces, methods);
    }

    /**
     * Skips the fields of the provided buffer, given it is positioned at their
     * immediate start.
     *
     * @param buffer The buffer to read from
     */
    private void skipFields(ByteBuffer buffer) {
        int fieldCount = buffer.getShort(); // read the field count
        for (int i = 0; i < fieldCount; i++) {
            buffer.position(buffer.position() + 6); // skip the access, name, and descriptor (2 bytes each)
            skipAttributes(buffer);
        }
    }

    /**
     * Reads the methods of the provided buffer, given it is positioned at their
     * immediate start.
     *
     * @param buffer The buffer to read from
     * @param pool The constant pool to read strings from
     * @return A {@link List} of read {@link MethodSignature}s
     */
    private List<MethodSignature> readMethods(ByteBuffer buffer, ConstantPool pool) {
        List<MethodSignature> methods = new ArrayList<>();

        int methodCount = asUshort(buffer.getShort());
        for (int i = 0; i < methodCount; i++) {
            MethodVisibility vis = MethodVisibility.fromAccessFlags(buffer.getShort());
            String name = getString(pool, buffer.getShort());
            MethodDescriptor desc = MethodDescriptor.fromString(getString(pool, buffer.getShort()));
            methods.add(new MethodSignature(name, desc, vis));

            skipAttributes(buffer);
        }

        return methods;
    }

    /**
     * Skips the upcoming attribute table of the provided buffer, given it is
     * positioned at their immediate start.
     *
     * @param buffer The buffer to read from
     */
    private void skipAttributes(ByteBuffer buffer) {
        int attrCount = asUshort(buffer.getShort()); // read the attribute count
        for (int j = 0; j < attrCount; j++) {
            buffer.position(buffer.position() + 2); // skip the attribute name
            // the length is a uint, but anything larger than Integer.MAX_VALUE won't fit in a Java array/buffer
            // also, why the hell would you have a 2 GB attribute in the first place?
            int attrLen = buffer.getInt(); // get the body length
            buffer.position(buffer.position() + attrLen); // skip the attribute body
        }
    }

    private String getString(ConstantPool pool, int strIndex) {
        assert strIndex <= pool.size();
        ConstantStructure cs = pool.get(strIndex);
        assert cs instanceof Utf8Structure;
        return ((Utf8Structure) cs).asString();
    }

    private String getClassNameFromIndex(ConstantPool pool, int index) {
        ConstantStructure classStruct = pool.get(index);
        checkArgument(classStruct instanceof ClassStructure, "Index does not point to class structure");
        return getString(pool, ((ClassStructure) classStruct).getNameIndex());
    }

}
