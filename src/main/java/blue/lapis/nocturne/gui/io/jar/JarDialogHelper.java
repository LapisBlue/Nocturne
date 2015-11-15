package blue.lapis.nocturne.gui.io.jar;

import blue.lapis.nocturne.Main;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/**
 * Static utility class for JAR open/save dialogs.
 */
public class JarDialogHelper {

    public static void openJar() throws IOException {
        //TODO: close current JAR if applicable
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Main.resourceBundle.getString("filechooser.open_jar"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Main.resourceBundle.getString("filechooser.type_jar"), "*.jar")
        );
        File selectedFile = fileChooser.showOpenDialog(Main.mainStage);
        //TODO
    }

}
