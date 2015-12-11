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
package blue.lapis.nocturne.transform.constpool;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.MEMBER_DELIMITER;
import static blue.lapis.nocturne.util.Constants.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.Constants.MEMBER_SUFFIX;

import blue.lapis.nocturne.transform.constpool.structure.ClassStructure;
import blue.lapis.nocturne.transform.constpool.structure.ConstantStructure;
import blue.lapis.nocturne.transform.constpool.structure.IrrelevantStructure;
import blue.lapis.nocturne.transform.constpool.structure.NameAndTypeStructure;
import blue.lapis.nocturne.transform.constpool.structure.RefStructure;
import blue.lapis.nocturne.transform.constpool.structure.StructureType;
import blue.lapis.nocturne.transform.constpool.structure.Utf8Structure;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.MemberType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages interpretation and transformation of constant pool, given the raw
 * bytecode of a class.
 */
public class ConstantPoolProcessor {

    private final byte[] bytes;

    private final ImmutableList<ConstantStructure> constantPool;

    private final int constantPoolEnd;

    public ConstantPoolProcessor(byte[] bytes) {
        this.bytes = bytes;

        List<ConstantStructure> tempPool = new ArrayList<>();

        int constPoolCount = ByteBuffer.allocate(Short.BYTES)
                .put(bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET], bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET + 1])
                .get() & Constants.SHORT_UNSIGNER;
        int offset = CLASS_FORMAT_CONSTANT_POOL_OFFSET + 2;
        for (int i = 0; i < constPoolCount; i++) {
            int length = StructureType.fromTag(bytes[offset]).getLength();
            offset++;
            byte[] structBytes = new byte[length + 1];
            System.arraycopy(bytes, offset, structBytes, 0, length);
            offset += length;
            tempPool.add(ConstantStructure.createConstantStructure(structBytes));
        }
        constantPool = ImmutableList.copyOf(tempPool);
        constantPoolEnd = offset;
    }

    /**
     * Processes the class loaded by this {@link ConstantPoolProcessor} for
     * further parsing within the program.
     *
     * @return The processed bytecode of the class
     */
    public byte[] process() {
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < CLASS_FORMAT_CONSTANT_POOL_OFFSET; i++) {
            byteList.add(bytes[i]);
        }

        for (ConstantStructure cs : getProcessedPool()) {
            for (byte b : cs.getBytes()) {
                byteList.add(b);
            }
        }

        for (int i = constantPoolEnd; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }

        byte[] newBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            newBytes[i] = byteList.get(i);
        }
        return newBytes;
    }

    @SuppressWarnings("fallthrough")
    private List<ConstantStructure> getProcessedPool() {
        List<ConstantStructure> newPool = Lists.newArrayList(constantPool);

        for (int i = 0; i < constantPool.size(); i++) {
            handleMember(constantPool.get(i), i, newPool);
        }
        return newPool;
    }

    private void handleMember(ConstantStructure cs, int index, List<ConstantStructure> pool) {
        if (!(cs instanceof IrrelevantStructure)) {
            if (cs.getType() == StructureType.CLASS) {
                handleClassMember(cs, index, pool);
            }
            if (       cs.getType() == StructureType.FIELDREF
                    || cs.getType() == StructureType.INTERFACE_METHODREF
                    || cs.getType() == StructureType.METHODREF) {
                handleNonClassMember(cs, pool);
            }
        }
        pool.add(cs);
    }

    private void handleClassMember(ConstantStructure cs, int index, List<ConstantStructure> pool) {
        String name = getString(((ClassStructure) cs).getNameIndex());
        String newName = getProcessedName(name, MemberType.CLASS.name());
        byte[] strBytes = newName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer strBuffer = ByteBuffer.allocate(strBytes.length + 3);
        strBuffer.put(StructureType.UTF_8.getTag());
        strBuffer.putShort((short) strBytes.length);
        strBuffer.put(strBytes);
        pool.add(new Utf8Structure(strBuffer.array()));

        ByteBuffer classBuffer = ByteBuffer.allocate(StructureType.CLASS.getLength());
        classBuffer.put(StructureType.CLASS.getTag());
        classBuffer.putShort((short) pool.size());
        pool.set(index, new ClassStructure(classBuffer.array()));
    }

    private void handleNonClassMember(ConstantStructure cs, List<ConstantStructure> pool) {
        String typeStr;
        switch (cs.getType()) {
            case FIELDREF: {
                typeStr = MemberType.FIELD.name();
                break;
            }
            case INTERFACE_METHODREF: {
                // fall through
            }
            case METHODREF: {
                typeStr = MemberType.METHOD.name();
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        String className = getClassName((RefStructure) cs);
        NameAndType nat = getNameAndType((RefStructure) cs);

        String newName = getProcessedName(className + CLASS_PATH_SEPARATOR_CHAR + nat.getName(), typeStr);
        byte[] newNameBytes = newName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nameBuffer = ByteBuffer.allocate(newNameBytes.length + 3);
        nameBuffer.put(StructureType.UTF_8.getTag());
        nameBuffer.putShort((short) newName.length());
        nameBuffer.put(newNameBytes);
        pool.add(new Utf8Structure(nameBuffer.array()));
        int nameIndex = pool.size();

        ByteBuffer buffer = ByteBuffer.allocate(StructureType.NAME_AND_TYPE.getLength());
        buffer.put(StructureType.NAME_AND_TYPE.getTag());
        buffer.putShort((short) nameIndex);
        int natIndex = ((RefStructure) cs).getNameAndTypeIndex();
        NameAndTypeStructure natStruct = (NameAndTypeStructure) constantPool.get(natIndex);
        buffer.putShort((short) natStruct.getTypeIndex());
        pool.set(natIndex - 1, new NameAndTypeStructure(buffer.array()));
    }

    private NameAndType getNameAndType(RefStructure rs) {
        int natStructIndex = rs.getNameAndTypeIndex();
        assert natStructIndex <= constantPool.size();

        ConstantStructure natStruct = constantPool.get(natStructIndex - 1);
        assert natStruct instanceof NameAndTypeStructure;

        int nameIndex = ((NameAndTypeStructure) natStruct).getNameIndex();
        int typeIndex = ((NameAndTypeStructure) natStruct).getNameIndex();

        return new NameAndType(getString(nameIndex), getString(typeIndex));
    }

    private String getString(int strIndex) {
        assert strIndex <= constantPool.size();
        ConstantStructure cs = constantPool.get(strIndex - 1);
        assert cs instanceof Utf8Structure;
        return ((Utf8Structure) cs).asString();
    }

    private String getClassName(RefStructure rs) {
        int classIndex = rs.getClassIndex();
        ConstantStructure classStruct = constantPool.get(classIndex - 1);
        assert classStruct instanceof ClassStructure;
        return getString(((ClassStructure) classStruct).getNameIndex());
    }

    private class NameAndType {

        private final String name;
        private final String type;

        NameAndType(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    }

    private static String getProcessedName(String memberName, String memberType) {
        return MEMBER_PREFIX + memberType + MEMBER_DELIMITER + memberName + MEMBER_SUFFIX;
    }

}