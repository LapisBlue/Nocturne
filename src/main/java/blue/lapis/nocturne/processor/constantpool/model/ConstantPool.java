package blue.lapis.nocturne.processor.constantpool.model;

import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Set;

/**
 * Represents the constant pool of a class.
 */
public class ConstantPool {

    private ConstantStructure[] contents;
    private int length;

    /**
     * Instantiates a new {@link ConstantPool} with the given contents.
     *
     * @param contents The contents of the new {@link ConstantPool}
     */
    public ConstantPool(Set<ConstantStructure> contents, int length) {
        this.contents = new ConstantStructure[contents.size()];
        contents.toArray(this.contents);
        this.length = length;
    }

    /**
     * Returns the number of structures contained by this {@link ConstantPool}.
     *
     * <p>This method should not be confused with {@link ConstantPool#length()}.
     * </p>
     *
     * @return The number of structures contained by this {@link ConstantPool}
     */
    public int size() {
        return contents.length;
    }

    /**
     * Returns the length of this {@link ConstantPool} in bytes.
     *
     * <p>This method should not be confused with {@link ConstantPool#size()}.
     * </p>
     *
     * @return The length of this {@link ConstantPool} in bytes
     */
    public int length() {
        return length;
    }

    /**
     * Returns the structure at the given index of this
     * <strong>1-indexed</strong> constant pool.
     *
     * @param index The index of the structure to retrieve
     * @return The retrieved structure
     * @throws IndexOutOfBoundsException If the given index is less than 1 or
     *     greater than the value returned by {@link ConstantPool#size()}
     */
    public ConstantStructure getStructure(int index) throws IndexOutOfBoundsException {
        if (index < 1 || index > size()) {
            throw new IndexOutOfBoundsException("Constant pool index " + index + " out-of-bounds");
        }
        return contents[index - 1];
    }

}
