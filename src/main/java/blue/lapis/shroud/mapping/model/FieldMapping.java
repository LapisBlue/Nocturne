package blue.lapis.shroud.mapping.model;

import blue.lapis.shroud.mapping.model.attribute.Type;

/**
 * Represents a {@link Mapping} for a field.
 */
public class FieldMapping extends Mapping implements ClassComponent {

    private final ClassMapping parent;
    private final Type type; //TODO: not necessary (or possible) for SRG mappings so we won't enforce it

    /**
     * Constructs a new {@link FieldMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the field
     * @param deobfName The deobfuscated name of the field
     * @param type The (obfuscated) {@link Type} of the field
     */
    public FieldMapping(ClassMapping parent, String obfName, String deobfName, Type type) {
        super(obfName, deobfName);
        this.parent = parent;
        this.type = type;

        parent.addFieldMapping(this);
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    /**
     * Returns the {@link Type} of this field.
     *
     * @return The {@link Type} of this field
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the deobfuscated {@link Type} of this field.
     *
     * @return The deobfuscated {@link Type} of this field
     */
    public Type getDeobfuscatedType() {
        return getType().deobfuscate();
    }

}
