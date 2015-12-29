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
package blue.lapis.nocturne.transform;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.MEMBER_DELIMITER;
import static blue.lapis.nocturne.util.Constants.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.Constants.MEMBER_SUFFIX;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUint;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUshort;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.transform.structure.ClassStructure;
import blue.lapis.nocturne.transform.structure.ConstantStructure;
import blue.lapis.nocturne.transform.structure.DummyStructure;
import blue.lapis.nocturne.transform.structure.IrrelevantStructure;
import blue.lapis.nocturne.transform.structure.NameAndTypeStructure;
import blue.lapis.nocturne.transform.structure.RefStructure;
import blue.lapis.nocturne.transform.structure.StructureType;
import blue.lapis.nocturne.transform.structure.Utf8Structure;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.ByteHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages interpretation and transformation of constant pool, given the raw
 * bytecode of a class.
 */
public class ClassTransformer {

    private final String className;
    private final byte[] bytes;

    private ImmutableList<ConstantStructure> constantPool;
    private List<ConstantStructure> processedPool;

    private int constantPoolLength;

    private List<String> syntheticFields = new ArrayList<>();
    private List<String> syntheticMethods = new ArrayList<>();

    private Map<Integer, Integer> processedFieldNameMap = new HashMap<>();
    private Map<Integer, Integer> processedFieldDescriptorMap = new HashMap<>();
    private Map<Integer, Integer> processedMethodNameMap = new HashMap<>();
    private Map<Integer, Integer> processedMethodDescriptorMap = new HashMap<>();

    private static final ImmutableList<String> IGNORED_METHODS = new ImmutableList.Builder<String>()
            .add("<init>").add("<clinit>").build();

    public ClassTransformer(String className, byte[] bytes) {
        this.className = className;
        this.bytes = bytes;
        loadConstantPool();
    }

    public String getClassName() {
        return className;
    }

    public List<String> getSyntheticFields() {
        return syntheticFields;
    }

    public List<String> getSyntheticMethods() {
        return syntheticMethods;
    }

