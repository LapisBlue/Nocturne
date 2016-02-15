/*
 * Nocturne
 * Copyright (c) 2015-2016, Lapis <https://github.com/LapisBlue>
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

import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_PATTERN;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a {@link Mapping} for a class.
 */
public abstract class ClassMapping extends Mapping {

    private final Map<String, FieldMapping> fieldMappings = new HashMap<>();
    private final Map<String, MethodMapping> methodMappings = new HashMap<>();
    private final Map<String, InnerClassMapping> innerClassMappings = new HashMap<>();

    /**
     * Constructs a new {@link ClassMapping} with the given parameters.
     *
     * @param obfName The obfuscated name of the class
     * @param deobfName The deobfuscated name of the class
     */
    protected ClassMapping(String obfName, String deobfName) {
        super(obfName, deobfName);
    }

    public abstract String getFullObfuscatedName();

    public abstract String getFullDeobfuscatedName();

    /**
     * Gets a clone of the {@link FieldMapping}s.
     *
     * @return A clone of the {@link FieldMapping}s
     */
    public ImmutableMap<String, FieldMapping> getFieldMappings() {
        return ImmutableMap.copyOf(this.fieldMappings);
    }

    /**
     * Gets a clone of the {@link MethodMapping}s.
     *
     * @return A clone of the {@link MethodMapping}s
     */
    public ImmutableMap<String, MethodMapping> getMethodMappings() {
        return ImmutableMap.copyOf(this.methodMappings);
    }

    /**
     * Gets a clone of the {@link InnerClassMapping}s.
     *
     * @return A clone of the {@link InnerClassMapping}s
     */
    public ImmutableMap<String, InnerClassMapping> getInnerClassMappings() {
        return ImmutableMap.copyOf(this.innerClassMappings);
    }

    /**
     * Adds the given {@link FieldMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link FieldMapping} to add
     */
    void addFieldMapping(FieldMapping mapping) {
        mapping.initialize();
        fieldMappings.put(mapping.getObfuscatedName(), mapping);
        getContext().setDirty(true);
    }

    /**
     * Removes the {@link FieldMapping} by the given name from this
     * {@link ClassMapping}.
     *
     * @param fieldName The name of the field to remove the mapping of
     */
    public void removeFieldMapping(String fieldName) {
        fieldMappings.remove(fieldName);
    }

    /**
     * Adds the given {@link MethodMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link MethodMapping} to add
     * @param propagate Whether to propagate this mapping to super- and
     *     sub-classes
     */
    void addMethodMapping(MethodMapping mapping, boolean propagate) {
        mapping.initialize(propagate);
        methodMappings.put(mapping.getObfuscatedName() + mapping.getObfuscatedDescriptor(), mapping);
    }

    /**
     * Removes the {@link MethodMapping} by the given name from this
     * {@link ClassMapping}.
     *
     * @param methodName The name of the method to remove the mapping of
     */
    public void removeMethodMapping(String methodName) {
        methodMappings.remove(methodName);
    }

    /**
     * Adds the given {@link InnerClassMapping} to this {@link ClassMapping}.
     *
     * @param mapping The {@link InnerClassMapping} to add
     */
    void addInnerClassMapping(InnerClassMapping mapping) {
        mapping.initialize();
        innerClassMappings.put(mapping.getObfuscatedName(), mapping);

        getContext().setDirty(true);
    }

    /**
     * Deobfuscates the given class name to the best of the given
     * {@link MappingContext}'s ability.
     *
     * @param context The {@link MappingContext} to use
     * @param qualifiedName The fully-qualified name of the class to get a
     *     mapping for
     * @return The retrieved or created {@link ClassMapping}
     */
    public static String deobfuscate(MappingContext context, String qualifiedName) {
        String[] arr = INNER_CLASS_SEPARATOR_PATTERN.split(qualifiedName);

        ClassMapping mapping = context.getMappings().get(arr[0]);
        if (mapping == null) {
            return qualifiedName;
        }

        String deobfName = mapping.getDeobfuscatedName();
        for (int i = 1; i < arr.length; i++) {
            ClassMapping child = mapping.getInnerClassMappings().get(arr[i]);
            if (child == null) {
                for (; i < arr.length; i++) {
                    deobfName += "$" + arr[i];
                }
                break;
            }
            deobfName += "$" + child.getDeobfuscatedName();
            mapping = child;
        }

        return deobfName;
    }

    @Override
    public void setDeobfuscatedName(String name) {
        setDeobfuscatedName(name, true);
    }

    public void setDeobfuscatedName(String name, boolean updateClassViews) {
        super.setDeobfuscatedName(name);
        updateEntryDeobfuscation();

        List<SelectableMember> memberList = SelectableMember.MEMBERS.get(getMemberKey());
        if (memberList == null) {
            return;
        }

        String unqualName = this instanceof InnerClassMapping ? name : MappingsHelper.unqualify(name);
        memberList.forEach(member -> {
            member.setText(unqualName);
            member.setDeobfuscated(!unqualName.equals(member.getName()));
        });

        if (updateClassViews) {
            MainController.INSTANCE.updateClassViews();
        }
    }

    public void updateEntryDeobfuscation() {
        if (Main.getInstance() != null && Main.getLoadedJar() != null) { // first check is to fix stupid unit tests
            Optional<JarClassEntry> classEntry = Main.getLoadedJar().getClass(getFullObfuscatedName());
            if (classEntry.isPresent()) {
                classEntry.get().setDeobfuscated(!getObfuscatedName().equals(getDeobfuscatedName()));
            }
        }
    }

}
