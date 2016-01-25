package blue.lapis.nocturne.processor.index;

import blue.lapis.nocturne.processor.ClassProcessor;
import blue.lapis.nocturne.processor.index.model.IndexedClass;

/**
 * Creates an index of select information from a given class.
 */
public class ClassIndexer extends ClassProcessor {

    protected ClassIndexer(String className, byte[] bytes) {
        super(className, bytes);
    }

    /**
     * Processes the class file and returns an {@link IndexedClass} object
     * representing it.
     *
     * @return The created index of the class
     */
    public IndexedClass process() {
        return null;
    }

}
