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

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.MappingsReader;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.transform.constpool.structure.ClassStructure;
import blue.lapis.nocturne.transform.constpool.structure.ConstantStructure;
import blue.lapis.nocturne.transform.constpool.structure.FieldrefStructure;
import blue.lapis.nocturne.transform.constpool.structure.IrrelevantStructure;
import blue.lapis.nocturne.transform.constpool.structure.MethodrefStructure;
import blue.lapis.nocturne.transform.constpool.structure.NameAndTypeStructure;
import blue.lapis.nocturne.transform.constpool.structure.RefStructure;
import blue.lapis.nocturne.transform.constpool.structure.StructureType;
import blue.lapis.nocturne.transform.constpool.structure.Utf8Structure;
import blue.lapis.nocturne.util.Constants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages interpretation and transformation of constant pool, given the raw
 * bytecode of a class.
 */
public class ConstantPoolTransformer {

    private final byte[] bytes;

    private List<ConstantStructure> constantPool = new ArrayList<>();

    private final int constantPoolEnd;

    public ConstantPoolTransformer(byte[] bytes) {
        this.bytes = bytes;
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
            constantPool.add(ConstantStructure.createConstantStructure(structBytes));
        }
        constantPoolEnd = offset;
    }

    /**
     * Transforms the class loaded by this {@link ConstantPoolTransformer} using the
     * given {@link MappingContext}.
     *
     * @param mappingContext The {@link MappingContext} to transform the class
     *                       against
     * @return The transformed bytecode of the class
     */
    public byte[] transform(MappingContext mappingContext) {
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < CLASS_FORMAT_CONSTANT_POOL_OFFSET; i++) {
            byteList.add(bytes[i]);
        }

        for (ConstantStructure cs : getTransformedPool(mappingContext)) {
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

    private List<ConstantStructure> getTransformedPool(MappingContext mappingContext) {
        List<ConstantStructure> newPool = Lists.newArrayList(constantPool);

        int i = -1;
        outer:
        for (ConstantStructure cs : constantPool) {
            ++i;
            if (!(cs instanceof IrrelevantStructure)) {
                switch (cs.getType()) {
                    case CLASS: {
                        String obfName = getString(((ClassStructure) cs).getNameIndex());
                        String deobfName = ClassMapping.deobfuscate(mappingContext, obfName);
                        if (!obfName.equals(deobfName)) {
                            byte[] strBytes = deobfName.getBytes(StandardCharsets.UTF_8);
                            ByteBuffer strBuffer = ByteBuffer.allocate(strBytes.length + 3);
                            strBuffer.put(StructureType.UTF_8.getTag());
                            strBuffer.putShort((short) strBytes.length);
                            strBuffer.put(strBytes);
                            newPool.add(new Utf8Structure(strBuffer.array()));

                            ByteBuffer classBuffer = ByteBuffer.allocate(StructureType.CLASS.getLength());
                            classBuffer.put(StructureType.CLASS.getTag());
                            classBuffer.putShort((short) newPool.size());
                            newPool.set(i, new ClassStructure(classBuffer.array()));
                        }
                        break;
                    }
                    case FIELDREF: {
                        // fall through
                    }
                    case INTERFACE_METHODREF: {
                        // fall through
                    }
                    case METHODREF: {
                        String className = getClassName((RefStructure) cs);
                        int natIndex = ((RefStructure) cs).getNameAndTypeIndex();
                        NameAndTypeStructure natStruct = (NameAndTypeStructure) constantPool.get(natIndex);
                        NameAndType nat = getNameAndType((RefStructure) cs);

                        String fullName = nat.getName() + (cs.getType() == StructureType.FIELDREF ? "" : nat.getType());
                        if (mappingContext.getMappings().containsKey(className)) {
                            TopLevelClassMapping cm = mappingContext.getMappings().get(className);
                            ImmutableMap<String, ? extends Mapping> mappings = cs.getType() == StructureType.FIELDREF
                                    ? cm.getFieldMappings() : cm.getMethodMappings();
                            if (mappings.containsKey(fullName)) {
                                Mapping mapping = mappings.get(fullName);

                                int nameIndex;
                                int typeIndex;
                                if (!mapping.getObfuscatedName().equals(mapping.getDeobfuscatedName())) {
                                    ByteBuffer buffer = ByteBuffer.allocate(mapping.getDeobfuscatedName().length() + 3);
                                    buffer.put(StructureType.UTF_8.getTag());
                                    buffer.putShort((short) mapping.getDeobfuscatedName().length());
                                    buffer.put(mapping.getDeobfuscatedName().getBytes(StandardCharsets.UTF_8));
                                    newPool.add(new Utf8Structure(buffer.array()));
                                    nameIndex = newPool.size();
                                } else {
                                    nameIndex = natStruct.getNameIndex();
                                }

                                String obfType = mapping instanceof FieldMapping
                                        ? ((FieldMapping) mapping).getType().toString()
                                        : ((MethodMapping) mapping).getSignature().toString();
                                String deobfType = mapping instanceof FieldMapping
                                        ? ((FieldMapping) mapping).getDeobfuscatedType().toString()
                                        : ((MethodMapping) mapping).getDeobfuscatedSignature().toString();
                                if (!obfType.equals(deobfType)) {
                                    ByteBuffer buffer = ByteBuffer.allocate(deobfType.length() + 3);
                                    buffer.put(StructureType.UTF_8.getTag());
                                    buffer.putShort((short) deobfType.length());
                                    buffer.put(deobfType.getBytes(StandardCharsets.UTF_8));
                                    newPool.add(new Utf8Structure(buffer.array()));
                                    typeIndex = newPool.size();
                                } else {
                                    typeIndex = natStruct.getTypeIndex();
                                }

                                ByteBuffer buffer = ByteBuffer.allocate(StructureType.NAME_AND_TYPE.getLength());
                                buffer.put(StructureType.NAME_AND_TYPE.getTag());
                                buffer.putShort((short) nameIndex);
                                buffer.putShort((short) typeIndex);
                                newPool.set(natIndex - 1, new NameAndTypeStructure(buffer.array()));
                                continue outer;
                            }
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            newPool.add(cs);
        }
        return newPool;
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

        public NameAndType(String name, String type) {
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

}
