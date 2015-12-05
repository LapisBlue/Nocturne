package blue.lapis.nocturne.transform.constpool.structure;

import java.nio.ByteBuffer;

/**
 * Represents a Class structure.
 */
public class ClassStructure extends ConstantStructure {

    private int nameIndex;

    public ClassStructure(byte[] bytes) {
        super(bytes);
        nameIndex = ByteBuffer.allocate(2).put(bytes[1], bytes[2]).asShortBuffer().get() & 0xFFFF;
    }
}
