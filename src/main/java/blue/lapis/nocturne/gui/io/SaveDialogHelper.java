package blue.lapis.nocturne.gui.io;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.io.writer.SrgWriter;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * Handles save dialogs.
 */
public class SaveDialogHelper {

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

        if (!selectedFile.exists()) {
            Files.createFile(selectedFile.toPath());
        }

        Main.currentMappingsPath = selectedFile.toPath();

        saveMappings0();
    }

    private static void saveMappings0() throws IOException {
        if (Main.mappings.isDirty()) {
            SrgWriter writer = new SrgWriter(new PrintWriter(Main.currentMappingsPath.toFile()));
            writer.write(Main.mappings);
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
