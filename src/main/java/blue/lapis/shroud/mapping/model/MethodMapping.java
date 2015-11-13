package blue.lapis.shroud.mapping.model;

import blue.lapis.shroud.mapping.model.attribute.MethodSignature;

/**
 * Represents a {@link Mapping} for a method.
 */
public class MethodMapping extends Mapping implements ClassComponent {

    private final ClassMapping parent;
    private final MethodSignature sig;

    /**
     * Constructs a new {@link MethodMapping} with the given parameters.
     *
     * @param parent The parent {@link ClassMapping}
     * @param obfName The obfuscated name of the method
     * @param deobfName The deobfuscated name of the method
     * @param signature The (obfuscated) {@link MethodSignature} of the method
     */
    public MethodMapping(ClassMapping parent, String obfName, String deobfName, MethodSignature signature) {
        super(obfName, deobfName);
        this.parent = parent;
        this.sig = signature;

        parent.addMethodMapping(this);
    }

    @Override
    public ClassMapping getParent() {
        return parent;
    }

    /**
     * Returns the {@link MethodSignature} of this method.
     *
     * @return The {@link MethodSignature} of this method
     */
    public MethodSignature getSignature() {
        return sig;
    }

    /**
     * Returns the deobfuscated {@link MethodSignature} of this method.
     *
     * @return The deobfuscated {@link MethodSignature} of this method
     */
    public MethodSignature getDeobfuscatedSignature() {
        return getSignature().deobfuscate();
    }

}
