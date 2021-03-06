/*
 * Nocturne
 * Copyright (c) 2015-2019, Lapis <https://github.com/LapisBlue>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.util.Constants.CLASS_MEMBER_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_PATTERN;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.ClassSet;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.MethodParameterMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.tuple.Pair;

import org.cadixdev.bombe.type.ArrayType;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.Type;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MemberSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Static utility class for assisting with mapping retrieval and creation.
 */
public final class MappingsHelper {

    public static ClassMapping genClassMapping(MappingContext context, String obf, String deobf,
            boolean updateClassViews) {
        if (!Main.getLoadedJar().getClass(obf).isPresent()) {
            Main.getLogger().warning("Discovered mapping for non-existent class \"" + obf + "\" - ignoring");
            return null;
        } else if (!StringHelper.isJavaClassIdentifier(obf) || !StringHelper.isJavaClassIdentifier(deobf)) {
            Main.getLogger().warning("Discovered class mapping with illegal name - ignoring");
            return null;
        }

        if (obf.contains(INNER_CLASS_SEPARATOR_CHAR + "")) {
            String[] obfSplit = INNER_CLASS_SEPARATOR_PATTERN.split(obf);
            String[] deobfSplit = INNER_CLASS_SEPARATOR_PATTERN.split(deobf);
            if (obfSplit.length != deobfSplit.length) { // non-inner mapped to inner or vice versa
                Main.getLogger().warning("Unsupported mapping: " + obf + " -> " + deobf);
                return null; // ignore it
            }

            // get the direct parent class to this inner class
            ClassMapping parent = getOrCreateClassMapping(context,
                    obf.substring(0, obf.lastIndexOf(INNER_CLASS_SEPARATOR_CHAR)));

            // atomic validation pass
            ClassMapping next = parent;
            for (int i = deobfSplit.length - 2; i >= 0; i--) {
                if (!next.getObfuscatedName().equals(next.getDeobfuscatedName())
                        && !next.getDeobfuscatedName().equals(deobfSplit[i])) {
                    Main.getLogger().warning("Nonsense mapping " + obf + " -> " + deobf
                            + " - conflicts with outer class mapping. Ignoring...");
                    return null;
                }
                if (next instanceof InnerClassMapping) {
                    next = ((InnerClassMapping) next).getParent();
                }
            }

            // application pass
            next = parent;
            for (int i = deobfSplit.length - 2; i >= 0; i--) {
                if (next.getObfuscatedName().equals(next.getDeobfuscatedName())) {
                    next.setDeobfuscatedName(deobfSplit[i]);
                }
                if (next instanceof InnerClassMapping) {
                    next = ((InnerClassMapping) next).getParent();
                }
            }

            String baseObfName = obfSplit[obfSplit.length - 1];
            String baseDeobfname = deobfSplit[deobfSplit.length - 1];
            if (parent.getInnerClassMappings().containsKey(baseObfName)) {
                InnerClassMapping mapping = parent.getInnerClassMappings().get(baseObfName);
                mapping.setDeobfuscatedName(baseDeobfname);
                return mapping;
            } else {
                return new InnerClassMapping(parent, baseObfName, baseDeobfname);
            }
        } else {
            if (context.getMappings().containsKey(obf)) {
                TopLevelClassMapping mapping = context.getMappings().get(obf);
                mapping.setDeobfuscatedName(deobf);
                return mapping;
            } else {
                TopLevelClassMapping mapping = new TopLevelClassMapping(context, obf, deobf);
                context.addMapping(mapping, updateClassViews);
                return mapping;
            }
        }
    }

