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
package blue.lapis.nocturne.gui.text;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_PATTERN;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.control.CodeTab;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Represents a selectable member in code.
 */
public class SelectableMember extends Text {

    private final CodeTab codeTab;
    private final MemberType type;

    private final StringProperty nameProperty = new SimpleStringProperty(this, "name");
    private final StringProperty descriptorProperty = new SimpleStringProperty(this, "descriptor");
    private final StringProperty parentClassProperty = new SimpleStringProperty(this, "parentClass");

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

        this.setFill(Color.web("orange"));

        this.setOnMouseClicked(event1 -> {
            if (event1.getButton() == MouseButton.PRIMARY) {
                this.updateCodeTab();
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> {
            TextInputDialog textInputDialog = new TextInputDialog(this.getText());
            textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));

            Optional<String> result = textInputDialog.showAndWait();
            if (result.isPresent() && !result.get().equals("")) {
                this.setMapping(result.get());
            }
        });

        MenuItem resetItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.reset"));
        resetItem.setOnAction(event -> {
            this.setMapping(this.getName());
            switch (getType()) {
                case CLASS: {
                    Optional<ClassMapping> mapping
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getName());
                    if (mapping.isPresent()) {
                        mapping.get().setDeobfuscatedName(mapping.get().getObfuscatedName());
                    }
                    break;
                }
                case FIELD: {
                    ClassMapping parent
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getParentClass()).get();
                    parent.removeFieldMapping(getName());
                    break;
                }
                case METHOD: {
                    ClassMapping parent
                            = MappingsHelper.getClassMapping(Main.getMappingContext(), getParentClass()).get();
                    parent.removeMethodMapping(getName());
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }
        });

        contextMenu.getItems().add(renameItem);
        contextMenu.getItems().add(resetItem);

        this.setOnContextMenuRequested(event ->
                contextMenu.show(SelectableMember.this, event.getScreenX(), event.getScreenY()));

        updateText();
    }

    public void setMapping(String mapping) {
        this.setText(mapping);
        switch (type) {
            case CLASS: {
                MappingsHelper.genClassMapping(Main.getMappingContext(),
                        getName(), mapping);
                break;
            }
            case FIELD: {
                MappingsHelper.genFieldMapping(Main.getMappingContext(),
                        getParentClass() + Constants.CLASS_PATH_SEPARATOR_CHAR + getName(), mapping);
                break;
            }
            case METHOD: {
                MappingsHelper.genMethodMapping(Main.getMappingContext(),
                        getParentClass() + Constants.CLASS_PATH_SEPARATOR_CHAR + getName(), getDescriptor(),
                        mapping,
                        MethodDescriptor.fromString(getDescriptor()).deobfuscate(Main.getMappingContext()).toString());
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        this.updateCodeTab();
    }

    public void updateCodeTab() {
        this.codeTab.setMemberType(CodeTab.SelectableMemberType.fromMemberType(this.type));
        this.codeTab.setMemberIdentifier(this.getText());
        if (this.type != MemberType.CLASS) {
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
        if (getType() == MemberType.CLASS) {
            String deobf = ClassMapping.deobfuscate(Main.getMappingContext(), getName());
            String[] arr = CLASS_PATH_SEPARATOR_PATTERN.split(deobf);
            deobf = arr[arr.length - 1];
            setText(deobf);
        } else if (getType() == MemberType.FIELD || getType() == MemberType.METHOD) {
            String deobf = getName();

            Optional<ClassMapping> classMapping
                    = MappingsHelper.getClassMapping(Main.getMappingContext(), getParentClass());
            if (classMapping.isPresent()) {
                Map<String, ? extends Mapping> mappings = getType() == MemberType.FIELD
                        ? classMapping.get().getFieldMappings()
                        : classMapping.get().getMethodMappings();
                Mapping mapping = mappings.get(getName());
                if (mapping != null) {
                    deobf = mapping.getDeobfuscatedName();
                }
            }

            setText(deobf);
        } else {
            throw new AssertionError();
        }
    }

    public static SelectableMember fromMatcher(CodeTab codeTab, Matcher matcher) {
        MemberType type = MemberType.fromString(matcher.group(1));
        String qualName = matcher.group(2);
        String descriptor = matcher.groupCount() > 2 ? matcher.group(3) : null;
        if (type != MemberType.CLASS) {
            String[] arr = CLASS_PATH_SEPARATOR_PATTERN.split(qualName);
            String parentClass = "";
            for (int i = 0; i < arr.length - 1; i++) {
                parentClass += arr[i];
            }
            String simpleName = arr[arr.length - 1];
            return new SelectableMember(codeTab, type, simpleName, descriptor, parentClass);
        } else {
            return new SelectableMember(codeTab, type, qualName);
        }
    }

}
