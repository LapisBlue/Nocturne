/*
 * Nocturne
 * Copyright (c) 2015, Lapis <https://github.com/LapisBlue>
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
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MemberMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

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
        this.parentClassProperty.set(parentClass);

        if (type == MemberType.CLASS) {
            fullName = getName();
        }

        this.setFill(Color.web("orange"));

        this.setOnMouseClicked(event1 -> {
            if (event1.getButton() == MouseButton.PRIMARY) {
                this.updateCodeTab();
            }
        });

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> {
            String dispText = this.getText();
            if (!isInnerClass()) {
                dispText = fullName;
            }
            TextInputDialog textInputDialog = new TextInputDialog(dispText);
            textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));

            Optional<String> result = textInputDialog.showAndWait();
            if (result.isPresent() && !result.get().equals("")) {
                if (isInnerClass() || checkClassDupe(result.get())) {
                    this.setMapping(result.get());
                }
            }
        });

        MenuItem resetItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.reset"));
        resetItem.setOnAction(event -> {
            switch (getType()) {
                case CLASS: {
                    Optional<ClassMapping> mapping
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getName());
                    if (mapping.isPresent()
                            && !mapping.get().getObfuscatedName().equals(mapping.get().getDeobfuscatedName())) {
                        if (!isInnerClass() && !checkClassDupe(mapping.get().getObfuscatedName())) {
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

        String qualifiedName = type == MemberType.CLASS ? name : parentClass + CLASS_PATH_SEPARATOR_CHAR + name;
        //TODO: we're ignoring field descriptors for now since SRG doesn't support them
        MemberKey key = new MemberKey(type, qualifiedName, type == MemberType.METHOD ? descriptor : null);
        if (!MEMBERS.containsKey(key)) {
            MEMBERS.put(key, new ArrayList<>());
        }
        MEMBERS.get(key).add(this);

        updateText();
    }

    private boolean checkClassDupe(String newName) {
        if (Main.getLoadedJar().getCurrentNames().containsValue(newName)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(Main.getResourceBundle().getString("rename.dupe.title"));
            alert.setContentText(Main.getResourceBundle().getString("rename.dupe.content"));
            alert.showAndWait();
            return false;
        }
        return true;
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
                MappingsHelper.genFieldMapping(Main.getMappingContext(), getParentClass(), getName(), mapping);
                break;
            }
            case METHOD: {
                MappingsHelper.genMethodMapping(Main.getMappingContext(), getParentClass(), getName(), mapping,
                        getDescriptor());
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
        if (getType() == MemberType.CLASS) {
            deobf = ClassMapping.deobfuscate(Main.getMappingContext(), getName());
        } else if (getType() == MemberType.FIELD || getType() == MemberType.METHOD) {
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
        } else {
            throw new AssertionError();
        }

        setAndProcessText(deobf);
    }

    public static SelectableMember fromMatcher(CodeTab codeTab, Matcher matcher) {
        MemberType type = MemberType.fromString(matcher.group(1));
        String qualName = matcher.group(2);
        String descriptor = matcher.groupCount() > 2 ? matcher.group(3) : null;

        if (type == MemberType.CLASS) {
            return new SelectableMember(codeTab, type, qualName);
        } else {
            int offset = qualName.lastIndexOf(CLASS_PATH_SEPARATOR_CHAR);
            String simpleName = qualName.substring(offset + 1);
            String parentClass = qualName.substring(0, offset);
            return new SelectableMember(codeTab, type, simpleName, descriptor, parentClass);
        }
    }

    public void setAndProcessText(String text) {
        setText(getType() == MemberType.CLASS ? MappingsHelper.unqualify(text) : text);
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

}
