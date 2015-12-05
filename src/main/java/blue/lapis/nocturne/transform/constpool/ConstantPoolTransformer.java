package blue.lapis.nocturne.transform.constpool;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.transform.constpool.structure.ConstantStructure;
import blue.lapis.nocturne.transform.constpool.structure.StructureType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages interpretation and transformation of constant pool, given the raw
 * bytecode of a class.
 */
public class ConstantPoolTransformer {

    private static final int SHORT_UNSIGNER = 0xFFFF;

    private final byte[] bytes;

    private List<ConstantStructure> constantPool = new ArrayList<>();

    private final int constantPoolEnd;

    public ConstantPoolTransformer(byte[] bytes) {
        this.bytes = bytes;
        int constPoolCount = ByteBuffer.allocate(Short.BYTES)
                .put(bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET], bytes[CLASS_FORMAT_CONSTANT_POOL_OFFSET + 1])
                .get() & SHORT_UNSIGNER;
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
     *     against
     * @return The transformed bytecode of the class
     */
    public byte[] transform(MappingContext mappingContext) {
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < CLASS_FORMAT_CONSTANT_POOL_OFFSET; i++) {
            byteList.add(bytes[i]);
        }

        //TODO: process constant pool

        for (int i = constantPoolEnd; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }

        byte[] newBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            newBytes[i] = byteList.get(i);
        }
        return newBytes;
    }

}
