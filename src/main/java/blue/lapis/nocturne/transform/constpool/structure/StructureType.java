package blue.lapis.nocturne.transform.constpool.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a particular structure type contained by the constant pool.
 */
public enum StructureType {

    UTF_8(0x01, -1),
    INTEGER(0x03, 4),
    FLOAT(0x04, 4),
    LONG(0x05, 8),
    DOUBLE(0x06, 8),
    CLASS(0x07, 2),
    STRING(0x08, 2),
    FIELD_REF(0x09, 4),
    METHOD_REF(0x0A, 4),
    INTERFACE_METHOD_REF(0x0B, 4),
    NAME_AND_TYPE(0x0C, 4),
    METHOD_HANDLE(0x0F, 3),
    METHOD_TYPE(0x10, 2),
    INVOKE_DYNAMIC(0x12, 4);

    private static Map<Byte, StructureType> types;

    private byte tag;
    private int length;

    StructureType(int tag, int length) {
        this.tag = (byte)tag;
        this.length = length;
        register((byte)tag);
    }

    /**
     * Adds this {@link StructureType} to the registry with the given tag.
     *
     * @param tag The tag to associated with this {@link StructureType}
     */
    private void register(byte tag) {
        if (types == null) {
            types = new HashMap<>();
        }
        types.put(tag, this);
    }

    /**
     * Gets the tag internally associated with this structure type.
     *
     * @return The tag internally associated with this structure type
     */
    public byte getTag() {
        return this.tag;
    }

    /**
     * Gets the expected length of structures of this type.
     *
     * @return The expected length of structures of this type
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Gets the {@link StructureType} associated with the given byte tag.
     *
     * @param tag The tag to get a {@link StructureType} for
     * @return The {@link StructureType} associated with the given byte tag
     */
    public static StructureType fromTag(byte tag) {
        return types.get(tag);
    }

}
