package blue.lapis.nocturne.transform.constpool.structure;

/**
 * Represents a structure in the constant pool.
 */
public abstract class ConstantStructure {

    private StructureType type;

    protected ConstantStructure(byte[] bytes) {
        assert bytes.length > 0;
        this.type = StructureType.fromTag(bytes[0]);
        assert bytes.length == type.getLength() + 1;
    }

    public StructureType getType() {
        return type;
    }

    public static ConstantStructure createConstantStructure(byte[] bytes) {
        assert bytes.length > 0;
        StructureType type = StructureType.fromTag(bytes[0]);
        switch (type) {
            case CLASS: {
                return new ClassStructure(bytes);
            }
            case FIELD_REF: {
                return new FieldrefStructure(bytes);
            }
            case INTERFACE_METHOD_REF: {
                return new InterfaceMethodrefStructure(bytes);
            }
            case METHOD_REF: {
                return new MethodrefStructure(bytes);
            }
            case NAME_AND_TYPE: {
                return new NameAndTypeStructure(bytes);
            }
            default: {
                return new IrrelevantStructure(bytes);
            }
        }
    }

}
