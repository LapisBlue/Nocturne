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
package blue.lapis.nocturne.processor.transform;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.MEMBER_DELIMITER;
import static blue.lapis.nocturne.util.Constants.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.Constants.MEMBER_SUFFIX;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUint;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUshort;
import static blue.lapis.nocturne.util.helper.ByteHelper.getBytes;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.processor.ClassProcessor;
import blue.lapis.nocturne.processor.constantpool.model.ConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.structure.ClassStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.IgnoredStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.NameAndTypeStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.RefStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.StructureType;
import blue.lapis.nocturne.processor.constantpool.model.structure.Utf8Structure;
import blue.lapis.nocturne.util.MemberType;

import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Manages interpretation and transformation of constant pool, given the raw
 * bytecode of a class.
 */
public class ClassTransformer extends ClassProcessor {

    private ImmutableConstantPool constantPool;
    private boolean isPoolProcessed;
    private ConstantPool processedPool;

    private List<String> syntheticFields = new ArrayList<>();
    private List<String> syntheticMethods = new ArrayList<>();

    private Map<Integer, Integer> processedFieldNameMap = new HashMap<>();
    private Map<Integer, Integer> processedFieldDescriptorMap = new HashMap<>();
    private Map<Integer, Integer> processedMethodNameMap = new HashMap<>();
    private Map<Integer, Integer> processedMethodDescriptorMap = new HashMap<>();

    private static final ImmutableList<String> IGNORED_METHODS = new ImmutableList.Builder<String>()
            .add("<init>").add("<clinit>").build();

    public ClassTransformer(String className, byte[] bytes) {
        super(className, bytes);
        assert JarClassEntry.INDEXED_CLASSES.containsKey(getClassName());
        constantPool = JarClassEntry.INDEXED_CLASSES.get(getClassName()).getConstantPool();
        processedPool = new ConstantPool(constantPool.getContents(), constantPool.length());
    }

    public List<String> getSyntheticFields() {
        return syntheticFields;
    }

    public List<String> getSyntheticMethods() {
        return syntheticMethods;
    }

