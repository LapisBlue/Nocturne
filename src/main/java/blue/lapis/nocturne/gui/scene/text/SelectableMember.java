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

package blue.lapis.nocturne.gui.scene.text;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.DOT_PATTERN;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_PREFIX;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MemberMapping;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.signature.FieldSignature;
import blue.lapis.nocturne.processor.index.model.signature.MemberSignature;
import blue.lapis.nocturne.processor.index.model.signature.MethodSignature;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.HierarchyHelper;
import blue.lapis.nocturne.util.helper.MappingsHelper;
import blue.lapis.nocturne.util.helper.StringHelper;

import com.google.common.collect.Sets;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Represents a selectable member in code.
 */
public class SelectableMember extends Text {

    public static final Map<MemberKey, List<SelectableMember>> MEMBERS = new HashMap<>();

    private final CodeTab codeTab;
    private final MemberType type;

    private final StringProperty nameProperty = new SimpleStringProperty(this, "name");
    private final StringProperty descriptorProperty = new SimpleStringProperty(this, "descriptor");
    private final StringProperty parentClassProperty = new SimpleStringProperty(this, "parentClass");

    private final MemberSignature sig;

    private String fullName = null; // only used for classes

    public SelectableMember(CodeTab codeTab, MemberType type, String name) {
        this(codeTab, type, name, null, null);
    }

    public SelectableMember(CodeTab codeTab, MemberType type, String name, String descriptor, String parentClass) {
        super(name);
        this.codeTab = codeTab;
        this.type = type;
        this.nameProperty.set(name);
        this.descriptorProperty.set(descriptor);

        if (type == MemberType.FIELD) {
            this.sig = new FieldSignature(name, Type.fromString(descriptor));
        } else if (type == MemberType.METHOD) {
            this.sig = new MethodSignature(name, MethodDescriptor.fromString(descriptor));
        } else {
            this.sig = null;
        }

        this.parentClassProperty.set(parentClass);

        if (type == MemberType.CLASS) {
            fullName = getName();
        }

        this.setOnMouseClicked(event1 -> {
            if (event1.getButton() == MouseButton.PRIMARY) {
                this.updateCodeTab();
            }
        });

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> {
            String dispText = this.getText();
            if (getType() == MemberType.CLASS && !isInnerClass()) {
                dispText = fullName;
            }
            TextInputDialog textInputDialog = new TextInputDialog(dispText);
            textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));

