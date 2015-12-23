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
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_PATTERN;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.control.CodeTab;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.mapping.io.reader.MappingsReader;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.util.MemberType;

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
                this.codeTab.setMemberType(CodeTab.SelectableMemberType.fromMemberType(this.type));
                this.codeTab.setMemberIdentifier(this.nameProperty.get());
                if (this.type != MemberType.CLASS) {
                    this.codeTab.setMemberInfo(this.descriptorProperty.get());
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> {
            TextInputDialog textInputDialog = new TextInputDialog(this.getText());
            textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));

            Optional<String> result = textInputDialog.showAndWait();
            if (result.isPresent() && !result.get().equals("")) {
                this.setText(result.get());
                switch (type) {
                    case CLASS: {
                        Matcher matcher = INNER_CLASS_SEPARATOR_PATTERN.matcher(getName());
                        if (matcher.matches()) {
                            String parent = getName().substring(0, matcher.end() - 1);
                            ClassMapping parentMapping
                                    = MappingsReader.getOrCreateClassMapping(Main.getMappings(), parent);
                            new InnerClassMapping(parentMapping, getName(), result.get());
                        } else {
                            Main.getMappings()
                                    .addMapping(new TopLevelClassMapping(Main.getMappings(), getName(), result.get()));
                        }
                        break;
                    }
                    case FIELD: {
                        ClassMapping parentMapping
                                = MappingsReader.getOrCreateClassMapping(Main.getMappings(), parentClass);
                        new FieldMapping(parentMapping, getName(), result.get(), Type.fromString(descriptor));
                        break;
                    }
                    case METHOD: {
                        ClassMapping parentMapping
                                = MappingsReader.getOrCreateClassMapping(Main.getMappings(), parentClass);
                        new MethodMapping(parentMapping, getName(), result.get(),
                                MethodDescriptor.fromString(descriptor));
                        break;
                    }
                }
            }
        });

        contextMenu.getItems().add(renameItem);

        this.setOnContextMenuRequested(event ->
                contextMenu.show(SelectableMember.this, event.getScreenX(), event.getScreenY()));

        updateText();
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
            String deobf = ClassMapping.deobfuscate(Main.getMappings(), getName());
            String[] arr = CLASS_PATH_SEPARATOR_PATTERN.split(deobf);
            deobf = arr[arr.length - 1];
            setText(deobf);
        } else if (getType() == MemberType.FIELD || getType() == MemberType.METHOD) {
            String deobf = getName();

            Optional<ClassMapping> classMapping = MappingsReader.getClassMapping(Main.getMappings(), getParentClass());
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
