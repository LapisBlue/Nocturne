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
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Static utility class for dialogs for opening mappings.
 */
public final class MappingsOpenDialogHelper {

    private MappingsOpenDialogHelper() {
    }

    public static void openMappings() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.resourceBundle.getString("filechooser.open_mapping"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Main.resourceBundle.getString("filechooser.type_srg"), "*.srg"),
                new FileChooser.ExtensionFilter(Main.resourceBundle.getString("filechooser.type_all"), "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
        if (selectedFile == null) {
            return;
        }

        Path selectedPath = selectedFile.toPath();

        if (Files.exists(selectedPath)) {
            MappingContext context;
            try (SrgReader reader = new SrgReader(Files.newBufferedReader(selectedPath))) {
                context = reader.read();
            }

            Main.mappings.merge(context);
            Main.mappings.setDirty(false);

            Main.currentMappingsPath = selectedPath;
        }
    }

}