            Optional<String> result = textInputDialog.showAndWait();
            if (result.isPresent() && !result.get().equals("") && !result.get().equals(getText())) {
                if ((getType() == MemberType.CLASS && !isInnerClass() && !checkClassDupe(result.get()))
                        || ((getType() != MemberType.CLASS || isInnerClass()) && !checkMemberDupe(result.get()))) {
                    return;
                }

                String res = result.get();
                if (getType() == MemberType.CLASS) {
                    res = DOT_PATTERN.matcher(res).replaceAll(CLASS_PATH_SEPARATOR_CHAR + "");
                }
                if ((getType() == MemberType.CLASS && !StringHelper.isJavaClassIdentifier(res))
                        || (getType() != MemberType.CLASS && !StringHelper.isJavaIdentifier(res))) {
                    showIllegalAlert();
                    return;
                }
                this.setMapping(res);
            }
        });

        MenuItem resetItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.reset"));
        resetItem.setOnAction(event -> {
            if (getText().equals(getName())) {
                return;
            }
            switch (getType()) {
                case CLASS: {
                    Optional<ClassMapping> mapping
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getName());
                    if (mapping.isPresent()
                            && !mapping.get().getObfuscatedName().equals(mapping.get().getDeobfuscatedName())) {
                        if ((!isInnerClass() && !checkClassDupe(mapping.get().getObfuscatedName()))
                                || (isInnerClass() && !checkMemberDupe(mapping.get().getObfuscatedName()))) {
                            break;
                        }
                        mapping.get().setDeobfuscatedName(mapping.get().getObfuscatedName());
                    }
                    fullName = getName();
                    break;
                }
                case FIELD:
                case METHOD: {
                    Optional<ClassMapping> parent
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getParentClass());
                    if (parent.isPresent()) {
                        MemberMapping mapping = getType() == MemberType.FIELD
                                ? parent.get().getFieldMappings().get(getName())
                                : parent.get().getMethodMappings().get(getName() + getDescriptor());
                        if (mapping != null) {
                            if (!checkMemberDupe(mapping.getObfuscatedName())) {
                                return;
                            }
                            mapping.setDeobfuscatedName(mapping.getObfuscatedName());
                        }
                    }
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }

            this.updateCodeTab();
        });

        MenuItem jumpToDefItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.jumpToDef"));
        jumpToDefItem.setOnAction(event -> {
            String className = getType() == MemberType.CLASS ? getName() : getParentClass();
            if (className.contains(INNER_CLASS_SEPARATOR_CHAR + "")) {
                className = className.substring(0, className.indexOf(INNER_CLASS_SEPARATOR_CHAR));
            }

            Optional<ClassMapping> cm = MappingsHelper.getClassMapping(Main.getMappingContext(), className);
            MainController.INSTANCE.openTab(className, cm.isPresent() ? cm.get().getDeobfuscatedName() : className);
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(renameItem);
        contextMenu.getItems().add(resetItem);
        contextMenu.getItems().add(jumpToDefItem);

        this.setOnContextMenuRequested(event ->
                contextMenu.show(SelectableMember.this, event.getScreenX(), event.getScreenY()));

        String qualName;
        IndexedClass ic = IndexedClass.INDEXED_CLASSES.get(getParentClass());
        switch (type) {
            case CLASS:
                qualName = name;
                break;
            case FIELD:
                //noinspection SuspiciousMethodCalls: sig must be a FieldSignature object
                if (!ic.getFields().containsKey(sig)) {
                    throw new IllegalArgumentException();
                }
                qualName = getParentClass() + CLASS_PATH_SEPARATOR_CHAR + name;
                break;
            case METHOD:
                String parent = null;
                //noinspection SuspiciousMethodCalls: sig must be a MethodSignature object
                if (ic.getMethods().containsKey(sig)) {
                    parent = getParentClass();
                } else {
                    for (IndexedClass hc : ic.getHierarchy()) {
                        //noinspection SuspiciousMethodCalls: sig must be a MethodSignature object
                        if (hc.getMethods().containsKey(sig)) {
                            parent = hc.getName();
                            break;
                        }
                    }
                }
                if (parent == null) {
                    throw new IllegalArgumentException(); //TODO
                }
                qualName = parent + CLASS_PATH_SEPARATOR_CHAR + name;
                break;
            default:
                throw new AssertionError();
        }
        //TODO: we're ignoring field descriptors for now since SRG doesn't support them
        MemberKey key = new MemberKey(type, qualName,
                type == MemberType.FIELD || type == MemberType.METHOD ? descriptor : null);
        if (!MEMBERS.containsKey(key)) {
            MEMBERS.put(key, new ArrayList<>());
        }
        MEMBERS.get(key).add(this);

        updateText();

        setDeobfuscated(!getName().equals(getText()));
    }

    private boolean checkClassDupe(String newName) {
        if (Main.getLoadedJar().getCurrentNames().containsValue(newName)) {
            showDupeAlert(false);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkMemberDupe(String newName) {
        switch (getType()) {
            case CLASS: {
                assert getName().contains(INNER_CLASS_SEPARATOR_CHAR + "");
                Optional<JarClassEntry> jce = Main.getLoadedJar().getClass(getName()
                        .substring(0, getName().lastIndexOf(INNER_CLASS_SEPARATOR_CHAR)));
                if (jce.isPresent()) {
                    if (jce.get().getCurrentInnerClassNames().containsValue(newName)) {
                        showDupeAlert(false);
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            case FIELD: {
                JarClassEntry jce = Main.getLoadedJar().getClass(getParentClass()).get();
                FieldSignature newSig = new FieldSignature(newName,
                        ((FieldSignature) sig).getType());
                if (jce.getCurrentFields().containsValue(newSig)) {
                    showDupeAlert(false);
                    return false;
                } else {
                    return true;
                }
            }
            case METHOD: {
                Set<JarClassEntry> hierarchy = HierarchyHelper.getClassesInHierarchy(getParentClass(),
                        (MethodSignature) sig)
                        .stream().filter(c -> Main.getLoadedJar().getClass(c).isPresent())
                        .map(c -> Main.getLoadedJar().getClass(c).get()).collect(Collectors.toSet());
                for (JarClassEntry jce : hierarchy) {
                    MethodSignature newSig
                            = new MethodSignature(newName, ((MethodSignature) sig).getDescriptor());
                    if (jce.getCurrentMethods().containsValue(newSig)) {
                        showDupeAlert(!jce.getName().equals(getName()));
                        return false;
                    }
                }
                return true;
            }
            default: {
                throw new AssertionError();
            }
        }
    }

    private void showDupeAlert(boolean hierarchical) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(Main.getResourceBundle().getString("rename.dupe.title"));
        alert.setHeaderText(null);
        alert.setContentText(
                Main.getResourceBundle().getString("rename.dupe.content" + (hierarchical ? ".hierarchy" : ""))
        );
        alert.showAndWait();
    }

    private void showIllegalAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(Main.getResourceBundle().getString("rename.illegal.title"));
        alert.setHeaderText(null);
        alert.setContentText(Main.getResourceBundle().getString("rename.illegal.content"));
        alert.showAndWait();
    }

    public void setMapping(String mapping) {
        switch (type) {
            case CLASS: {
                if (fullName.contains(INNER_CLASS_SEPARATOR_CHAR + "")) {
                    mapping = fullName.substring(0, fullName.lastIndexOf(INNER_CLASS_SEPARATOR_CHAR) + 1) + mapping;
                }
                MappingsHelper.genClassMapping(Main.getMappingContext(), getName(), mapping, true);
                fullName = mapping;
                break;
            }
            case FIELD: {
                MappingsHelper.genFieldMapping(Main.getMappingContext(), getParentClass(), getName(), mapping,
                        getDescriptor());
                break;
            }
            case METHOD: {
                IndexedClass clazz = IndexedClass.INDEXED_CLASSES.get(getParentClass());
                Set<IndexedClass> classes = Sets.newHashSet(clazz.getHierarchy());
                classes.add(clazz);

                for (IndexedClass ic : classes) {
                    //noinspection SuspiciousMethodCalls: sig must be a MethodSignature object
                    if (ic.getMethods().containsKey(sig)) {
                        MappingsHelper.genMethodMapping(Main.getMappingContext(), ic.getName(), getName(), mapping,
                                getDescriptor(), false);
                    }
                }
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
    }

    public void updateCodeTab() {
        CodeTab.SelectableMemberType sType = CodeTab.SelectableMemberType.fromMemberType(this.type);
        this.codeTab.setMemberType(sType);
        this.codeTab.setMemberIdentifier(this.getText());
        if (sType.isInfoEnabled()) {
            this.codeTab.setMemberInfo(this.getDescriptor());
        }
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public StringProperty getDescriptorProperty() {
        return descriptorProperty;
    }

    public StringProperty getParentClassProperty() {
        return parentClassProperty;
    }

    public MemberType getType() {
        return type;
    }

    public String getName() {
        return getNameProperty().get();
    }

    public String getDescriptor() {
        return getDescriptorProperty().get();
    }

    public String getParentClass() {
        return getParentClassProperty().get();
    }

    private void updateText() {
        String deobf;
        switch (this.getType()) {
            case CLASS:
                deobf = ClassMapping.deobfuscate(Main.getMappingContext(), getName());
                if (!isInnerClass()) {
                    fullName = deobf;
                }
                break;
            case FIELD:
            case METHOD:
                deobf = getName();

                Optional<ClassMapping> classMapping
                        = MappingsHelper.getClassMapping(Main.getMappingContext(), getParentClass());
                if (classMapping.isPresent()) {
                    Map<String, ? extends Mapping> mappings = getType() == MemberType.FIELD
                            ? classMapping.get().getFieldMappings()
                            : classMapping.get().getMethodMappings();
                    Mapping mapping = mappings.get(getName() + (getType() == MemberType.METHOD ? getDescriptor() : ""));
                    if (mapping != null) {
                        deobf = mapping.getDeobfuscatedName();
                    }
                }
                break;
            default:
                throw new AssertionError();
        }

        setAndProcessText(deobf);
    }

    public static SelectableMember fromMatcher(CodeTab codeTab, Matcher matcher) {
        MemberType type = matcher.group().startsWith(CLASS_PREFIX)
                ? MemberType.CLASS
                : MemberType.fromString(matcher.group(1));

        if (type == MemberType.CLASS) {
            return new SelectableMember(codeTab, type, matcher.group(1));
        } else {
            String qualName = matcher.group(2);
            String descriptor = matcher.group(3);
            int offset = qualName.lastIndexOf(CLASS_PATH_SEPARATOR_CHAR);
            String simpleName = qualName.substring(offset + 1);
            String parentClass = qualName.substring(0, offset);
            try {
                return new SelectableMember(codeTab, type, simpleName, descriptor, parentClass);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    public void setAndProcessText(String text) {
        setText(getType() == MemberType.CLASS ? StringHelper.unqualify(text) : text);
    }

    public static final class MemberKey {

        private final MemberType type;
        private final String qualName;
        private final String descriptor;

        public MemberKey(MemberType type, String qualifiedName, String descriptor) {
            this.type = type;
            this.qualName = qualifiedName;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MemberKey)) {
                return false;
            }
            MemberKey key = (MemberKey) obj;
            return     Objects.equals(type, key.type)
                    && Objects.equals(qualName, key.qualName)
                    && Objects.equals(descriptor, key.descriptor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, qualName, descriptor);
        }

    }

    public boolean isInnerClass() {
        return getType() == MemberType.CLASS && getName().contains(INNER_CLASS_SEPARATOR_CHAR + "");
    }

    public void setDeobfuscated(boolean deobfuscated) {
        getStyleClass().clear();
        if (deobfuscated) {
            getStyleClass().add("deobfuscated");
        } else {
            getStyleClass().add("obfuscated");
        }
    }

}
