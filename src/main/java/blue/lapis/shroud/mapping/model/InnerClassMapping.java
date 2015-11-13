package blue.lapis.shroud.mapping.model;

/**
 * Represents a {@link Mapping} for an inner class, i.e. a class parented by
 * another class.
 */
public class InnerClassMapping extends ClassMapping implements ClassComponent {

    private static final char INNER_CLASS_SEPARATOR_CHAR = '$';

    private final ClassMapping parent;

    /**
     * Constructs a new {@link InnerClassMapping} with the given parameters.
     *
     * <p>The name should not include the parent class(es), just the name of the
     * inner class itself.</p>
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the inner class
     * @param deobfName The deobfuscated name of the inner class
     */
    public InnerClassMapping(ClassMapping parent, String obfName, String deobfName) {
        super(obfName, deobfName);
        this.parent = parent;

        parent.addInnerClassMapping(this);
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    /**
     * Returns the full obfuscated name of this inner class.
     *
     * @return The full obfuscated name of this inner class
     */
    public String getFullObfuscatedName() {
        return (parent instanceof InnerClassMapping
                ? ((InnerClassMapping) parent).getFullObfuscatedName() + INNER_CLASS_SEPARATOR_CHAR : "")
                + getObfuscatedName();
    }

    /**
     * Returns the full deobfuscated name of this inner class.
     *
     * @return The full deobfuscated name of this inner class
     */
    public String getFullDeobfuscatedName() {
        return (parent instanceof InnerClassMapping
                ? ((InnerClassMapping) parent).getFullDeobfuscatedName() + INNER_CLASS_SEPARATOR_CHAR : "")
                + getDeobfuscatedName();
    }

}
