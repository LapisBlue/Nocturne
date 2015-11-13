package blue.lapis.shroud.mapping.model;

import blue.lapis.shroud.mapping.MappingSet;

/**
 * Represents a top-level {@link ClassMapping} (i.e. not an inner class).
 */
public class TopLevelClassMapping extends ClassMapping {

    private final MappingSet parent;

    /**
     * Constructs a new {@link TopLevelClassMapping} with the given parameters.
     *
     * @param parent The parent {@link MappingSet} of this
     *     {@link TopLevelClassMapping}
     * @param obfName The obfuscated name of the class
     * @param deobfName The deobfuscated name of the class
     */
    public TopLevelClassMapping(MappingSet parent, String obfName, String deobfName) {
        super(obfName, deobfName);
        this.parent = parent;
    }

    /**
     * Returns the parent {@link MappingSet} of this
     * {@link TopLevelClassMapping}.
     *
     * @return The parent {@link MappingSet} of this
     *     {@link TopLevelClassMapping}
     */
    public MappingSet getParent() {
        return parent;
    }

}
