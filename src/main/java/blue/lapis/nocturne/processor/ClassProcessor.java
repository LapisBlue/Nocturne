package blue.lapis.nocturne.processor;

/**
 * Accepts the bytes of a class file as input and returns a processed version.
 */
public abstract class ClassProcessor {

    protected final String className;
    protected final byte[] bytes;

    protected ClassProcessor(String className, byte[] bytes) {
        this.className = className;
        this.bytes = bytes;
    }

    public String getClassName() {
        return className;
    }

    public byte[] getOriginalBytes() {
        return bytes;
    }

}