    /**
     * Processes the class and returns the new bytecode.
     *
     * @return The processed bytecode
     */
    public byte[] process() throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] header = processClassHeader(buffer);
        buffer.get(new byte[constantPool.length()]); // skip constant pool
        byte[] intermediate = processIntermediateBytes(buffer);
        byte[] fields = processFieldBytes(buffer);
        byte[] methods = processMethodBytes(buffer);
        byte[] remainder = processRemainder(buffer);

        // next call MUST come after field and method processing
        byte[] poolBytes = getProcessedPool().getBytes();

        ByteBuffer bb = ByteBuffer.allocate(bytes.length + (poolBytes.length - constantPool.length()));
        bb.put(header);
        bb.put(poolBytes);
        bb.put(intermediate);
        bb.put(fields);
        bb.put(methods);
        bb.put(remainder);
        return bb.array();
    }

    /**
     * Processes the header of the class (the first 8 bytes).
     *
     * @param buffer The {@link ByteBuffer} to read from
     * @return The processed class header
     */
    public byte[] processClassHeader(ByteBuffer buffer) {
        byte[] value = new byte[CLASS_FORMAT_CONSTANT_POOL_OFFSET];
        buffer.get(value);
        return value;
    }

    /**
     * Processes the intermediate bytes between the constant pool and the member
     * definitions.
     *
     * @param buffer The buffer to read from
     * @return The intermediate bytes
     */
    public byte[] processIntermediateBytes(ByteBuffer buffer) {
        int initialPos = buffer.position(); // mark the initial position of the buffer

        // Okay, so here's what's happening here:
        //     - The first 6 bytes aren't relevant at all
        //     - The next 2 bytes are the number of interfaces the class implements
        //     - The remaining bytes are pointers to class structures, one for each interface, each being 2 bytes
        // So, we need to process 8 bytes plus [2 times the interface count]. Hopefully this comment makes sense.
        final int irrelevantBytes = 6; // magic number
        buffer.get(new byte[irrelevantBytes]); // skip the header
        int interfaceCount = asUshort(buffer.getShort()); // read the interface count
        byte[] finalArray = new byte[irrelevantBytes + 2 + interfaceCount * 2]; // allocate an appropriately-sized array
        buffer.position(initialPos); // rewind the buffer to the initial position
        buffer.get(finalArray); // put the bytes into the allocated array
        return finalArray;
    }

    /**
     * Processes field definitions.
     *
     * @param buffer The buffer to read from
     * @return The new field definition bytes
     */
    public byte[] processFieldBytes(ByteBuffer buffer) throws IOException {
        return processMemberBytes(buffer, false);
    }

    /**
     * Processes method definitions.
     *
     * @param buffer The buffer to read from
     * @return The new method definition bytes
     */
    public byte[] processMethodBytes(ByteBuffer buffer) throws IOException {
        return processMemberBytes(buffer, true);
    }

    /**
     * Processes member definitions.
     *
     * @param buffer The buffer to read from
     * @param isMethod Whether the member is a method (a value of {@link false}
     *     for this parameter is taken to mean the member is a field)
     * @return The new member definition bytes
     */
    public byte[] processMemberBytes(ByteBuffer buffer, boolean isMethod) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int count = asUshort(buffer.getShort());
        os.write(getBytes((short) count));

        for (int m = 0; m < count; m++) {
            final int memberStart = buffer.position();

            short access = buffer.getShort();
            os.write(getBytes(access));
            boolean isSynthetic = (access & 0x1000) != 0;

            buffer.get(new byte[4]);
            ByteArrayOutputStream attrOs = new ByteArrayOutputStream();
            int attrCount = asUshort(buffer.getShort());
            attrOs.write(getBytes((short) attrCount));
            for (int i = 0; i < attrCount; i++) {
                int attrNameIndex = asUshort(buffer.getShort());
                attrOs.write(getBytes((short) attrNameIndex));
                if (!isSynthetic) {
                    String attrName = getString(attrNameIndex);
                    if (attrName.equals("Synthetic")) {
                        isSynthetic = true;
                    }
                }

                long attrLength = asUint(buffer.getInt());
                attrOs.write(getBytes((int) attrLength));
                for (int j = 0; j < attrLength; j++) {
                    try {
                        attrOs.write(buffer.get());
                    } catch (BufferUnderflowException ex) {
                        System.err.println("Class: " + getClassName() + " - m: " + m);
                        throw ex;
                    }
                }
            }

            buffer.position(memberStart + 2);

            int nameIndex = asUshort(buffer.getShort());
            int descriptorIndex = asUshort(buffer.getShort());

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
                        Utf8Structure nameStruct = new Utf8Structure(procName);
                        processedPool.add(nameStruct);
                        nameIndex = processedPool.size();
                    }
                }
            }
            os.write(getBytes((short) nameIndex));

            Map<Integer, Integer> map = isMethod ? processedMethodDescriptorMap : processedFieldDescriptorMap;
            if (map.containsKey(descriptorIndex)) {
                descriptorIndex = map.get(descriptorIndex);
            } else {
                String procDesc = getProcessedDescriptor(
                        isMethod ? MemberType.METHOD : MemberType.FIELD,
                        getString(descriptorIndex)
                );
                if (!procDesc.equals(getString(descriptorIndex))) {
                    Utf8Structure descStruct = new Utf8Structure(procDesc);
                    processedPool.add(descStruct);
                    descriptorIndex = processedPool.size();
                }
            }
            os.write(getBytes((short) descriptorIndex));

            byte[] attrArr = attrOs.toByteArray();
            os.write(attrArr);
            buffer.position(buffer.position() + attrArr.length);
        }

        return os.toByteArray();
    }

    /**
     * Returns any bytes remaining in the class file after the given offset.
     *
     * @param buffer The buffer to read
     * @return The remainder of the class file
     */
    public byte[] processRemainder(ByteBuffer buffer) {
        return ByteBuffer.allocate(buffer.capacity() - buffer.position()).put(buffer).array();
    }

    private ConstantPool getProcessedPool() {
        if (!isPoolProcessed) {
            IntStream.range(1, processedPool.size() + 1).forEach(this::handleMember);
            isPoolProcessed = true;
        }
        return processedPool;
    }

    private void handleMember(int index) {
        ConstantStructure cs = processedPool.get(index);
        if (!(cs instanceof IgnoredStructure)) {
            if (cs.getType() == StructureType.CLASS) {
                handleClassMember(cs, index, processedPool);
            } else if (  cs.getType() == StructureType.FIELDREF
                    || cs.getType() == StructureType.INTERFACE_METHODREF
                    || cs.getType() == StructureType.METHODREF) {
                handleNonClassMember(cs, processedPool);
            }
        }
    }

    private void handleClassMember(ConstantStructure cs, int index, ConstantPool pool) {
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

    private void handleNonClassMember(ConstantStructure cs, ConstantPool pool) {
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
        String className = getClassNameFromStruct((RefStructure) cs);

        NameAndType nat = getNameAndType((RefStructure) cs);
        int natIndex = ((RefStructure) cs).getNameAndTypeIndex();
        NameAndTypeStructure natStruct = (NameAndTypeStructure) constantPool.get(natIndex);
        int nameIndex = natStruct.getNameIndex();
        int typeIndex = natStruct.getTypeIndex();

        boolean ignored = false;
        if (IGNORED_METHODS.contains(nat.getName())) { // don't process ignored methods
            ignored = true;
        }

        String desc = nat.getType();

        boolean isSynthetic
                = (memberType == MemberType.FIELD ? syntheticFields : syntheticMethods).contains(nat.getName());

        if (Main.getLoadedJar().getClass(className).isPresent() && !isSynthetic && !ignored) {
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
        int natStructIndex = rs.getNameAndTypeIndex();
        assert natStructIndex <= constantPool.size();

        ConstantStructure natStruct = constantPool.get(natStructIndex);
        assert natStruct instanceof NameAndTypeStructure;

        int nameIndex = ((NameAndTypeStructure) natStruct).getNameIndex();
        int typeIndex = ((NameAndTypeStructure) natStruct).getTypeIndex();

        return new NameAndType(getString(nameIndex), getString(typeIndex));
    }

    private String getString(int strIndex) {
        assert strIndex <= processedPool.size();
        ConstantStructure cs = processedPool.get(strIndex);
        assert cs instanceof Utf8Structure;
        return ((Utf8Structure) cs).asString();
    }

    private String getClassNameFromStruct(RefStructure rs) {
        int classIndex = rs.getClassIndex();
        ConstantStructure classStruct = processedPool.get(classIndex);
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
                        if (param.isPrimitive()) {
                            newParams.add(param);
                        } else {
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
