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
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openJarButton.setDisable(true); //TODO: temporary
        closeJarButton.setDisable(true);

        final String langRadioPrefix = "langRadio-";
        languageGroup.getToggles().stream()
                .filter(toggle -> ((RadioMenuItem) toggle).getId().equals(langRadioPrefix + Main.locale))
                .forEach(toggle -> toggle.setSelected(true));

        setAccelerators();
    }

    private void initSampleCodeTabs() {
        // The following is example code, for adding code-tabs
        CodeTab fieldExample = new CodeTab();
        fieldExample.setText("cG");
        fieldExample.setMemberType(CodeTab.MemberType.FIELD);
        fieldExample.setMemberIdentifier("logger");
        fieldExample.setMemberInfo("java.util.Logger");

        CodeTab methodExample = new CodeTab();
        methodExample.setText("aQ");
        methodExample.setMemberType(CodeTab.MemberType.METHOD);
        methodExample.setMemberIdentifier("doSomething");
        methodExample.setMemberInfo("(Ljava/lang/String)V");

        this.tabs.getTabs().addAll(fieldExample, methodExample);
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
        JarDialogHelper.openJar();
        closeJarButton.setDisable(false);
    }

    public void closeJar(ActionEvent actionEvent) {
        //TODO
        closeJarButton.setDisable(true);
    }

    public void loadMappings(ActionEvent actionEvent) throws IOException {
        MappingsOpenDialogHelper.openMappings();
    }

    public void clearMappings(ActionEvent actionEvent) {
        try {
            if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Main.mappings = new MappingContext();
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

    public void showAbout(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Main.resourceBundle.getString("about.title"));
        alert.setHeaderText("Nocturne " + Constants.VERSION);

        alert.setContentText(
                Main.resourceBundle.getString("about.copyright") + " (c) 2015 Lapis.\n"
                + Main.resourceBundle.getString("about.license") + "\n\n"
                + "Github: https://github.com/LapisBlue/Nocturne"
        );
        alert.showAndWait();
    }

    public void onLanguageSelect(ActionEvent actionEvent) throws IOException {
        //TODO: this is going to be a huge pain in the ass further on in development,
        // so we should probably store it somewhere (e.g. the appdata folder) and request a restart
        // also, memory leaks
        RadioMenuItem radioItem = (RadioMenuItem) actionEvent.getSource();
        if (!radioItem.isSelected()) {
            final String langPrefix = "langRadio-";
            String langId = radioItem.getId().substring(langPrefix.length());
            Main.getInstance().loadView(langId);
        }
        throw new NullPointerException();
    }

}