    public static FieldMapping genFieldMapping(MappingContext context, String owningClass, final FieldSignature sig,
            String deobf) {
        if (!Main.getLoadedJar().getClass(owningClass).isPresent()) {
            Main.getLogger().warning("Discovered mapping for field in non-existent class \"" + owningClass
                    + "\" - ignoring");
            return null;
        } else if (!StringHelper.isJavaIdentifier(sig.getName()) || !StringHelper.isJavaIdentifier(deobf)) {
            Main.getLogger().warning("Discovered field mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping parent = getOrCreateClassMapping(context, owningClass);
        if (parent.getFieldMappings().containsKey(sig)) {
            final FieldMapping fieldMapping = parent.getFieldMappings().get(sig);
            fieldMapping.setDeobfuscatedName(deobf);
            return fieldMapping;
        } else {
            FieldSignature finalSig = sig;
            if (!sig.getType().isPresent()) {
                List<FieldSignature> sigList = IndexedClass.INDEXED_CLASSES.get(owningClass).getFields().keySet()
                        .stream().filter(s -> s.getName().equals(sig.getName())).collect(Collectors.toList());
                if (sigList.size() > 1) {
                    Main.getLogger().warning("Discovered ambiguous field mapping! Ignoring...");
                    return null;
                } else if (sigList.size() == 0) {
                    Main.getLogger().warning("Discovered field mapping for non-existent field - ignoring...");
                    return null;
                }
                finalSig = sigList.get(0);
            }
            return new FieldMapping(parent, finalSig, deobf);
        }
    }

    public static MethodMapping genMethodMapping(MappingContext context, String owningClass, MethodSignature sig,
            String deobf, boolean acceptInitializer) {
        if (!Main.getLoadedJar().getClass(owningClass).isPresent()) {
            Main.getLogger().warning("Discovered mapping for method in non-existent class \"" + owningClass
                    + "\" - ignoring");
            return null;
        } else if (!(sig.getName().equals("<init>") && acceptInitializer && sig.getName().equals(deobf))
                && (!StringHelper.isJavaIdentifier(sig.getName()) || !StringHelper.isJavaIdentifier(deobf))) {
            Main.getLogger().warning("Discovered method mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping parent = getOrCreateClassMapping(context, owningClass);
        if (parent.getMethodMappings().containsKey(sig)) {
            final MethodMapping methodMapping = parent.getMethodMappings().get(sig);
            methodMapping.setDeobfuscatedName(deobf);
            return methodMapping;
        } else {
            return new MethodMapping(parent, sig, deobf);
        }
    }

    public static void genArgumentMapping(MappingContext context, MethodMapping methodMapping, int index,
            String deobf) {
        if (!StringHelper.isJavaIdentifier(deobf)) {
            Main.getLogger().warning("Discovered argument mapping with illegal name - ignoring");
            return;
        }

        Optional<MethodParameterMapping> mapping = methodMapping.getParamMappings().values().stream()
                .filter(argumentMapping -> argumentMapping.getIndex() == index).findFirst();
        if (mapping.isPresent()) {
            mapping.get().setDeobfuscatedName(deobf);
        } else {
            new MethodParameterMapping(methodMapping, index, deobf, true);
        }
    }

    private static Optional<ClassMapping> getClassMapping(MappingContext context, String qualifiedName,
            boolean create) {
        String[] arr = INNER_CLASS_SEPARATOR_PATTERN.split(qualifiedName);

        ClassMapping mapping = context.getMappings().get(arr[0]);
        if (mapping == null) {
            if (create) {
                mapping = new TopLevelClassMapping(context, arr[0], arr[0]);
                context.addMapping((TopLevelClassMapping) mapping, false);
            } else {
                return Optional.empty();
            }
        }

        for (int i = 1; i < arr.length; i++) {
            ClassMapping child = mapping.getInnerClassMappings().get(arr[i]);
            if (child == null) {
                if (create) {
                    child = new InnerClassMapping(mapping, arr[i], arr[i]);
                } else {
                    return Optional.empty();
                }
            }
            mapping = child;
        }

        return Optional.of(mapping);
    }

    public static Optional<ClassMapping> getClassMapping(MappingContext context, String qualifiedName) {
        return getClassMapping(context, qualifiedName, false);
    }

    /**
     * Gets the {@link ClassMapping} for the given qualified name, iteratively
     * creating mappings for both outer and inner classes as needed if they do
     * not exist.
     *
     * @param context       The {@link MappingContext} to use
     * @param qualifiedName The fully-qualified name of the class to get a
     *                      mapping for
     * @return The retrieved or created {@link ClassMapping}
     */
    public static ClassMapping getOrCreateClassMapping(MappingContext context, String qualifiedName) {
        return getClassMapping(context, qualifiedName, true).get();
    }

    private static ObjectType deobfuscateObject(final MappingContext ctx, final ObjectType objType) {
        final Optional<ClassMapping> typeMapping = getClassMapping(ctx, objType.getClassName());
        return new ObjectType(typeMapping.map(ClassMapping::getFullDeobfuscatedName).orElse(objType.getClassName()));
    }

    public static FieldType deobfuscateField(final MappingContext ctx, final FieldType fieldType) {
        if (fieldType instanceof ObjectType) {
            return deobfuscateObject(ctx, (ObjectType) fieldType);
        } else if (fieldType instanceof ArrayType
                && ((ArrayType) fieldType).getComponent() instanceof ObjectType) {
            final ArrayType arr = (ArrayType) fieldType;
            final ObjectType obj = (ObjectType) arr.getComponent();
            return new ArrayType(arr.getDimCount(), deobfuscateObject(ctx, obj));
        }
        return fieldType;
    }

    public static Type deobfuscate(final MappingContext ctx, final Type obfuscatedType) {
        if (obfuscatedType instanceof FieldType) {
            return deobfuscateField(ctx, (FieldType) obfuscatedType);
        }
        return obfuscatedType;
    }

    public static MethodDescriptor deobfuscate(final MappingContext ctx, final MethodDescriptor obfDesc) {
        return new MethodDescriptor(
                obfDesc.getParamTypes().stream().map(type -> deobfuscateField(ctx, type)).collect(Collectors.toList()),
                deobfuscate(ctx, obfDesc.getReturnType())
        );
    }

    /**
     * Returns whether an item with the given remapped name already exists.
     *
     * @param classes The {@link ClassSet} to search
     * @param originalQualifiedName The original, fully qualified name of the
     *     item
     * @param signature The signature of the item
     * @param remappedName The prospective remapped name of the item
     * @param type The item's type
     * @return Whether the remapped name clashes, and whether that clash occurs
     *     within the same class or in the class hierarchy
     */
    public static Pair<Boolean, Boolean> doesRemappedNameClash(ClassSet classes, String originalQualifiedName,
            @Nullable MemberSignature signature, String remappedName, MemberType type) {
        switch (type) {
            case CLASS:
                return Pair.of(classes.getCurrentNames().containsValue(remappedName), false);
            case INNER_CLASS:
                assert originalQualifiedName.contains(INNER_CLASS_SEPARATOR_CHAR + "");
                return Pair.of(Main.getLoadedJar()
                        .getClass(originalQualifiedName
                                .substring(0, originalQualifiedName.lastIndexOf(INNER_CLASS_SEPARATOR_CHAR)))
                        .map(jarClassEntry -> jarClassEntry.getCurrentInnerClassNames().containsValue(remappedName))
                        .orElse(false),
                        false);
            case FIELD: {
                String parentClass = originalQualifiedName
                        .substring(0, originalQualifiedName.lastIndexOf(CLASS_MEMBER_SEPARATOR_CHAR));
                JarClassEntry jce = Main.getLoadedJar().getClass(parentClass).get();
                FieldSignature newSig = new FieldSignature(remappedName,
                        ((FieldSignature) signature).getType().orElse(null));
                return Pair.of(jce.getCurrentFields().containsValue(newSig), false);
            }
            case METHOD: {
                String parentClass = originalQualifiedName
                        .substring(0, originalQualifiedName.lastIndexOf(CLASS_MEMBER_SEPARATOR_CHAR));
                Set<JarClassEntry> hierarchy = HierarchyHelper.getClassesInHierarchy(parentClass,
                        (MethodSignature) signature)
                        .stream().filter(c -> Main.getLoadedJar().getClass(c).isPresent())
                        .map(c -> Main.getLoadedJar().getClass(c).get()).collect(Collectors.toSet());
                for (JarClassEntry jce : hierarchy) {
                    MethodSignature newSig
                            = new MethodSignature(remappedName, ((MethodSignature) signature).getDescriptor());
                    if (jce.getCurrentMethods().containsValue(newSig)) {
                        return Pair.of(true, !jce.getName().equals(parentClass));
                    }
                }
                return Pair.of(false, false);
            }
            default:
                throw new AssertionError("Unhandled type " + type);
        }
    }

}
