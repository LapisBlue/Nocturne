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
import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import blue.lapis.nocturne.mapping.io.writer.SrgWriter;
import blue.lapis.nocturne.util.Constants;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * The main Java.FX controller.
 */
public class MainController implements Initializable {

    public MenuItem openJarButton;
    public MenuItem closeJarButton;
    public MenuItem openMappingsButton;
    public MenuItem closeMappingsButton;
    public MenuItem saveMappingsButton;
    public MenuItem saveMappingsAsButton;
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
        fileChooser.setTitle("Select jar File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jar Files", "*.jar")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
        //TODO
    }

    public void closeJar(ActionEvent actionEvent) {
        //TODO
    }

    public void openMappings(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Mapping File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SRG Files", "*.srg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);

        if (selectedFile != null) {
            SrgReader reader = new SrgReader(new BufferedReader(new FileReader(selectedFile)));
            MappingSet mappingSet = reader.read();
            if (Main.mappings == null) {
                Main.mappings = mappingSet;
            } else {
                Main.mappings.merge(mappingSet);
            }
            this.closeMappingsButton.setDisable(false);
            this.saveMappingsButton.setDisable(false);
        }
    }

    public void closeMappings(ActionEvent actionEvent) {
        Main.mappings = null;

        this.closeMappingsButton.setDisable(true);
        this.saveMappingsButton.setDisable(true);
    }

    public void saveMappings(ActionEvent actionEvent) throws IOException {
        if (Main.currentMappingsPath == null) {
            saveMappingsAs(actionEvent);
            return;
        }

        saveMappings();
    }

    public void saveMappingsAs(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Destination File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SRG Files", "*.srg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
        if (!selectedFile.exists()) {
            Files.createFile(selectedFile.toPath());
        }

        saveMappings();
    }

    private void saveMappings() throws IOException {
        SrgWriter writer = new SrgWriter(new PrintWriter(Main.currentMappingsPath.toFile()));
        writer.write(Main.mappings);
    }

    public void onClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void showAbout(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Nocturne");
        alert.setHeaderText("Nocturne " + Constants.VERSION);
        alert.setContentText("Copyright (c) 2015 Lapis.\n"
                + "This software is made available under the MIT license.\n"
                + "\n"
                + "Github: https://github.com/LapisBlue/Nocturne");
        alert.showAndWait();
    }

}
