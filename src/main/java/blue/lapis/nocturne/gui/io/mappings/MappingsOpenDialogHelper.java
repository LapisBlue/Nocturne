package blue.lapis.nocturne.gui.io.mappings;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;

import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Static utility class for dialogs for opening mappings.
 */
public class MappingsOpenDialogHelper {

    public static void openMappings() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.resourceBundle.getString("filechooser.open_mapping"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Main.resourceBundle.getString("filechooser.type_srg"), "*.srg"),
                new FileChooser.ExtensionFilter(Main.resourceBundle.getString("filechooser.type_all"), "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);

        if (selectedFile != null && selectedFile.exists()) {
            SrgReader reader = new SrgReader(new BufferedReader(new FileReader(selectedFile)));
            MappingContext context = reader.read();
            Main.mappings.merge(context);
            Main.mappings.setDirty(false);

            Main.currentMappingsPath = selectedFile.toPath();
        }
    }

}
