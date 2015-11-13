package blue.lapis.shroud.mapping;

import blue.lapis.shroud.mapping.model.ClassMapping;
import blue.lapis.shroud.mapping.model.TopLevelClassMapping;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of {@link ClassMapping}s.
 */
public class MappingSet {

    private final Map<String, TopLevelClassMapping> mappings = new HashMap<>();

    /**
     * Returns an {@link ImmutableMap} of all {@link ClassMapping}s contained by
     * this {@link MappingSet}.
     *
     * @return An {@link ImmutableMap} of all {@link ClassMapping}s contained by
     *     this {@link MappingSet}
     */
    public ImmutableMap<String, ClassMapping> getMappings() {
        return ImmutableMap.copyOf(mappings);
    }

    /**
     * Adds the given {@link TopLevelClassMapping} to this {@link MappingSet}.
     *
     * @param mapping The {@link TopLevelClassMapping} to add
     */
    public void addMapping(TopLevelClassMapping mapping) {
        mappings.put(mapping.getObfuscatedName(), mapping);
    }

}
