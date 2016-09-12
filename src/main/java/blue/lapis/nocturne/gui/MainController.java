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

package blue.lapis.nocturne.gui;

import static com.google.common.base.Preconditions.checkArgument;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.io.jar.JarDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsOpenDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsSaveDialogHelper;
import blue.lapis.nocturne.gui.scene.control.ClassTreeItem;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.jar.model.hierarchy.Hierarchy;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyElement;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyNode;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.helper.PropertiesHelper;
import blue.lapis.nocturne.util.helper.SceneHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * The main JavaFX controller.
 */
public class MainController implements Initializable {

    public static MainController INSTANCE;

    private static final Alert RESTART_ALERT = new Alert(Alert.AlertType.WARNING);

    public MenuItem openJarButton;
    public MenuItem closeJarButton;
    public MenuItem loadMappingsButton;
    public MenuItem mergeMappingsButton;
    public MenuItem saveMappingsButton;
    public MenuItem saveMappingsAsButton;
    public MenuItem closeButton;

    public MenuItem resetMappingsButton;

    public ToggleGroup languageGroup;

    public MenuItem aboutButton;

    public TabPane tabs;

    public TreeView<String> obfTree;
    public TreeView<String> deobfTree;

    public MainController() {
        INSTANCE = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeJarButton.setDisable(Main.getLoadedJar() == null);
        loadMappingsButton.setDisable(Main.getLoadedJar() == null);
        mergeMappingsButton.setDisable(Main.getLoadedJar() == null);
        saveMappingsButton.setDisable(Main.getLoadedJar() == null);
        saveMappingsAsButton.setDisable(Main.getLoadedJar() == null);
        resetMappingsButton.setDisable(Main.getLoadedJar() == null);

        final String langRadioPrefix = "langRadio-";
        for (Toggle toggle : languageGroup.getToggles()) {
            if (((RadioMenuItem) toggle).getId().equals(langRadioPrefix + Main.getCurrentLocale())) {
                toggle.setSelected(true);
                break;
            }
        }

        setAccelerators();

        this.initTreeViews();

        RESTART_ALERT.setTitle(Main.getResourceBundle().getString("dialog.restart.title"));
        RESTART_ALERT.setHeaderText(null);
        RESTART_ALERT.setContentText(Main.getResourceBundle().getString("dialog.restart.content"));
    }

    private void initTreeViews() {
        BiConsumer<InputEvent, TreeView<String>> clickHandler = (event, treeView) -> {
            if ((event instanceof MouseEvent && ((MouseEvent) event).getClickCount() == 2)
                    || (event instanceof KeyEvent && ((KeyEvent) event).getCode() == KeyCode.ENTER)) {
                TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                if (selected instanceof ClassTreeItem) {
                    String className = ((ClassTreeItem) selected).getId();
                    if (Main.getLoadedJar() != null) {
                        openTab(className, selected.getValue());
                    }
                } else {
                    if (event instanceof MouseEvent == selected.isExpanded()) {
                        selected.setExpanded(true);
                        while (selected.getChildren().size() == 1) {
                            selected = selected.getChildren().get(0);
                            selected.setExpanded(true);
                        }
                    } else {
                        selected.setExpanded(false);
                    }
                }
            }
        };

        obfTree.setOnMouseClicked(event -> clickHandler.accept(event, obfTree));
        deobfTree.setOnMouseClicked(event -> clickHandler.accept(event, deobfTree));

        obfTree.setOnKeyReleased(event -> clickHandler.accept(event, obfTree));
        deobfTree.setOnKeyReleased(event -> clickHandler.accept(event, deobfTree));
    }

