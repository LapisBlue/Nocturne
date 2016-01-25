package blue.lapis.nocturne.processor.index.model;

import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;

/**
 * Represents the unique signature of a particular method.
 */
public class MethodSignature {

    private final String name;
    private final MethodDescriptor descriptor;

    public MethodSignature(String name, MethodDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public String getName() {
        return name;
    }

    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

}
