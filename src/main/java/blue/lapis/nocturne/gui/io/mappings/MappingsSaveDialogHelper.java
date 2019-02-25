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

package blue.lapis.nocturne.gui.io.mappings;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.MappingFormat;
import blue.lapis.nocturne.mapping.io.writer.MappingsWriter;
import blue.lapis.nocturne.util.helper.PropertiesHelper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Static utility class for dialogs for saving mappings.
 */
public final class MappingsSaveDialogHelper {

    private MappingsSaveDialogHelper() {
    }

    public static void saveMappings() throws IOException {
        if (Main.getCurrentMappingsPath() == null) {
            saveMappingsAs();
            return;
        }

        saveMappings0(Main.getCurrentMappingFormat());
    }

    public static boolean saveMappingsAs() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.getResourceBundle().getString("filechooser.save_mapping"));
        Arrays.asList(MappingFormat.values()).forEach(t -> {
            fileChooser.getExtensionFilters().add(t.getExtensionFilter());
            if (Main.getPropertiesHelper().getProperty(PropertiesHelper.Key.LAST_MAPPING_SAVE_FORMAT)
                    .equals(t.name())) {
                fileChooser.setSelectedExtensionFilter(t.getExtensionFilter());
            }
        });

        String lastDir = Main.getPropertiesHelper().getProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY);
        if (!lastDir.isEmpty()) {
            File initialDir = new File(lastDir);
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        }

        File selectedFile = fileChooser.showSaveDialog(Main.getMainStage());
        if (selectedFile == null) {
            return false;
        }
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY, selectedFile.getParent());

        Path selectedPath = selectedFile.toPath();

        if (Files.notExists(selectedPath)) {
            Files.createFile(selectedPath);
        }

        if (Main.getCurrentMappingsPath() == null || !Files.isSameFile(Main.getCurrentMappingsPath(), selectedPath)) {
            Main.getMappingContext().setDirty(true);
        }

        final Optional<MappingFormat> mappingFormat
                = MappingFormat.fromExtensionFilter(fileChooser.getSelectedExtensionFilter());

        if (!mappingFormat.isPresent()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(Main.getResourceBundle().getString("filechooser.no_extension.title"));
            alert.setContentText(Main.getResourceBundle().getString("filechooser.no_extension"));

            alert.showAndWait();
            return false;
        }

        Main.setCurrentMappingsPath(selectedFile.toPath());
        Main.setCurrentMappingFormat(mappingFormat.get());

        saveMappings0(mappingFormat.get());
        Main.getPropertiesHelper()
                .setProperty(PropertiesHelper.Key.LAST_MAPPING_SAVE_FORMAT, mappingFormat.get().name());
        return true;
    }

    private static void saveMappings0(MappingFormat format) throws IOException {
        if (Main.getMappingContext().isDirty()) {
            try (MappingsWriter writer
                         = format.createWriter(new PrintWriter(Files.newOutputStream(Main.getCurrentMappingsPath())))) {
                writer.write(Main.getMappingContext());
            }

            Main.getMappingContext().setDirty(false);
        }
    }

    /**
     * Prompts the user to save the current mappings if dirty.
     *
     * @return {@code true} if the user cancelled the action, {@code false}
     * otherwise
     * @throws IOException If an exception occurs while saving the mappings
     */
    public static boolean doDirtyConfirmation() throws IOException {
        if (Main.getMappingContext().isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(Main.getResourceBundle().getString("filechooser.dirty.title"));
            alert.setHeaderText(null);
            alert.setContentText(Main.getResourceBundle().getString("filechooser.dirty.content"));
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                return !saveMappingsAs();
            } else if (alert.getResult() == ButtonType.CANCEL) {
                return true;
            }
        }
        return false;
    }

}
