package blue.lapis.shroud.mapping.model.attribute;

/**
 * Represents a method signature consisting of parameter {@link Type}s and a
 * return {@link Type}.
 */
public class MethodSignature {

    private final Type returnType;
    private final Type[] paramTypes;

    /**
     * Constructs a new {@link MethodSignature} with the given return and
     * parameter {@link Type}s.
     *
     * @param returnType The return {@link Type} of the new
     *     {@link MethodSignature}
     * @param paramTypes The parameter {@link Type}s of the new
     *     {@link MethodSignature}
     */
    public MethodSignature(Type returnType, Type... paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    /**
     * Gets the return {@link Type} of this {@link MethodSignature}.
     *
     * @return The return {@link Type} of this {@link MethodSignature}
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Gets the parameter {@link Type}s of this {@link MethodSignature}.
     *
     * @return The parameter {@link Type}s of this {@link MethodSignature}
     */
    public Type[] getParamTypes() {
        return paramTypes;
    }

    /**
     * Attempts to deobfuscate this {@link MethodSignature}.
     *
     * @return The deobfuscated {@link MethodSignature}
     */
    public MethodSignature deobfuscate() {
        Type[] deobfParams = new Type[getParamTypes().length];
        for (int i = 0; i < getParamTypes().length; i++) {
            deobfParams[i] = getParamTypes()[i].deobfuscate();
        }

        return new MethodSignature(getReturnType().deobfuscate(), deobfParams);
    }

}