    private void loadConstantPool() {
        List<ConstantStructure> tempPool = new ArrayList<>();

        int constPoolCount = asUshort(bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET],
                bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET + 1]) - 1;
        int offset = CLASS_FORMAT_CONSTANT_POOL_OFFSET + 2;
        for (int i = 0; i < constPoolCount; i++) {
            StructureType sType = StructureType.fromTag(bytes[offset]);
            int length = sType == StructureType.UTF_8
                    ? asUshort(bytes[offset + 1], bytes[offset + 2]) + 2
                    : sType.getLength();
            byte[] structBytes = new byte[length + 1];
            System.arraycopy(bytes, offset, structBytes, 0, length + 1);
            offset += length + 1;
            tempPool.add(ConstantStructure.createConstantStructure(structBytes));

            if (sType == StructureType.DOUBLE || sType == StructureType.LONG) {
                tempPool.add(new DummyStructure());
                i++;
            }
        }
        constantPool = ImmutableList.copyOf(tempPool);
        constantPoolLength = offset - CLASS_FORMAT_CONSTANT_POOL_OFFSET;
    }

    /**
     * Processes the class and returns the new bytecode.
     *
     * @return The processed bytecode
     */
    public byte[] process() {
        int offset = 0;

        byte[] header = processClassHeader();
        offset += header.length + constantPoolLength;

        byte[] intermediate = processIntermediateBytes(offset);
        offset += intermediate.length;

        byte[] fields = processFieldBytes(offset);
        offset += fields.length;

        byte[] methods = processMethodBytes(offset);
        offset += methods.length;

        byte[] remainder = processRemainder(offset);
        offset += remainder.length;

        // next call MUST come after field and method processing
        byte[] constantPool = getProcessedConstantPoolBytes();

        ByteBuffer bb = ByteBuffer.allocate(offset + (constantPool.length - constantPoolLength));
        bb.put(header);
        bb.put(constantPool);
        bb.put(intermediate);
        bb.put(fields);
        bb.put(methods);
        bb.put(remainder);
        return bb.array();
    }

    /**
     * Processes the header of the class (the first 8 bytes)
     *
     * @return The processed class header
     */
    public byte[] processClassHeader() {
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < CLASS_FORMAT_CONSTANT_POOL_OFFSET; i++) {
            byteList.add(bytes[i]);
        }

        ByteBuffer bb = ByteBuffer.allocate(CLASS_FORMAT_CONSTANT_POOL_OFFSET);
        byteList.forEach(bb::put);
        return bb.array();
    }

    /**
     * Gets the bytecode of the processed constant pool.
     *
     * @return The bytecode of the processed constant pool
     */
    public byte[] getProcessedConstantPoolBytes() {
        List<Byte> byteList = new ArrayList<>();

        List<ConstantStructure> constPool = getProcessedPool();
        for (ConstantStructure cs : constPool) {
            for (byte b : cs.getBytes()) {
                byteList.add(b);
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(byteList.size() + Short.BYTES);
        bb.putShort((short) (constPool.size() + 1)); // set the size
        byteList.forEach(bb::put);// set the actual bytes

        return bb.array();
    }

    /**
     * Processes the intermediate bytes between the constant pool and the member
     * definitions.
     *
     * @param offset The offset to begin from
     * @return The intermediate bytes
     */
    public byte[] processIntermediateBytes(int offset) {
        List<Byte> byteList = new ArrayList<>();

        // Okay, so here's what's happening here:
        //     - The first 6 bytes aren't relevant at all
        //     - The next 2 bytes are the number of interfaces the class implements
        //     - The remaining bytes are pointers to class structures, one for each interface, each being 2 bytes
        // So, we need to process 8 bytes plus [2 times the interface count]. Hopefully this comment makes sense.
        int interfacesCount = asUshort(bytes[offset + 6], bytes[offset + 7]);
        for (int i = 0; i < 8 + (interfacesCount * 2); i++) {
            byteList.add(bytes[offset + i]);
        }

        ByteBuffer bb = ByteBuffer.allocate(byteList.size());
        byteList.forEach(bb::put);
        return bb.array();
    }

    /**
     * Processes field definitions.
     *
     * @param offset The offset to begin from
     * @return The new field definition bytes
     */
    public byte[] processFieldBytes(int offset) {
        return processMemberBytes(offset, false);
    }

    /**
     * Processes method definitions.
     *
     * @param offset The offset to begin from
     * @return The new method definition bytes
     */
    public byte[] processMethodBytes(int offset) {
        return processMemberBytes(offset, true);
    }

    /**
     * Processes member definitions.
     *
     * @param offset The offset to begin from
     * @param isMethod Whether the member is a method (a value of {@link false}
     *     for this parameter is taken to mean the member is a field)
     * @return The new member definition bytes
     */
    public byte[] processMemberBytes(int offset, boolean isMethod) {
        List<Byte> byteList = new ArrayList<>();

        int count = asUshort(bytes[offset], bytes[offset + 1]);
        byteList.add(bytes[offset]);
        byteList.add(bytes[offset + 1]);

        int current = offset + 2;
        for (int m = 0; m < count; m++) {
            int access = asUshort(bytes[current], bytes[current + 1]);
            boolean isSynthetic = (access & 0x1000) != 0;
            byteList.add(bytes[current]);
            byteList.add(bytes[current + 1]);
            current += 2;

            List<Byte> attrBytes = new ArrayList<>();
            int attrOffset = current + 4;
            int attrCount = asUshort(bytes[attrOffset], bytes[attrOffset + 1]);
            attrBytes.add(bytes[attrOffset]);
            attrBytes.add(bytes[attrOffset + 1]);
            attrOffset += 2;

            for (int i = 0; i < attrCount; i++) {
                if (!isSynthetic) {
                    String attrName = getString(asUshort(bytes[attrOffset], bytes[attrOffset + 1]));
                    if (attrName.equals("Synthetic")) {
                        isSynthetic = true;
                    }
                }

                long attrLength = asUint(bytes[attrOffset + 2], bytes[attrOffset + 3], bytes[attrOffset + 4],
                        bytes[attrOffset + 5]);
                // loop twice to get through the name, 4 times for the length, then n times for the content
                for (int j = 0; j < 6 + attrLength; j++) {
                    attrBytes.add(bytes[attrOffset]);
                    attrOffset++;
                }
            }

            int nameIndex = asUshort(bytes[current], bytes[current + 1]);
            int descriptorIndex = asUshort(bytes[current + 2], bytes[current + 3]);

            if (isSynthetic) {
                (isMethod ? syntheticMethods : syntheticFields).add(getString(nameIndex));
            }

            if (!isSynthetic) {
                if (!isMethod || !IGNORED_METHODS.contains(getString(nameIndex))) {
                    Map<Integer, Integer> map = isMethod ? processedMethodNameMap : processedFieldNameMap;
                    if (map.containsKey(nameIndex)) {
                        nameIndex = map.get(nameIndex);
                    } else {
                        String procName = getProcessedName(
                                getClassName() + CLASS_PATH_SEPARATOR_CHAR + getString(nameIndex),
                                getString(descriptorIndex),
                                isMethod ? MemberType.METHOD : MemberType.FIELD
                        );
                        getProcessedPool().add(new Utf8Structure(procName));
                        nameIndex = getProcessedPool().size();
                    }
                }
            }
            byte[] nameBytes = ByteBuffer.allocate(Short.BYTES).putShort((short) nameIndex).array();
            byteList.add(nameBytes[0]);
            byteList.add(nameBytes[1]);
            current += 2;

            if (!isSynthetic) {
                Map<Integer, Integer> map = isMethod ? processedMethodDescriptorMap : processedFieldDescriptorMap;
                if (map.containsKey(nameIndex)) {
                    descriptorIndex = map.get(nameIndex);
                } else {
                    String procDesc = getProcessedDescriptor(
                            isMethod ? MemberType.METHOD : MemberType.FIELD,
                            getString(descriptorIndex)
                    );
                    if (!procDesc.equals(getString(descriptorIndex))) {
                        getProcessedPool().add(new Utf8Structure(procDesc));
                        descriptorIndex = getProcessedPool().size();
                    }
                }
            }
            byte[] descBytes = ByteBuffer.allocate(Short.BYTES).putShort((short) descriptorIndex).array();
            byteList.add(descBytes[0]);
            byteList.add(descBytes[1]);

            byteList.addAll(attrBytes);
            current = attrOffset;

        }

        ByteBuffer bb = ByteBuffer.allocate(byteList.size());
        byteList.forEach(bb::put);
        return bb.array();
    }

    /**
     * Returns any bytes remaining in the class file after the given offset.
     *
     * @return The remainder of the class file
     */
    public byte[] processRemainder(int offset) {
        byte[] remainderBytes = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, remainderBytes, 0, remainderBytes.length);
        return remainderBytes;
    }

    @SuppressWarnings("fallthrough")
    private List<ConstantStructure> getProcessedPool() {
        if (processedPool == null) {
            List<ConstantStructure> newPool = Lists.newArrayList(constantPool);

            for (int i = 0; i < constantPool.size(); i++) {
                handleMember(constantPool.get(i), i, newPool);
            }

            processedPool = newPool;
        }
        return processedPool;
    }

    private void handleMember(ConstantStructure cs, int index, List<ConstantStructure> pool) {
        if (!(cs instanceof IrrelevantStructure)) {
            if (cs.getType() == StructureType.CLASS) {
                handleClassMember(cs, index, pool);
            } else if (  cs.getType() == StructureType.FIELDREF
                    || cs.getType() == StructureType.INTERFACE_METHODREF
                    || cs.getType() == StructureType.METHODREF) {
                handleNonClassMember(cs, pool);
            }
        }
    }

    private void handleClassMember(ConstantStructure cs, int index, List<ConstantStructure> pool) {
        String name = getString(((ClassStructure) cs).getNameIndex());

        if (!Main.getLoadedJar().getClass(name).isPresent()) {
            return;
        }

        String newName = getProcessedName(name, null, MemberType.CLASS);
        byte[] strBytes = newName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer strBuffer = ByteBuffer.allocate(strBytes.length + 3);
        strBuffer.put(StructureType.UTF_8.getTag());
        strBuffer.putShort((short) strBytes.length);
        strBuffer.put(strBytes);
        pool.add(new Utf8Structure(strBuffer.array()));

        ByteBuffer classBuffer = ByteBuffer.allocate(StructureType.CLASS.getLength() + 1);
        classBuffer.put(StructureType.CLASS.getTag());
        classBuffer.putShort((short) pool.size());
        pool.set(index, new ClassStructure(classBuffer.array()));
    }

    private void handleNonClassMember(ConstantStructure cs, List<ConstantStructure> pool) {
        MemberType memberType;
        switch (cs.getType()) {
            case FIELDREF: {
                memberType = MemberType.FIELD;
                break;
            }
            case INTERFACE_METHODREF: // fall through
            case METHODREF: {
                memberType = MemberType.METHOD;
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        String className = getClassName((RefStructure) cs);

        NameAndType nat = getNameAndType((RefStructure) cs);
        int natIndex = ((RefStructure) cs).getNameAndTypeIndex() - 1;
        NameAndTypeStructure natStruct = (NameAndTypeStructure) constantPool.get(natIndex);
        int nameIndex = natStruct.getNameIndex();
        int typeIndex = natStruct.getTypeIndex();

        if (IGNORED_METHODS.contains(nat.getName())) { // don't process ignored methods
            return;
        }

        String desc = nat.getType();

        if (Main.getLoadedJar().getClass(className).isPresent()) {
            String newName = getProcessedName(className + CLASS_PATH_SEPARATOR_CHAR + nat.getName(), desc,
                    memberType);
            byte[] newNameBytes = newName.getBytes(StandardCharsets.UTF_8);
            ByteBuffer nameBuffer = ByteBuffer.allocate(newNameBytes.length + 3);
            nameBuffer.put(StructureType.UTF_8.getTag());
            nameBuffer.putShort((short) newNameBytes.length);
            nameBuffer.put(newNameBytes);
            pool.add(new Utf8Structure(nameBuffer.array()));
            Map<Integer, Integer> map = memberType == MemberType.FIELD
                    ? processedFieldNameMap : processedMethodNameMap;
            map.put(nameIndex, pool.size());
            nameIndex = pool.size();
        }

        String processedDesc = getProcessedDescriptor(
                cs.getType() == StructureType.FIELDREF ? MemberType.FIELD : MemberType.METHOD,
                desc
        );
        if (!processedDesc.equals(desc)) {
            byte[] newTypeBytes = processedDesc.getBytes(StandardCharsets.UTF_8);
            ByteBuffer typeBuffer = ByteBuffer.allocate(newTypeBytes.length + 3);
            typeBuffer.put(StructureType.UTF_8.getTag());
            typeBuffer.putShort((short) newTypeBytes.length);
            typeBuffer.put(newTypeBytes);
            pool.add(new Utf8Structure(typeBuffer.array()));
            Map<Integer, Integer> map = memberType == MemberType.FIELD
                    ? processedFieldDescriptorMap : processedMethodDescriptorMap;
            map.put(typeIndex, pool.size());
            typeIndex = pool.size();
        }

        ByteBuffer buffer = ByteBuffer.allocate(StructureType.NAME_AND_TYPE.getLength() + 1);
        buffer.put(StructureType.NAME_AND_TYPE.getTag());
        buffer.putShort((short) nameIndex);
        buffer.putShort((short) typeIndex);
        pool.set(natIndex, new NameAndTypeStructure(buffer.array()));
    }

    private NameAndType getNameAndType(RefStructure rs) {
        int natStructIndex = rs.getNameAndTypeIndex() - 1;
        assert natStructIndex <= constantPool.size();

        ConstantStructure natStruct = constantPool.get(natStructIndex);
        assert natStruct instanceof NameAndTypeStructure;

        int nameIndex = ((NameAndTypeStructure) natStruct).getNameIndex();
        int typeIndex = ((NameAndTypeStructure) natStruct).getTypeIndex();

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

    // current format is %NOCTURNE+TYPE-name-descriptor% (descriptor is optional)
    public static String getProcessedName(String qualifiedMemberName, String descriptor, MemberType memberType) {
        return MEMBER_PREFIX + memberType.name() + MEMBER_DELIMITER + qualifiedMemberName
                + (descriptor != null ? MEMBER_DELIMITER + descriptor : "") + MEMBER_SUFFIX;
    }

    private static String getProcessedDescriptor(MemberType memberType, String desc) {
        switch (memberType) {
            case FIELD: {
                if (desc.startsWith("L") && desc.endsWith(";")) {
                    String typeClass = desc.substring(1, desc.length());
                    if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                        return "L" + getProcessedName(typeClass, null, MemberType.CLASS) + ";";
                    }
                }
                break;
            }
            case METHOD: {
                if (!desc.contains(MEMBER_PREFIX)) { // if this condition is true then it's already been processed
                    MethodDescriptor md = MethodDescriptor.fromString(desc);
                    List<Type> newParams = new ArrayList<>();
                    for (Type param : md.getParamTypes()) {
                        if (!param.isPrimitive()) {
                            String typeClass = param.getClassName();
                            if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                                newParams.add(new Type(getProcessedName(typeClass, null, MemberType.CLASS),
                                        param.getArrayDimensions()));
                            } else {
                                newParams.add(param);
                            }
                        }
                    }
                    Type returnType = md.getReturnType();
                    if (!returnType.isPrimitive()) {
                        String typeClass = returnType.getClassName();
                        if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                            returnType = new Type(getProcessedName(typeClass, null, MemberType.CLASS),
                                    returnType.getArrayDimensions());
                        }
                    }

                    Type[] newParamArr = new Type[newParams.size()];
                    newParams.toArray(newParamArr);
                    MethodDescriptor newMd = new MethodDescriptor(returnType, newParamArr);
                    return newMd.toString();
                }
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        return desc;
    }

}
