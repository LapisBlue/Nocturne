package blue.lapis.nocturne.processor.index;

import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts and applies a hierarchy to a given {@link JarClassEntry} set.
 */
public class HierarchyBuilder {

    private final ImmutableMap<String, IndexedClass> classes;

    public HierarchyBuilder(Set<IndexedClass> classes) {
        this.classes = ImmutableMap.copyOf(classes.stream().collect(Collectors.toMap(IndexedClass::getName, c -> c)));
    }

    public void apply() {
        for (IndexedClass clazz : classes.values()) {
            for (IndexedMethod method : clazz.getMethods().values()) {
                Set<String> bases = getBaseDefinitionClasses(method.getSignature(), clazz);
                method.getBaseDefinitions().addAll(bases);
                bases.forEach(base -> classes.get(base).getMethods().get(method.getSignature()).getOverrides()
                        .add(clazz.getName()));
            }
        }
    }

    private Set<String> getBaseDefinitionClasses(IndexedMethod.Signature sig, IndexedClass clazz) {
        return getBaseDefinitionClasses(sig, clazz, true);
    }

    private Set<String> getBaseDefinitionClasses(IndexedMethod.Signature sig, IndexedClass clazz, boolean returnEmpty) {
        Set<String> bases = new HashSet<>();

        if (classes.containsKey(clazz.getSuperclass())) {
            IndexedClass superClass = classes.get(clazz.getSuperclass());
            if (superClass.getMethods().containsKey(sig)) {
                bases.addAll(getBaseDefinitionClasses(sig, superClass, false));
            }
        }

        clazz.getInterfaces().stream()
                .filter(classes::containsKey)
                .forEach(interfaceName -> {
                    IndexedClass interfaceClass = classes.get(interfaceName);
                    if (interfaceClass.getMethods().containsKey(sig)) {
                        bases.addAll(getBaseDefinitionClasses(sig, interfaceClass, false));
                    }
                });

        return !bases.isEmpty() ? bases : returnEmpty ? Collections.EMPTY_SET : Collections.singleton(clazz.getName());
    }

}
