package blue.lapis.nocturne.transform.constpool.structure;

import java.nio.ByteBuffer;

/**
 * Represents a *ref structure.
 */
public class RefStructure extends ConstantStructure {

    private int classIndex;
    private int natIndex;

    protected RefStructure(byte[] bytes) {
        super(bytes);
        classIndex = ByteBuffer.allocate(2).put(bytes[1], bytes[2]).asShortBuffer().get() & 0xFFFF;
        natIndex = ByteBuffer.allocate(2).put(bytes[3], bytes[4]).asShortBuffer().get() & 0xFFFF;
    }

}
