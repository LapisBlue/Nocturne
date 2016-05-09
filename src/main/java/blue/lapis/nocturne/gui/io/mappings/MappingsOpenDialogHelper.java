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

package blue.lapis.nocturne.gui.io.mappings;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.MappingReaderType;
import blue.lapis.nocturne.mapping.io.reader.MappingsReader;
import blue.lapis.nocturne.util.helper.PropertiesHelper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Static utility class for dialogs for opening mappings.
 */
public final class MappingsOpenDialogHelper {

    private MappingsOpenDialogHelper() {
    }

    public static void openMappings() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.getResourceBundle().getString("filechooser.open_mapping"));
        Arrays.asList(MappingReaderType.values())
                .forEach(t -> fileChooser.getExtensionFilters().add(t.getExtensionFilter()));

        String lastDir = Main.getPropertiesHelper().getProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY);
        if (!lastDir.isEmpty()) {
            File initialDir = new File(lastDir);
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        }

        File selectedFile = fileChooser.showOpenDialog(Main.getMainStage());
        if (selectedFile == null) {
            return;
        }
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY, selectedFile.getParent());

        Path selectedPath = selectedFile.toPath();

        if (Files.exists(selectedPath)) { //TODO: isn't this redundant?
            MappingReaderType type = MappingReaderType.fromExtensionFilter(fileChooser.getSelectedExtensionFilter());
            try (MappingsReader reader = type.constructReader(new BufferedReader(new FileReader(selectedFile)))) {
                if (type == MappingReaderType.ENIGMA) {
                    Alert confirmEngima = new Alert(Alert.AlertType.CONFIRMATION,
                            Main.getResourceBundle().getString("enigma.support_notice"),
                            ButtonType.YES, ButtonType.NO);
                    confirmEngima.showAndWait();

                    if (confirmEngima.getResult() == ButtonType.NO) {
                        return;
                    }
                }

                MappingContext context = reader.read();
                Main.getMappingContext().assimilate(context);
                MainController.INSTANCE.updateClassViews();
                Main.getMappingContext().setDirty(false);
            }

            Main.setCurrentMappingsPath(selectedPath);
        }
    }

}
