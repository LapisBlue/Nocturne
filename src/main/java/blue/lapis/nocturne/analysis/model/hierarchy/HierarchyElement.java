package blue.lapis.nocturne.analysis.model.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an object pertaining to a hierarchy structure.
 */
public abstract class HierarchyElement implements Iterable<HierarchyNode> {

    protected Map<String, HierarchyNode> children = new HashMap<>();

    /**
     * Attempts to get the child element by the given name from the current one.
     *
     * @param name The name of the child element
     * @return The child element if it exists, or {@link Optional#empty()}
     *     otherwise
     */
    public Optional<HierarchyNode> getChild(String name) {
        if (children.containsKey(name)) {
            return Optional.of(children.get(name));
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link ImmutableSet} of all children of this
     * {@link HierarchyNode}.
     *
     * @return An {@link ImmutableSet} of all children of this
     *     {@link HierarchyNode}
     * @throws IllegalStateException If this element is
     *     {@link HierarchyNode#isTerminal()}
     */
    public ImmutableList<HierarchyNode> getChildren() throws IllegalStateException {
        return ImmutableList.copyOf(children.values());
    }

    /**
     * Adds a child {@link HierarchyNode} to the current one
     *
     * @param element The child element to add
     * @throws IllegalArgumentException If the current element is
     *     {@link HierarchyNode#isTerminal() terminal}
     * @throws IllegalStateException If an element by the given name is already
     *     a child of the current one
     */
    protected void addChild(HierarchyNode element) throws IllegalArgumentException, IllegalStateException {
        checkArgument(!getChild(element.getName()).isPresent(),
                "Element by name " + element.getName() + " already present in hierarchy");

        children.put(element.getName(), element);
    }

    @Override
    public Iterator<HierarchyNode> iterator() {
        return getChildren().iterator();
    }

}
