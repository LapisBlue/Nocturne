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

package blue.lapis.nocturne.gui.scene.text;

import static blue.lapis.nocturne.util.Constants.CLASS_MEMBER_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.DOT_PATTERN;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_PREFIX;
import static blue.lapis.nocturne.util.Constants.Processing.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.helper.MappingsHelper.doesRemappedNameClash;
import static blue.lapis.nocturne.util.helper.MappingsHelper.genMethodMapping;
import static blue.lapis.nocturne.util.helper.StringHelper.looksDeobfuscated;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MemberMapping;
import blue.lapis.nocturne.mapping.model.MethodParameterMapping;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.MappingsHelper;
import blue.lapis.nocturne.util.helper.ReferenceHelper;
import blue.lapis.nocturne.util.helper.StringHelper;
import blue.lapis.nocturne.util.tuple.Pair;

import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.FieldReference;
import org.cadixdev.bombe.type.reference.MethodParameterReference;
import org.cadixdev.bombe.type.reference.MethodReference;
import org.cadixdev.bombe.type.reference.QualifiedReference;
import org.cadixdev.bombe.type.reference.TopLevelClassReference;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

/**
 * Represents a selectable member in code.
 */
public class SelectableMember extends Text {

    public static final Map<QualifiedReference, List<SelectableMember>> MEMBERS = new HashMap<>();

    private static void updateView(QualifiedReference ref, @Nullable String paramName) {
        String deobf = MappingsHelper.getDeobfuscatedName(Main.getMappingContext(), ref)
                .orElse(ReferenceHelper.getName(ref, paramName));
        if (ref instanceof ClassReference) {
            deobf = StringHelper.unqualify(deobf);
        }
        String deobfF = deobf;

        MEMBERS.get(ref).forEach(sm -> sm.setText(deobfF));
    }

    public static SelectableMember fromMatcher(CodeTab codeTab, Matcher matcher) {
        QualifiedReference.Type type;

        //TODO: handle params
        if (matcher.group().startsWith(CLASS_PREFIX)) {
            String qualClassName = matcher.group(1);
            return new SelectableMember(codeTab, ReferenceHelper.createClassReference(qualClassName));
        } else if (matcher.group().startsWith(MEMBER_PREFIX)) {
            type = QualifiedReference.Type.valueOf(matcher.group(1));

            String qualName = matcher.group(2);
            String descriptor = matcher.group(3);

            int offset = qualName.lastIndexOf(CLASS_PATH_SEPARATOR_CHAR);

            String simpleName = qualName.substring(offset + 1);
            String parentClassName = qualName.substring(0, offset);

            ClassReference classRef = ReferenceHelper.createClassReference(parentClassName);

            if (type == QualifiedReference.Type.FIELD) {
                FieldSignature fieldSig = new FieldSignature(simpleName, FieldType.of(descriptor));
                return new SelectableMember(codeTab, new FieldReference(classRef, fieldSig));
            } else if (type == QualifiedReference.Type.METHOD) {
                MethodSignature methodSig = new MethodSignature(simpleName, MethodDescriptor.of(descriptor));
                return new SelectableMember(codeTab, new MethodReference(classRef, methodSig));
            } else {
                throw new AssertionError("Unhandled case " + type.name());
            }
        } else {
            throw new AssertionError("Don't know how to handle transformed identifier " + matcher.group());
        }
    }

    private final CodeTab codeTab;

    private final QualifiedReference reference;
    private final String origName;

    private boolean deobfuscated;

    public SelectableMember(CodeTab codeTab, QualifiedReference reference, @Nullable String name) {
        super(ReferenceHelper.getName(reference, name));
        this.codeTab = codeTab;
        this.reference = reference;

        this.origName = super.getText();

        this.setOnMouseClicked(event1 -> {
            if (event1.getButton() == MouseButton.PRIMARY) {
                this.updateCodeTab();
            }
        });

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> handleRenameAction());

        MenuItem resetItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.reset"));
        resetItem.setOnAction(event -> handleResetAction(name));

        MenuItem toggleDeobf = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.toggleDeobf"));
        toggleDeobf.setOnAction(event -> handleToggleDeobfAction());

