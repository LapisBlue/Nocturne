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

package blue.lapis.nocturne.gui.io.jar;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.jar.io.JarLoader;
import blue.lapis.nocturne.jar.model.ClassSet;
import blue.lapis.nocturne.util.helper.PropertiesHelper;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Static utility class for JAR open/save dialogs.
 */
public final class JarDialogHelper {

    private static Dialog<Boolean> loadDialog;

    static {
        loadDialog = new Dialog<>();
        loadDialog.setTitle(Main.getResourceBundle().getString("dialog.load_jar.title"));
        loadDialog.setHeaderText(null);
        loadDialog.setContentText(Main.getResourceBundle().getString("dialog.load_jar.content"));
        loadDialog.setResult(false);
    }

    private JarDialogHelper() {
    }

    public static void openJar(MainController controller) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.getResourceBundle().getString("filechooser.open_jar"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Main.getResourceBundle().getString("filechooser.type_jar"), "*.jar")
        );

        String lastDir = Main.getPropertiesHelper().getProperty(PropertiesHelper.Key.LAST_JAR_DIRECTORY);
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
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_JAR_DIRECTORY, selectedFile.getParent());

        if (Files.exists(selectedFile.toPath())) {
            loadDialog.show();
            ClassSet classSet;
            try {
                classSet = JarLoader.loadJar(selectedFile.getAbsolutePath(), new FileInputStream(selectedFile));
            } finally {
                loadDialog.close();
            }
            if (classSet != null) {
                controller.closeJarButton.setDisable(false);
                controller.loadMappingsButton.setDisable(false);
                controller.saveMappingsAsButton.setDisable(false);
                controller.resetMappingsButton.setDisable(false);
            }
        }
    }

}
