//******************************************************************************
// Copyright (c) Jamie Mansfield <https://jamiemansfield.me/>
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//******************************************************************************

package blue.lapis.nocturne.gui.io;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.util.helper.PropertiesHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.lorenz.io.MappingFormats;

import java.io.File;
import java.io.IOException;

/**
 * A helper class for reading and writing mappings to
 * file, graphically.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public final class MappingsDialogHelper {

    private static final BiMap<FileChooser.ExtensionFilter, MappingFormat> FORMATS = HashBiMap.create();
    private static final FileChooser FILE_CHOOSER = new FileChooser();

    private static void register(final FileChooser.ExtensionFilter filter,
                                 final MappingFormat format,
                                 final boolean isDefault) {
        FORMATS.put(filter, format);
        FILE_CHOOSER.getExtensionFilters().add(filter);
        if (isDefault) FILE_CHOOSER.setSelectedExtensionFilter(filter);
    }

    private static void register(final FileChooser.ExtensionFilter filter,
                                 final MappingFormat format) {
        register(filter, format, false);
    }

    private static void setFormat(final PropertiesHelper.Key key) {
        final String lastFormat = Main.getPropertiesHelper().getProperty(key);
        if (lastFormat != null && !lastFormat.isEmpty()) {
            final MappingFormat format = MappingFormats.byId(lastFormat);
            if (format != null) {
                FILE_CHOOSER.setSelectedExtensionFilter(FORMATS.inverse().get(format));
            }
        }
    }

    static {
        // Register the formats Nocturne supports
        register(Formats.SRG, MappingFormats.SRG, true);
        register(Formats.CSRG, MappingFormats.CSRG);
        register(Formats.TSRG, MappingFormats.TSRG);
        register(Formats.ENIGMA, MappingFormats.byId("enigma"));
        register(Formats.JAM, MappingFormats.byId("jam"));
        register(Formats.KIN, MappingFormats.byId("kin"));

        // Open up at the same directory
        final String lastDir = Main.getPropertiesHelper().getProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY);
        if (lastDir != null && !lastDir.isEmpty()) {
            final File initialDir = new File(lastDir);
            if (initialDir.exists()) {
                FILE_CHOOSER.setInitialDirectory(initialDir);
            }
        }
    }

    /**
     * Loads mappings to the given {@link MappingSet}, in the {@link Window}.
     *
     * @param window The parent window
     * @param mappings The mappings
     * @return {@code true} if the mappings were read successfully;
     *         {@code false} otherwise
     */
    public static boolean loadMappings(final Window window, final MappingSet mappings) {
        // Setup the file chooser appropriately for the operation being done
        FILE_CHOOSER.setTitle("Load Mappings");

        // Use the same format
        setFormat(PropertiesHelper.Key.LAST_MAPPING_LOAD_FORMAT);

        // Gets the mappings
        final File mappingsPath = FILE_CHOOSER.showOpenDialog(window);
        if (mappingsPath == null) return false;
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY, mappingsPath.getParent());

        // Reads from file
        final MappingFormat format = FORMATS.get(FILE_CHOOSER.getSelectedExtensionFilter());
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPING_LOAD_FORMAT, format.toString());
        try {
            format.read(mappings, mappingsPath.toPath());
        }
        catch (final IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves mappings from the given {@link MappingSet}, in the {@link Window}.
     *
     * @param window The parent window
     * @param mappings The mappings
     * @return {@code true} if the mappings were written successfully;
     *         {@code false} otherwise
     */
    public static boolean saveMappingsAs(final Window window, final MappingSet mappings) {
        // Setup the file chooser appropriately for the operation being done
        FILE_CHOOSER.setTitle("Save Mappings");

        // Use the same format
        setFormat(PropertiesHelper.Key.LAST_MAPPING_SAVE_FORMAT);

        // Gets the mappings
        final File mappingsPath = FILE_CHOOSER.showSaveDialog(window);
        if (mappingsPath == null) return false;
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPINGS_DIRECTORY, mappingsPath.getParent());

        // Reads from file
        final MappingFormat format = FORMATS.get(FILE_CHOOSER.getSelectedExtensionFilter());
        Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LAST_MAPPING_SAVE_FORMAT, format.toString());
        try {
            format.write(mappings, mappingsPath.toPath());
        }
        catch (final IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private MappingsDialogHelper() {
    }

    /**
     * A pseudo-enumeration of {@link FileChooser.ExtensionFilter}s.
     */
    public static final class Formats {

        public static final FileChooser.ExtensionFilter SRG = new FileChooser.ExtensionFilter(
                "SRG Files",
                "*.srg"
        );

        public static final FileChooser.ExtensionFilter CSRG = new FileChooser.ExtensionFilter(
                "CSRG Files",
                "*.csrg"
        );

        public static final FileChooser.ExtensionFilter TSRG = new FileChooser.ExtensionFilter(
                "TSRG Files",
                "*.tsrg"
        );

        public static final FileChooser.ExtensionFilter ENIGMA = new FileChooser.ExtensionFilter(
                "Enigma Files",
                "*.enigma", "*.mappings"
        );

        public static final FileChooser.ExtensionFilter JAM = new FileChooser.ExtensionFilter(
                "JAM Files",
                "*.jam"
        );

        public static final FileChooser.ExtensionFilter KIN = new FileChooser.ExtensionFilter(
                "Kin Files",
                "*.kin"
        );

        private Formats() {
        }

    }

}
