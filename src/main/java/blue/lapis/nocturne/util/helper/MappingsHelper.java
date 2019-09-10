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

import static blue.lapis.nocturne.util.helper.ReferenceHelper.createClassReference;
import static blue.lapis.nocturne.util.helper.ReferenceHelper.getDisplayName;
import static blue.lapis.nocturne.util.helper.StringHelper.looksDeobfuscated;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.ClassSet;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MemberMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.MethodParameterMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.tuple.Pair;

import org.cadixdev.bombe.type.ArrayType;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.Type;
import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.FieldReference;
import org.cadixdev.bombe.type.reference.InnerClassReference;
import org.cadixdev.bombe.type.reference.MemberReference;
import org.cadixdev.bombe.type.reference.MethodParameterReference;
import org.cadixdev.bombe.type.reference.MethodReference;
import org.cadixdev.bombe.type.reference.QualifiedReference;
import org.cadixdev.bombe.type.reference.TopLevelClassReference;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MemberSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Static utility class for assisting with mapping retrieval and creation.
 */
public final class MappingsHelper {

    public static ClassMapping<?> genClassMapping(MappingContext context, ClassReference ref, String deobf) {
        if (!StringHelper.isJavaClassIdentifier(ref.toJvmsIdentifier())
                || (deobf != null && !StringHelper.isJavaClassIdentifier(deobf))) {
            Main.getLogger().warning("Discovered class mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping<?> mapping = getOrCreateClassMapping(context, ref);
        mapping.setDeobfuscatedName(deobf);

        return mapping;
    }

    public static FieldMapping genFieldMapping(MappingContext context, FieldReference ref, @Nullable String deobf) {
        if (!Main.getLoadedJar().getClass(ref.getOwningClass()).isPresent()) {
            Main.getLogger().warning("Discovered mapping for field in non-existent class \""
                    + ref.getOwningClass().toJvmsIdentifier() + "\" - ignoring");
            return null;
        } else if (!StringHelper.isJavaIdentifier(ref.getSignature().getName())
                || (deobf != null && !StringHelper.isJavaIdentifier(deobf))) {
            Main.getLogger().warning("Discovered field mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping<?> parent = getOrCreateClassMapping(context, ref.getOwningClass());

        final Optional<FieldMapping> fieldMapping = parent.getFieldMapping(ref.getSignature());
        if (fieldMapping.isPresent()) {
            fieldMapping.get().setDeobfuscatedName(deobf);
            return fieldMapping.get();
        } else {
            if (ref.getSignature().getType().isPresent()) {
                return new FieldMapping(parent, ref, deobf);
            } else {
                List<FieldSignature> sigList = IndexedClass.INDEXED_CLASSES.get(ref.getOwningClass()).getFields()
                        .keySet()
                        .stream()
                        .filter(s -> s.getName().equals(ref.getSignature().getName()))
                        .collect(Collectors.toList());
                if (sigList.size() > 1) {
                    Main.getLogger().warning("Discovered ambiguous field mapping! Ignoring...");
                    return null;
                } else if (sigList.size() == 0) {
                    Main.getLogger().warning("Discovered field mapping for non-existent field - ignoring...");
                    return null;
                }
                return new FieldMapping(parent, new FieldReference(ref.getOwningClass(), sigList.get(0)), deobf);
            }
        }
    }

    public static MethodMapping genMethodMapping(MappingContext context, MethodReference ref,
            String deobf, boolean acceptInitializer) {
        if (!Main.getLoadedJar().getClass(ref.getOwningClass()).isPresent()) {
            Main.getLogger().warning("Discovered mapping for method in non-existent class \""
                    + ref.getOwningClass().toJvmsIdentifier() + "\" - ignoring");
            return null;
        } else if (!(
                ref.getSignature().getName().equals("<init>")
                        && acceptInitializer
                        && ref.getSignature().getName().equals(deobf))
                && (
                !StringHelper.isJavaIdentifier(ref.getSignature().getName())
                        || !StringHelper.isJavaIdentifier(deobf))) {
            Main.getLogger().warning("Discovered method mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping<?> parent = getOrCreateClassMapping(context, ref.getOwningClass());
        Optional<MethodMapping> methodMapping = parent.getMethodMapping(ref.getSignature());
        if (methodMapping.isPresent()) {
            methodMapping.get().setDeobfuscatedName(deobf);
            return methodMapping.get();
        } else {
            return new MethodMapping(parent, ref, deobf);
        }
    }

    public static MethodParameterMapping genMethodParamMapping(MappingContext context, MethodParameterReference ref,
            String deobf) {
        if (!StringHelper.isJavaIdentifier(deobf)) {
            Main.getLogger().warning("Discovered argument mapping with illegal name - ignoring");
            return null;
        }

        ClassMapping<?> classMapping = getOrCreateClassMapping(context, ref.getParentMethod().getOwningClass());

        MethodMapping methodMapping;
        if (classMapping.getMethodMappings().containsKey(ref.getParentMethod().getSignature())) {
            methodMapping = classMapping.getMethodMappings().get(ref.getParentMethod().getSignature());
        } else {
            methodMapping = genMethodMapping(context, ref.getParentMethod(), null, true);

            if (methodMapping == null) {
                return null;
            }
        }

        Optional<MethodParameterMapping> mapping = methodMapping.getParamMappings().values().stream()
                .filter(paramMapping -> paramMapping.getReference().equals(ref)).findFirst();
        if (mapping.isPresent()) {
            mapping.get().setDeobfuscatedName(deobf);
            return mapping.get();
        } else {
            return new MethodParameterMapping(methodMapping, ref, deobf, true);
        }
    }

    public static Optional<? extends Mapping<?>> getMapping(MappingContext context, QualifiedReference ref,
            boolean create) {
        switch (ref.getType()) {
            case TOP_LEVEL_CLASS:
            case INNER_CLASS:
                assert ref instanceof ClassReference;
                return getClassMapping(context, (ClassReference) ref, create);
            case FIELD:
                assert ref instanceof FieldReference;
                return getFieldMapping(context, (FieldReference) ref, create);
            case METHOD:
                assert ref instanceof MethodReference;
                return getMethodMapping(context, (MethodReference) ref, create);
            case METHOD_PARAMETER:
                assert ref instanceof MethodParameterReference;
                return getMethodParamMapping(context, (MethodParameterReference) ref, create);
            default:
                throw new AssertionError("Unhandled case " + ref.getType().name());
        }
    }

    public static Mapping<?> getOrCreateMapping(MappingContext context, QualifiedReference ref) {
        return getMapping(context, ref, true).get();
    }

    public static Optional<ClassMapping<?>> getClassMapping(MappingContext context, ClassReference classRef,
            boolean create) {
        if (classRef.getType() == QualifiedReference.Type.TOP_LEVEL_CLASS) {
            assert classRef instanceof TopLevelClassReference;

            TopLevelClassMapping mapping = context.getMappings().get(classRef);
            if (mapping == null && create) {
                mapping = new TopLevelClassMapping(context, (TopLevelClassReference) classRef, null);
                context.addMapping(mapping);
            }

            return Optional.ofNullable(mapping);
        } else {
            assert classRef.getType() == QualifiedReference.Type.INNER_CLASS;
            assert classRef instanceof InnerClassReference;

            InnerClassReference innerClassRef = (InnerClassReference) classRef;

            Optional<ClassMapping<?>> parentMapping
                    = getClassMapping(context, (innerClassRef).getParentClass(), create);

            if (!parentMapping.isPresent()) {
                return Optional.empty();
            }

            Optional<InnerClassMapping> mapping = parentMapping.get().getInnerClassMapping(innerClassRef);

            if (!mapping.isPresent() && create) {
                return Optional.of(new InnerClassMapping(parentMapping.get(), (InnerClassReference) classRef, null));
            } else {
                return Optional.ofNullable(mapping.orElse(null));
            }
        }
    }

    public static ClassMapping<?> getOrCreateClassMapping(MappingContext context, ClassReference owningClass) {
        Optional<ClassMapping<?>> mapping = getClassMapping(context, owningClass, true);
        assert mapping.isPresent();
        return mapping.get();
    }

    public static Optional<FieldMapping> getFieldMapping(MappingContext context, FieldReference ref,
            boolean create) {
        return getMemberMapping(context, ref, create, ClassMapping::getFieldMapping,
                fr -> genFieldMapping(context, fr, null));
    }

    public static Optional<MethodMapping> getMethodMapping(MappingContext context, MethodReference ref,
            boolean create) {
        return getMemberMapping(context, ref, false, ClassMapping::getMethodMapping,
                fr -> genMethodMapping(context, fr, null, true));
    }

    private static <S extends MemberSignature, R extends MemberReference<S>, M extends MemberMapping<R>>
    Optional<M> getMemberMapping(MappingContext context, R ref, boolean create,
            BiFunction<ClassMapping<?>, S, Optional<M>> getter, Function<R, M> genner) {
        Optional<ClassMapping<?>> classMapping
                = getClassMapping(context, ref.getOwningClass(), create);
        if (!classMapping.isPresent()) {
            return Optional.empty();
        }

        Optional<M> mapping = getter.apply(classMapping.get(), ref.getSignature());

        if (mapping.isPresent()) {
            return mapping;
        } else if (create) {
            return Optional.ofNullable(genner.apply(ref));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<MethodParameterMapping> getMethodParamMapping(MappingContext context,
            MethodParameterReference ref, boolean create) {
        Optional<MethodMapping> methodMapping = getMethodMapping(context, ref.getParentMethod(), create);
        if (!methodMapping.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(genMethodParamMapping(context, ref, null));
    }

    private static ObjectType deobfuscateObject(final MappingContext ctx, final ObjectType objType) {
        final Optional<ClassMapping<?>> typeMapping = getClassMapping(ctx, createClassReference(objType), false);
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
     * @param ref A reference to the item being remapped
     * @param remappedName The prospective remapped name of the item
     * @return Whether the remapped name clashes, and whether that clash occurs
     *     within the same class or in the class hierarchy
     */
    public static Pair<Boolean, Boolean> doesRemappedNameClash(ClassSet classes, QualifiedReference ref,
            String remappedName) {
        switch (ref.getType()) {
            case TOP_LEVEL_CLASS:
                return Pair.of(classes.getCurrentNames().containsValue(remappedName), false);
            case INNER_CLASS:
                //TODO: this is broken
                return Pair.of(Main.getLoadedJar()
                                .getClass(((InnerClassReference) ref).getParentClass())
                                .map(jarClassEntry -> jarClassEntry.getCurrentInnerClassNames().containsValue(remappedName))
                                .orElse(false),
                        false);
            case FIELD: {
                FieldReference fieldRef = (FieldReference) ref;

                ClassReference owningClass = fieldRef.getOwningClass();
                JarClassEntry jce = Main.getLoadedJar().getClass(owningClass).get();

                FieldSignature newSig = new FieldSignature(remappedName,
                        fieldRef.getSignature().getType().orElse(null));
                return Pair.of(jce.getCurrentFields().containsValue(newSig), false);
            }
            case METHOD: {
                MethodReference methodRef = (MethodReference) ref;

                ClassReference owningClass = methodRef.getOwningClass();
                Set<JarClassEntry> hierarchy = HierarchyHelper.getClassesInHierarchy(owningClass,
                        methodRef.getSignature())
                        .stream().filter(c -> Main.getLoadedJar().getClass(c).isPresent())
                        .map(c -> Main.getLoadedJar().getClass(c).get()).collect(Collectors.toSet());
                for (JarClassEntry jce : hierarchy) {
                    MethodSignature newSig
                            = new MethodSignature(remappedName, methodRef.getSignature().getDescriptor());
                    if (jce.getCurrentMethods().containsValue(newSig)) {
                        return Pair.of(true, !jce.getReference().equals(methodRef.getOwningClass()));
                    }
                }
                return Pair.of(false, false);
            }
            default:
                throw new AssertionError("Unhandled type " + ref.getType());
        }
    }

    public static Optional<String> getDeobfuscatedName(MappingContext context, QualifiedReference ref) {
        Optional<? extends Mapping> mappingOpt = getMapping(context, ref, false);
        return mappingOpt.map(Mapping::getDeobfuscatedName);
    }

    public static boolean isDeobfuscated(QualifiedReference ref) {
        //TODO: is this method too much of a hack?

        Optional<? extends Mapping<?>> mapping = MappingsHelper.getMapping(Main.getMappingContext(), ref, false);

        if (mapping.isPresent()) {
            return mapping.get().isAdHoc() || !getDisplayName(ref).equals(mapping.get().getDeobfuscatedName());
        }

        if (ref.getType() == QualifiedReference.Type.METHOD_PARAMETER) {
            return false;
        }

        return looksDeobfuscated(getDisplayName(ref));
    }
}