        MenuItem jumpToDefItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.jumpToDef"));
        jumpToDefItem.setOnAction(event -> {
            handleJumpToDefAction();
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(renameItem);
        contextMenu.getItems().add(resetItem);
        contextMenu.getItems().add(toggleDeobf);
        contextMenu.getItems().add(jumpToDefItem);

        this.setOnContextMenuRequested(event -> showContextMenu(toggleDeobf, contextMenu, event));

        MEMBERS.computeIfAbsent(reference, ref -> new ArrayList<>()).add(this);

        //TODO: figure out how to do this
        /*setDeobfuscated(looksDeobfuscated(getName())
                || (fullName != null && !getName().equals(fullName))
                || (mapping.isPresent() && mapping.get().isAdHoc()), false);*/
    }

    public SelectableMember(CodeTab codeTab, QualifiedReference reference) {
        this(codeTab, reference, null);
    }

    public QualifiedReference getReference() {
        return reference;
    }

    public void resetName() {
        setText(origName);
        //TODO: check if deobfuscated
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

    private void setMappedName(String newName) {
        switch (reference.getType()) {
            case TOP_LEVEL_CLASS:
            case INNER_CLASS: {
                MappingsHelper.genClassMapping(Main.getMappingContext(), (TopLevelClassReference) reference, newName);
                break;
            }
            case FIELD: {
                MappingsHelper.genFieldMapping(Main.getMappingContext(), (FieldReference) reference, newName);
                break;
            }
            case METHOD: {
                MethodReference methodRef = (MethodReference) reference;

                IndexedClass owningClass
                        = IndexedClass.INDEXED_CLASSES.get((methodRef).getOwningClass());

                Set<IndexedClass> classes = new HashSet<>(owningClass.getHierarchy());
                classes.add(owningClass);

                for (IndexedClass ic : classes) {
                    //noinspection SuspiciousMethodCalls: sig must be a MethodSignature object
                    if (ic.getMethods().containsKey(methodRef.getSignature())) {
                        genMethodMapping(Main.getMappingContext(),
                                new MethodReference(ic.getReference(), methodRef.getSignature()), newName, false);
                    }
                }
                break;
            }
            default: {
                throw new AssertionError();
            }
        }

        updateView(reference, origName);
    }

    public void updateCodeTab() {
        CodeTab.SelectableMemberType sType = CodeTab.SelectableMemberType.fromReferenceType(reference.getType());
        this.codeTab.setMemberType(sType);
        this.codeTab.setMemberIdentifier(this.getText());
        if (sType.isInfoEnabled()) {
            switch (reference.getType()) {
                case FIELD:
                    this.codeTab.setMemberInfo(
                            Objects.toString(((FieldReference) reference).getSignature().getType().orElse(null))
                    );
                    break;
                case METHOD:
                    this.codeTab.setMemberInfo(((MethodReference) reference).getSignature().getDescriptor().toString());
                    break;
                case METHOD_PARAMETER:
                    MethodParameterReference paramRef = (MethodParameterReference) reference;
                    this.codeTab.setMemberInfo(paramRef.getParentMethod().getSignature().getDescriptor()
                            .getParamTypes().get(paramRef.getParameterIndex()).toString());
            }
        }
    }

    public void setDeobfuscated(boolean deobfuscated, boolean soft) {
        if (this.deobfuscated && !deobfuscated && soft) {
            return;
        }

        this.deobfuscated = deobfuscated;
        getStyleClass().clear();
        if (deobfuscated) {
            getStyleClass().add("deobfuscated");
        } else {
            getStyleClass().add("obfuscated");
        }
    }

    private void handleRenameAction() {
        String dispText;
        if (reference.getType() == QualifiedReference.Type.TOP_LEVEL_CLASS) {
            dispText = ((TopLevelClassReference) reference).getClassType().getClassName();
        } else {
            dispText = this.getText();
        }
        TextInputDialog textInputDialog = new TextInputDialog(dispText);
        textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));

        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent() && !result.get().equals("") && !result.get().equals(getText())) {
            Pair<Boolean, Boolean> dupe = doesRemappedNameClash(Main.getLoadedJar(), reference, result.get());
            if (dupe.first()) {
                showDupeAlert(dupe.second());
            }

            String res = result.get();
            if (reference instanceof ClassReference) {
                if (!StringHelper.isJavaClassIdentifier(res)) {
                    showIllegalAlert();
                }

                res = DOT_PATTERN.matcher(res).replaceAll(CLASS_PATH_SEPARATOR_CHAR + "");
            } else if (!StringHelper.isJavaIdentifier(res)) {
                showIllegalAlert();
                return;
            }
            this.setMappedName(res);
        }
    }

    private void handleResetAction(String origName) {
        //TODO: rewrite this completely
    }

    private void handleToggleDeobfAction() {
        // I know this is gross but it's a hell of a lot easier than fixing the problem the "proper" way
        boolean shouldDeobf = !this.deobfuscated;
        // set as ad hoc if we need to mark it as deobfuscated
        MappingsHelper.getMapping(Main.getMappingContext(), reference, true).get().setAdHoc(!this.deobfuscated);
        MEMBERS.get(reference).forEach(sm -> sm.setDeobfuscated(shouldDeobf, false));
    }

    private void handleJumpToDefAction() {
        TopLevelClassReference classRef = ReferenceHelper.getRootClass(reference);
        Optional<ClassMapping<?>> cm = MappingsHelper.getClassMapping(Main.getMappingContext(), classRef, false);
        MainController.INSTANCE.openTab(classRef,
                cm.isPresent() ? cm.get().getDeobfuscatedName() : classRef.toJvmsIdentifier());
    }

    private void showContextMenu(MenuItem toggleDeobf, ContextMenu contextMenu, ContextMenuEvent event) {
        Optional<? extends Mapping> mapping = MappingsHelper.getMapping(Main.getMappingContext(), reference, false);
        toggleDeobf.setDisable(mapping.isPresent()
                && !reference.toJvmsIdentifier().equals(mapping.get().getDeobfuscatedName()));

        contextMenu.show(SelectableMember.this, event.getScreenX(), event.getScreenY());
    }

}
