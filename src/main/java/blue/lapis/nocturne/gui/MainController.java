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

package blue.lapis.nocturne.gui;

import static blue.lapis.nocturne.util.helper.Preconditions.checkArgument;
import static blue.lapis.nocturne.util.helper.StringHelper.looksDeobfuscated;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.io.jar.JarDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsOpenDialogHelper;
import blue.lapis.nocturne.gui.io.mappings.MappingsSaveDialogHelper;
import blue.lapis.nocturne.gui.scene.control.ClassesTreeView;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.helper.CacheHelper;
import blue.lapis.nocturne.util.helper.SceneHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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
    public MenuItem preferencesButton;

    public ToggleGroup languageGroup;

    public MenuItem aboutButton;

    public TabPane tabs;

    public ClassesTreeView obfTree;
    public ClassesTreeView deobfTree;

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

        RESTART_ALERT.setTitle(Main.getResourceBundle().getString("dialog.restart.title"));
        RESTART_ALERT.setHeaderText(null);
        RESTART_ALERT.setContentText(Main.getResourceBundle().getString("dialog.restart.content"));
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
                    member.setDeobfuscated(looksDeobfuscated(member.getName()), false);
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

    public void showPreferences(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fxml/preferences.fxml"));
        loader.setResources(Main.getResourceBundle());
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(Main.getResourceBundle().getString("preferences.title"));
        stage.setScene(new Scene(root));
        stage.showAndWait();
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
            Main.getCacheHelper().setProperty(CacheHelper.CacheKey.LOCALE, langId);

            RESTART_ALERT.showAndWait();
        }

    }

    public void updateClassViews() {
        this.obfTree.update(Main.getLoadedJar().getObfuscatedHierarchy());
        this.deobfTree.update(Main.getLoadedJar().getDeobfuscatedHierarchy());
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
    public void closeAllTabs() {
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
