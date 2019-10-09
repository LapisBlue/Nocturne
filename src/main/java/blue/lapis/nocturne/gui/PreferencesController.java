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

import static blue.lapis.nocturne.util.Constants.PREFS_FILE_NAME;
import static blue.lapis.nocturne.util.helper.FilesystemHelper.getNocturneDirectory;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.preferences.PreferenceType;
import blue.lapis.nocturne.preferences.PreferencesContext;
import blue.lapis.nocturne.util.Constants;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

    private PreferencesContext prefsCtx;

    @FXML private Button applyButton;
    @FXML private Button cancelButton;
    @FXML private Button okButton;

    @FXML private CheckBox colorblindToggle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.prefsCtx = Main.getPreferencesContext().copy();
    }

    public void applyPreferences(ActionEvent actionEvent) {
        try {
            Main.getPreferencesContext().mergeFrom(this.prefsCtx);
            Main.getPreferencesContext().saveTo(getNocturneDirectory().resolve(PREFS_FILE_NAME));
        } catch (Throwable t) {
            Main.getLogger().severe("Failed to save preferences to disk");
            t.printStackTrace();
        }
    }

    public void closePreferences(ActionEvent actionEvent) {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    public void submitPreferences(ActionEvent actionEvent) {
        applyPreferences(actionEvent);
        closePreferences(actionEvent);
    }

    public void toggleColorblindMode(ActionEvent actionEvent) {
        this.prefsCtx.setPreference(PreferenceType.COLORBLIND_MODE, colorblindToggle.isSelected());
    }
}
