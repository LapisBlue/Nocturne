package blue.lapis.nocturne.gui.control;

import blue.lapis.nocturne.Main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Hyperlink;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a {@link Hyperlink} to a web resource.
 */
public class WebLink extends Hyperlink {

    public WebLink() {
        setOnAction(event -> {
            String url = getUrl();

            try { // First try the JavaFX way to open URLs (doesn't seem to be supported everywhere)
                Main.getInstance().getHostServices().showDocument(url);
            } catch (Throwable ignored) {
            }

            try {
                if ((System.getProperty("os.name").contains("nix") || System.getProperty("os.name").contains("nux"))
                        && (new File("/usr/bin/xdg-open").exists() || new File("/usr/local/bin/xdg-open").exists())) {
                    // Work-around to support non-GNOME Linux desktop environments with xdg-open installed
                    new ProcessBuilder("xdg-open", url).start();
                } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    Main.getLogger().warning("Could not open hyperlink (not supported)");
                }
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private final StringProperty urlProperty = new SimpleStringProperty(this, "url");

    public final StringProperty urlProperty() {
        return urlProperty;
    }

    public final String getUrl() {
        return urlProperty.get();
    }

    public final void setUrl(String value) {
        urlProperty.set(value);
    }

}
