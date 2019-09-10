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

package blue.lapis.nocturne.mapping.model;

import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.helper.ReferenceHelper.explodeScopedClasses;

import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.helper.ReferenceHelper;
import blue.lapis.nocturne.util.helper.StringHelper;
import blue.lapis.nocturne.util.tuple.Pair;

import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.InnerClassReference;
import org.cadixdev.bombe.type.reference.QualifiedReference;
import org.cadixdev.bombe.type.reference.TopLevelClassReference;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

/**
 * Represents a {@link Mapping} for a class.
 */
public abstract class ClassMapping<T extends ClassReference> extends Mapping<T> {

    private final Map<FieldSignature, FieldMapping> fieldMappings = new HashMap<>();
    private final Map<MethodSignature, MethodMapping> methodMappings = new HashMap<>();
    private final Map<InnerClassReference, InnerClassMapping> innerClassMappings = new HashMap<>();

    /**
     * Constructs a new {@link ClassMapping} with the given parameters.
     *
     * @param ref A reference to the mapped item
     * @param deobfName The deobfuscated name of the class
     */
    protected ClassMapping(T ref, String deobfName) {
        super(ref, deobfName);
    }

    public abstract String getFullDeobfuscatedName();

    /**
     * Gets a clone of the {@link FieldMapping}s.
     *
     * @return A clone of the {@link FieldMapping}s
     */
    public Map<FieldSignature, FieldMapping> getFieldMappings() {
        return Collections.unmodifiableMap(this.fieldMappings);
    }

    /**
     * Gets the field mapping with the given signature, if any.
     * @param sig The signature to lookup
     * @return The field mapping, if it exists
     */
    public Optional<FieldMapping> getFieldMapping(FieldSignature sig) {
        return Optional.ofNullable(this.fieldMappings.get(sig));
    }

    /**
     * Gets a clone of the {@link MethodMapping}s.
     *
     * @return A clone of the {@link MethodMapping}s
     */
    public Map<MethodSignature, MethodMapping> getMethodMappings() {
        return Collections.unmodifiableMap(this.methodMappings);
    }

    /**
     * Gets the method mapping with the given signature, if any.
     * @param sig The signature to lookup
     * @return The method mapping, if it exists
     */
    public Optional<MethodMapping> getMethodMapping(MethodSignature sig) {
        return Optional.ofNullable(this.methodMappings.get(sig));
    }

    /**
     * Gets a clone of the {@link InnerClassMapping}s.
     *
     * @return A clone of the {@link InnerClassMapping}s
     */
    public Map<InnerClassReference, InnerClassMapping> getInnerClassMappings() {
        return Collections.unmodifiableMap(this.innerClassMappings);
    }

    /**
     * Gets the inner class mapping with the given signature, if any.
     * @param ref The reference to lookup
     * @return The inner class mapping, if it exists
     */
    public Optional<InnerClassMapping> getInnerClassMapping(InnerClassReference ref) {
        return Optional.ofNullable(this.innerClassMappings.get(ref));
    }

    /**
     * Adds the given {@link FieldMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link FieldMapping} to add
     */
    void addFieldMapping(FieldMapping mapping) {
        mapping.initialize();
        fieldMappings.put(mapping.getReference().getSignature(), mapping);
    }

    /**
     * Removes the {@link FieldMapping} with the given signature from this
     * {@link ClassMapping}.
     *
     * @param fieldSig The signature of the field to remove the mapping of
     */
    public void removeFieldMapping(FieldSignature fieldSig) {
        fieldMappings.remove(fieldSig);
    }

    /**
     * Adds the given {@link MethodMapping} to this {@link ClassMapping}.
     *
     * @param mapping   The {@link MethodMapping} to add
     * @param propagate Whether to propagate this mapping to super- and
     *                  sub-classes
     */
    void addMethodMapping(MethodMapping mapping, boolean propagate) {
        mapping.initialize(propagate);
        methodMappings.put(mapping.getReference().getSignature(), mapping);
    }

    /**
     * Removes the {@link MethodMapping} with the given signature from this
     * {@link ClassMapping}.
     *
     * @param methodSig The signature of the method to remove the mapping of
     */
    public void removeMethodMapping(MethodSignature methodSig) {
        methodMappings.remove(methodSig);
    }

