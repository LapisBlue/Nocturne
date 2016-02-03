package blue.lapis.nocturne.processor.index;

import static blue.lapis.nocturne.util.helper.StringHelper.resolvePackageName;

import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Primitive;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts and applies a hierarchy to a given {@link JarClassEntry} set.
 */
public class HierarchyBuilder {

    private static final ImmutableSet<IndexedMethod.Signature> IGNORED_METHODS;

    private final ImmutableMap<String, IndexedClass> classes;

    static {
        IGNORED_METHODS = ImmutableSet.<IndexedMethod.Signature>builder()
                .add(new IndexedMethod.Signature("<init>", new MethodDescriptor(new Type(Primitive.VOID, 0))))
                .add(new IndexedMethod.Signature("<clinit>", new MethodDescriptor(new Type(Primitive.VOID, 0))))
                .build();
    }

    public HierarchyBuilder(Set<IndexedClass> classes) {
        this.classes = ImmutableMap.copyOf(classes.stream().collect(Collectors.toMap(IndexedClass::getName, c -> c)));
    }

    public void apply() {
        for (IndexedClass clazz : classes.values()) {
            clazz.getMethods().values().stream()
                    .filter(IGNORED_METHODS::contains)
                    .forEach(method -> {
                        Set<String> bases = getBaseDefinitionClasses(method.getSignature(), clazz);
                        method.getBaseDefinitions().addAll(bases);
                        bases.forEach(base -> classes.get(base).getMethods().get(method.getSignature()).getOverrides()
                                .add(clazz.getName()));
                    });
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
                if (isVisible(superClass.getMethods().get(sig), clazz.getName(), clazz.getSuperclass())) {
                    bases.addAll(getBaseDefinitionClasses(sig, superClass, false));
                }
            }
        }

        clazz.getInterfaces().stream()
                .filter(classes::containsKey)
                .forEach(interfaceName -> {
                    IndexedClass interfaceClass = classes.get(interfaceName);
                    if (interfaceClass.getMethods().containsKey(sig)) {
                        if (isVisible(interfaceClass.getMethods().get(sig), clazz.getName(), interfaceName)) {
                            bases.addAll(getBaseDefinitionClasses(sig, interfaceClass, false));
                        }
                    }
                });

        return !bases.isEmpty() ? bases : returnEmpty ? Collections.EMPTY_SET : Collections.singleton(clazz.getName());
    }

    private static boolean isVisible(IndexedMethod method, String class1, String class2) {
        switch (method.getVisibility()) {
            case PUBLIC:
            case PROTECTED:
                return true;
            case PACKAGE:
                return resolvePackageName(class1).equals(resolvePackageName(class2));
            default:
                return false;
        }
    }

}
