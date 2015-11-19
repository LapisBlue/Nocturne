package blue.lapis.nocturne.analysis.model.hierarchy;

import blue.lapis.nocturne.util.Constants;

import java.util.Set;

/**
 * Represents a complete hierarchy.
 */
public class Hierarchy extends HierarchyElement {

    /**
     * Generates a hierarchy from the given string {@link Set}.
     *
     * @param strings The strings to generate a hierarchy from
     * @return The generated entry {@link HierarchyNode}
     */
    public static Hierarchy fromSet(Set<String> strings) {
        Hierarchy root = new Hierarchy();

        for (String str : strings) {
            String[] arr = Constants.CLASS_PATH_SEPARATOR_PATTERN.split(str);

            HierarchyElement parent = root;
            for (int i = 0; i < arr.length - 1; i++) {
                if (parent != null && parent.getChild(arr[i]).isPresent()) {
                    parent = parent.getChild(arr[i]).get();
                } else {
                    parent = new HierarchyNode(arr[i], false, parent);
                }
            }
            new HierarchyNode(arr[arr.length - 1], true, parent);
        }

        return root;
    }

}