    /**
     * Adds the given {@link InnerClassMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link InnerClassMapping} to add
     */
    void addInnerClassMapping(InnerClassMapping mapping) {
        mapping.initialize();
        innerClassMappings.put(mapping.getReference(), mapping);
    }

    /**
     * Deobfuscates the given class name to the best of the given
     * {@link MappingContext}'s ability.
     *
     * @param context       The {@link MappingContext} to use
     * @param classRef A reference to the class to get the mapping of
     * @return The retrieved or created {@link ClassMapping}
     */
    public static String deobfuscate(MappingContext context, ClassReference classRef) {
        Pair<TopLevelClassReference, List<InnerClassReference>> hierarchy = explodeScopedClasses(classRef);

        // get the mapping of the top-level class
        TopLevelClassMapping rootMapping = context.getMappings().get(hierarchy.first());
        // if it doesn't exist, nothing is deobfuscated yet
        if (rootMapping == null) {
            return classRef.toJvmsIdentifier();
        }

        if (hierarchy.second().isEmpty()) {
            return rootMapping.getFullDeobfuscatedName();
        }

        StringBuilder deobfBuilder = new StringBuilder(rootMapping.getFullDeobfuscatedName());

        ClassMapping<?> curMapping = rootMapping;
        for (InnerClassReference innerRef : hierarchy.second()) {
            deobfBuilder.append(INNER_CLASS_SEPARATOR_CHAR);

            // no more mappings, so just slap on the deobfuscated name of this inner class
            if (curMapping == null) {
                String qualInnerName = innerRef.getClassType().getClassName();
                // we have to unqualify the name
                deobfBuilder.append(qualInnerName.substring(qualInnerName.lastIndexOf(INNER_CLASS_SEPARATOR_CHAR) + 1));
                continue;
            }

            // get the mapping for the current inner class and append its unqualified deobfuscated name
            curMapping = curMapping.getInnerClassMappings().get(innerRef);
            if (curMapping != null) {
                deobfBuilder.append(curMapping.innerClassMappings.get(innerRef).getDeobfuscatedName());
            }
        }

        return deobfBuilder.toString();
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String name, boolean updateClassViews) {
        super.setDeobfuscatedName(name);
        updateEntryDeobfuscation();

        //TODO: we're moving this logic soon anyway
        /*List<SelectableMember> memberList = SelectableMember.MEMBERS.get(getMemberKey());
        if (memberList == null) {
            return;
        }

        String unqualName = this instanceof InnerClassMapping ? name : StringHelper.unqualify(name);
        memberList.forEach(member -> {
            member.setText(unqualName);
            //member.setDeobfuscated(!name.equals(member.getReference()), true);
        });

        if (updateClassViews) {
            MainController.INSTANCE.updateClassViews();
        }*/
    }

    private void updateEntryDeobfuscation() {
        //TODO: moving this logic soon
        /*if (Main.getInstance() != null && Main.getLoadedJar() != null) { // first check is to fix stupid unit tests
            Optional<JarClassEntry> classEntry = Main.getLoadedJar().getClass(getFullObfuscatedName());
            classEntry.ifPresent(jce -> jce.setDeobfuscated(!getObfuscatedName().equals(getDeobfuscatedName())));
        }*/
    }

    @Override
    public String toString() {
        return "{"
                + "ref=" + this.toString() + ";"
                + "deobfName=" + this.getDeobfuscatedName() + ";"
                + "fields=" + this.getFieldMappings() + ";"
                + "methods=" + this.getMethodMappings() + ";"
                + "innerClasses=" + this.getInnerClassMappings()
                + "}";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ClassMapping)) {
            return false;
        }
        final ClassMapping<?> that = (ClassMapping) obj;
        return Objects.equals(this.fieldMappings, that.fieldMappings)
                && Objects.equals(this.methodMappings, that.methodMappings)
                && Objects.equals(this.innerClassMappings, that.innerClassMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.fieldMappings, this.methodMappings, this.innerClassMappings);
    }

}
