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

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The main Java.FX controller.
 */
public class MainController implements Initializable {

    private static final String VERSION = "1.0.0"; //TODO: keep up to date

    public MenuItem openJarButton;
    public MenuItem closeJarButton;
    public MenuItem openMappingsButton;
    public MenuItem closeMappingsButton;
    public MenuItem saveMappingsButton;
    public MenuItem closeButton;
    public MenuItem aboutButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.closeJarButton.setDisable(true);
        this.openMappingsButton.setDisable(true);
        this.closeMappingsButton.setDisable(true);
        this.saveMappingsButton.setDisable(true);
    }

    public void openJar(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select jar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jar file", "*.jar")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
    }

    public void closeJar(ActionEvent actionEvent) {
        //TODO
    }

    public void openMappings(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select mappings");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("srg file", "*.srg")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
    }

    public void closeMappings(ActionEvent actionEvent) {
        //TODO
    }

    public void saveMappings(ActionEvent actionEvent) {
        //TODO
    }

    public void onClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void showAbout(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Nocturne");
        alert.setHeaderText("Nocturne " + VERSION);
        alert.setContentText("Copyright (c) 2015 Lapis.\n"
                + "This software is made available under the MIT license.\n"
                + "\n"
                + "Github: https://github.com/LapisBlue/Nocturne");
        alert.showAndWait();
    }

}
