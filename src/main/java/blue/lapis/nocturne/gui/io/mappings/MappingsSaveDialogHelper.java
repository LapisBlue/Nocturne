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
package blue.lapis.nocturne.gui.io.mappings;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.io.writer.SrgWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Static utility class for dialogs for saving mappings.
 */
public class MappingsSaveDialogHelper {

    public static void saveMappings() throws IOException {
        if (Main.currentMappingsPath == null) {
            saveMappingsAs();
            return;
        }

        saveMappings0();
    }

    public static void saveMappingsAs() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Destination File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SRG Mapping Files", "*.srg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showSaveDialog(Main.mainStage);

        if (selectedFile == null) {
            return;
        }

        Path selectedPath = selectedFile.toPath();

        if (Files.notExists(selectedPath)) {
            Files.createFile(selectedPath);
        }

        if (!Files.isSameFile(Main.currentMappingsPath, selectedPath)) {
            Main.mappings.setDirty(true);
        }

        Main.currentMappingsPath = selectedPath;

        saveMappings0();
    }

    private static void saveMappings0() throws IOException {
        if (Main.mappings.isDirty()) {
            try (SrgWriter writer = new SrgWriter(new PrintWriter(Files.newBufferedWriter(Main.currentMappingsPath)))) {
                writer.write(Main.mappings);
            }

            Main.mappings.setDirty(false);
        }
    }

    /**
     * Prompts the user to save the current mappings if dirty.
     *
     * @return {@code true} if the user cancelled the action, {@code false}
     *     otherwise
     * @throws IOException If an exception occurs while saving the mappings
     */
    public static boolean doDirtyConfirmation() throws IOException {
        if (Main.mappings.isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save?");
            alert.setHeaderText(null);
            alert.setContentText("Would you like to save the current mappings?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                saveMappingsAs();
            } else if (alert.getResult() == ButtonType.CANCEL) {
                return true;
            }
        }
        return false;
    }

}
