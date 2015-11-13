package blue.lapis.shroud.mapping.model;

/**
 * Represents a mapping which is parented by a class.
 */
public interface ClassComponent {

    /**
     * Gets the parent {@link ClassMapping} of this {@link ClassComponent}.
     *
     * @return The parent {@link ClassMapping} of this {@link ClassComponent}
     */
    ClassMapping getParent();

}
