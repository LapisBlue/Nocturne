package blue.lapis.shroud.mapping.model.attribute;

/**
 * Represents a primitive data type.
 */
public enum Primitive {

    BOOLEAN('Z'),
    BYTE('B'),
    CHAR('C'),
    DOUBLE('D'),
    FLOAT('F'),
    INT('I'),
    LONG9('J'),
    SHORT('S');

    private final char key;

    Primitive(char key) {
        this.key = key;
    }

    /**
     * Gets the key character associated with this {@link Primitive} type.
     *
     * @return The key character associated with this {@link Primitive} type
     */
    public char getKey() {
        return key;
    }

    /**
     * Gets the {@link Primitive} type associated with the given key character.
     * @param key The key character to match
     * @return The {@link Primitive} type associated with the given key
     *     character
     */
    public static Primitive getFromKey(char key) {
        for (Primitive prim : Primitive.values()) {
            if (prim.getKey() == key) {
                return prim;
            }
        }
        throw new IllegalArgumentException("Illegal primitive key");
    }

}
