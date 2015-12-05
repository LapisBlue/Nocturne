package blue.lapis.nocturne.transform.constpool.structure;

import java.nio.ByteBuffer;

/**
 * Repressents a NameAndType structure.
 */
public class NameAndTypeStructure extends ConstantStructure {

    private int nameIndex;
    private int sigIndex;

    public NameAndTypeStructure(byte[] bytes) {
        super(bytes);
        this.nameIndex = ByteBuffer.allocate(2).put(bytes[1], bytes[2]).asShortBuffer().get() & 0xFFFF;
        this.sigIndex = ByteBuffer.allocate(2).put(bytes[3], bytes[4]).asShortBuffer().get() & 0xFFFF;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int index) {
        this.nameIndex = index;
    }

    public int getSignatureIndex() {
        return sigIndex;
    }

    public void setSignatureIndex(int index) {
        this.sigIndex = index;
    }

}
