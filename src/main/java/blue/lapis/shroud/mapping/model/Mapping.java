package blue.lapis.shroud.mapping.model;

/**
 * Represents a single obfuscation mapping for a particular member.
 */
public abstract class Mapping {

    private final String obf;
    private final String deobf;

    protected Mapping(String obfName, String deobfName) {
        this.obf = obfName;
        this.deobf = deobfName;
    }

    /**
     * Returns the obfuscated name of this {@link Mapping}.
     *
     * @return The obfuscated name of this {@link Mapping}
     */
    public String getObfuscatedName() {
        return obf;
    }

    /**
     * Returns the deobfuscated name of this {@link Mapping}.
     *
     * @return The deobfuscated name of this {@link Mapping}
     */
    public String getDeobfuscatedName() {
        return deobf;
    }

}
