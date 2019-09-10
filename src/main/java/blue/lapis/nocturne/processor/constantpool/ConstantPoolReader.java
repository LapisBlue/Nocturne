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

package blue.lapis.nocturne.processor.constantpool;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUshort;

import blue.lapis.nocturne.processor.ClassProcessor;
import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.DummyStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.StructureType;

import org.cadixdev.bombe.type.reference.ClassReference;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses the constant pool of a class given its bytecode.
 */
public class ConstantPoolReader extends ClassProcessor {

    public ConstantPoolReader(ClassReference className, byte[] bytes) {
        super(className, bytes);
    }

    public ImmutableConstantPool read() {
        List<ConstantStructure> tempPool = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.get(new byte[CLASS_FORMAT_CONSTANT_POOL_OFFSET]);
        int constPoolCount = asUshort(buffer.getShort()) - 1;
        for (int i = 0; i < constPoolCount; i++) {
            StructureType sType = StructureType.fromTag(buffer.get());
            int length = sType.getLength();
            if (sType == StructureType.UTF_8) {
                length = asUshort(buffer.getShort()) + 2;
                buffer.position(buffer.position() - 2);
            }
            byte[] structBytes = new byte[length + 1];
            structBytes[0] = sType.getTag();
            buffer.get(structBytes, 1, length);
            tempPool.add(ConstantStructure.createConstantStructure(structBytes));

            if (sType == StructureType.DOUBLE || sType == StructureType.LONG) {
                tempPool.add(new DummyStructure());
                i++;
            }
        }
        return new ImmutableConstantPool(tempPool, buffer.position() - CLASS_FORMAT_CONSTANT_POOL_OFFSET);
    }

}
