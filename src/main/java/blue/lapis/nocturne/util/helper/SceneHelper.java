package blue.lapis.nocturne.util.helper;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Static utility class for convenience methods regarding {@link Scene}s.
 */
public class SceneHelper {

    public static void addStdStylesheet(Scene scene) {
        scene.getStylesheets().add("css/nocturne.css");
    }

    public static void addStdStylesheet(Parent parent) {
        parent.getStylesheets().add("css/nocturne.css");
    }

}
