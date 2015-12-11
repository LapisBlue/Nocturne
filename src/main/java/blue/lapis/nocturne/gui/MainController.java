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
package blue.lapis.nocturne.gui;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.control.CodeTab;
import blue.lapis.nocturne.gui.io.jar.JarDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsOpenDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsSaveDialogHelper;
import blue.lapis.nocturne.jar.model.hierarchy.Hierarchy;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyElement;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyNode;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.helper.PropertiesHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The main JavaFX controller.
 */
public class MainController implements Initializable {

    public MenuItem openJarButton;
    public MenuItem closeJarButton;
    public MenuItem loadMappingsButton;
    public MenuItem saveMappingsButton;
    public MenuItem saveMappingsAsButton;
    public MenuItem closeButton;

    public MenuItem clearMappingsButton;

    public ToggleGroup languageGroup;

    public MenuItem aboutButton;

    public TabPane tabs;

    public TreeView<String> obfTree;
    public TreeView<String> deobfTree;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeJarButton.setDisable(Main.getLoadedJar() == null)  ;

        final String langRadioPrefix = "langRadio-";
        languageGroup.getToggles().stream()
                .filter(toggle -> ((RadioMenuItem) toggle).getId().equals(langRadioPrefix + Main.getCurrentLocale()))
                .forEach(toggle -> toggle.setSelected(true));

        setAccelerators();
        this.initSampleCodeTabs();
    }

    private void initSampleCodeTabs() {
        // The following is example code, for adding code-tabs
        CodeTab fieldExample = new CodeTab();
        fieldExample.setText("cG");
        fieldExample.setMemberType(CodeTab.SelectableMemberType.FIELD);
        fieldExample.setMemberIdentifier("logger");
        fieldExample.setMemberInfo("java.util.Logger");

        CodeTab methodExample = new CodeTab();
        methodExample.setText("aQ");
        methodExample.setMemberType(CodeTab.SelectableMemberType.METHOD);
        methodExample.setMemberIdentifier("doSomething");
        methodExample.setMemberInfo("(Ljava/lang/String)V");

        CodeTab classExample = new CodeTab();
        classExample.setText("jH");
        classExample.setMemberType(CodeTab.SelectableMemberType.CLASS);
        classExample.setMemberIdentifier("jH");

        this.tabs.getTabs().addAll(fieldExample, methodExample, classExample);
    }

    private void setAccelerators() {
        openJarButton.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        loadMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        saveMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveMappingsAsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN,
                KeyCombination.ALT_DOWN));
        aboutButton.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    }

    public void openJar(ActionEvent actionEvent) throws IOException {
        if (Main.getLoadedJar() != null) {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
            Main.getMappings().clear();
            Main.setLoadedJar(null);
        }

        JarDialogHelper.openJar(this);
        updateClassViews();
    }

    public void closeJar(ActionEvent actionEvent) throws IOException {
        if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
            return;
        }

        Main.getMappings().clear();
        Main.setLoadedJar(null);
        closeJarButton.setDisable(true);
        updateClassViews();
    }

    public void loadMappings(ActionEvent actionEvent) throws IOException {
        MappingsOpenDialogHelper.openMappings();
        updateClassViews();
    }

    public void clearMappings(ActionEvent actionEvent) {
        try {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Main.getMappings().clear();
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
        alert.getDialogPane().getStylesheets().add("css/nocturne.css");

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fx/about.fxml"));
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

            Main.reload();
        }

    }

    public void updateObfuscatedClassListView() {
        if (Main.getLoadedJar() != null) {
            TreeItem<String> root = generateTreeItem(Main.getLoadedJar().getObfuscatedHierarchy());
            root.setExpanded(true);
            obfTree.setRoot(root);
        } else {
            obfTree.setRoot(null);
        }

    }

    public void updateDeobfuscatedClassListView() {
        if (Main.getLoadedJar() != null) {
            TreeItem<String> root = generateTreeItem(Main.getLoadedJar().getDeobfuscatedHierarchy());
            root.setExpanded(true);
            deobfTree.setRoot(root);
        } else {
            deobfTree.setRoot(null);
        }
    }

    public TreeItem<String> generateTreeItem(HierarchyElement element) {
        String name = "(root)";
        if (element instanceof HierarchyNode) {
            name = ((HierarchyNode) element).getName();
        }
        TreeItem<String> treeItem = new TreeItem<>(name);
        if (element instanceof Hierarchy
                || (element instanceof HierarchyNode && !((HierarchyNode) element).isTerminal())) {
            treeItem.getChildren().addAll(
                    element.getChildren().stream().map(this::generateTreeItem).collect(Collectors.toList())
            );
        }
        treeItem.getChildren().setAll(treeItem.getChildren().sorted());
        return treeItem;
    }

    public void updateClassViews() {
        updateObfuscatedClassListView();
        updateDeobfuscatedClassListView();
    }

}