    private void setAccelerators() {
        openJarButton.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        loadMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        mergeMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN,
                KeyCombination.ALT_DOWN));
        saveMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveMappingsAsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN,
                KeyCombination.ALT_DOWN));
        aboutButton.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    }

    public void openJar(ActionEvent actionEvent) throws IOException {
        if (Main.getLoadedJar() != null && !deinitializeCurrentJar()) {
            return;
        }
        JarDialogHelper.openJar(this);
        updateClassViews();

    }

    public void closeJar(ActionEvent actionEvent) throws IOException {
        if (!deinitializeCurrentJar()) {
            return;
        }

        closeJarButton.setDisable(true);
        loadMappingsButton.setDisable(true);
        mergeMappingsButton.setDisable(true);
        saveMappingsButton.setDisable(true);
        saveMappingsAsButton.setDisable(true);
        resetMappingsButton.setDisable(true);

        Main.getMappingContext().clear();
        Main.getMappingContext().setDirty(false);

        updateClassViews();
    }

    public void loadMappings(ActionEvent actionEvent) throws IOException {
        try {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        MappingsOpenDialogHelper.openMappings(false);
        updateClassViews();
    }

    public void mergeMappings(ActionEvent actionEvent) throws IOException {
        MappingsOpenDialogHelper.openMappings(true);
        updateClassViews();
    }

    public void resetMappings(ActionEvent actionEvent) {
        try {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Main.getMappingContext().getMappings().values().forEach(cm -> {
            Main.getLoadedJar().getCurrentNames().put(cm.getObfuscatedName(), cm.getObfuscatedName());
            JarClassEntry jce = Main.getLoadedJar().getClass(cm.getObfuscatedName()).orElse(null);
            if (jce == null) {
                return;
            }
            cm.getInnerClassMappings().values()
                    .forEach(im -> jce.getCurrentInnerClassNames().put(im.getObfuscatedName(), im.getObfuscatedName()));
            cm.getFieldMappings().values()
                    .forEach(fm -> jce.getCurrentFields().put(fm.getSignature(), fm.getSignature()));
            cm.getMethodMappings().values()
                    .forEach(mm -> jce.getCurrentMethods().put(mm.getSignature(), mm.getSignature()));
        });
        Main.getMappingContext().clear();
        Main.getLoadedJar().getClasses().forEach(jce -> jce.setDeobfuscated(false));
        CodeTab.CODE_TABS.values().forEach(CodeTab::resetClassName);
        SelectableMember.MEMBERS.values()
                .forEach(list -> list.forEach(member -> {
                    member.setAndProcessText(member.getName());
                    member.setDeobfuscated(false);
                }));
        updateClassViews();
    }

    public void saveMappings(ActionEvent actionEvent) throws IOException {
        MappingsSaveDialogHelper.saveMappings();
    }

    public void saveMappingsAs(ActionEvent actionEvent) throws IOException {
        MappingsSaveDialogHelper.saveMappingsAs();
    }

    public void onClose(ActionEvent actionEvent) {
        try {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(0);
    }

    public void showAbout(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Main.getResourceBundle().getString("about.title"));
        alert.setHeaderText("Nocturne v" + Constants.VERSION);

        alert.getDialogPane().getStyleClass().add("about");
        SceneHelper.addStdStylesheet(alert.getDialogPane());

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fxml/about.fxml"));
        loader.setResources(Main.getResourceBundle());
        Node content = loader.load();
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    public void onLanguageSelect(ActionEvent actionEvent) throws IOException {
        RadioMenuItem radioItem = (RadioMenuItem) actionEvent.getSource();
        final String langPrefix = "langRadio-";
        String langId = radioItem.getId().substring(langPrefix.length());

        if (!langId.equals(Main.getCurrentLocale())) {
            Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LOCALE, langId);

            RESTART_ALERT.showAndWait();
        }

    }

    public void updateObfuscatedClassListView() {
        if (Main.getLoadedJar() != null) {
            TreeItem<String> root = generateTreeItem(Main.getLoadedJar().getObfuscatedHierarchy(), obfTree.getRoot());
            root.setExpanded(true);
            obfTree.setRoot(root);
        } else {
            obfTree.setRoot(null);
        }

    }

    public void updateDeobfuscatedClassListView() {
        if (Main.getLoadedJar() != null) {
            TreeItem<String> root
                    = generateTreeItem(Main.getLoadedJar().getDeobfuscatedHierarchy(), deobfTree.getRoot());
            root.setExpanded(true);
            deobfTree.setRoot(root);
        } else {
            deobfTree.setRoot(null);
        }
    }

    public TreeItem<String> generateTreeItem(HierarchyElement element, TreeItem<String> oldTreeItem) {
        TreeItem<String> treeItem;
        if (element instanceof HierarchyNode) {
            HierarchyNode node = (HierarchyNode) element;
            if (node.isTerminal()) {
                treeItem = new ClassTreeItem(node.getId(), node.getDisplayName());
            } else {
                treeItem = new TreeItem<>(node.getDisplayName());
            }
        } else {
            treeItem = new TreeItem<>("(root)");
        }
        if (oldTreeItem != null) {
            treeItem.setExpanded(oldTreeItem.isExpanded());
        }
        if (element instanceof Hierarchy
                || (element instanceof HierarchyNode && !((HierarchyNode) element).isTerminal())) {
            for (HierarchyNode node : element.getChildren()) {
                if (oldTreeItem != null) {
                    boolean added = false;
                    for (TreeItem<String> child : oldTreeItem.getChildren()) {
                        if (node.getDisplayName().equalsIgnoreCase(child.getValue())) {
                            treeItem.getChildren().add(this.generateTreeItem(node, child));
                            added = true;
                        }
                    }
                    if (!added) {
                        treeItem.getChildren().add(this.generateTreeItem(node, null));
                    }
                } else {
                    treeItem.getChildren().add(this.generateTreeItem(node, null));
                }
            }
        }
        treeItem.getChildren().setAll(treeItem.getChildren().sorted((t1, t2) -> {
            boolean c1 = t1.getChildren().size() > 0;
            boolean c2 = t2.getChildren().size() > 0;
            if (c1 == c2) { // both either terminal or non-terminal
                return t1.getValue().compareTo(t2.getValue());
            } else if (c1) { // first is non-terminal, second is terminal
                return -1;
            } else { // first is terminal, second is non-terminal
                return 1;
            }
        }));
        return treeItem;
    }

    public void updateClassViews() {
        updateObfuscatedClassListView();
        updateDeobfuscatedClassListView();
    }

    private boolean deinitializeCurrentJar() throws IOException {
        if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
            return false;
        }
        Main.getMappingContext().clear();
        closeAllTabs();
        Main.setLoadedJar(null);
        return true;
    }

    /**
     * Closes all currently opened tabs.
     */
    private void closeAllTabs() {
        tabs.getTabs().forEach(tab -> tab.getOnClosed().handle(null));
        tabs.getTabs().clear();
        CodeTab.CODE_TABS.clear();
    }

    public void openTab(String className, String displayName) {
        if (CodeTab.CODE_TABS.containsKey(className)) {
            tabs.getSelectionModel().select(CodeTab.CODE_TABS.get(className));
        } else {
            CodeTab tab = new CodeTab(tabs, className, displayName);

            Optional<JarClassEntry> clazz = Main.getLoadedJar().getClass(className);
            checkArgument(clazz.isPresent(), "Cannot find class entry for " + className);
            tab.setCode(clazz.get().decompile());
        }
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

}
