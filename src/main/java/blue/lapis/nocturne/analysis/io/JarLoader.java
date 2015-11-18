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
package blue.lapis.nocturne.analysis.io;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.analysis.model.ClassSet;
import blue.lapis.nocturne.analysis.model.JarClassEntry;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Utility class for loading JAR files.
 */
public class JarLoader {

    /**
     * Loads the given JAR {@link File} for use with Nocturne.
     *
     * @param jarFile The {@link File} handle to the JAR file to load
     * @return A {@link ClassSet} representing the JAR file
     * @throws IOException If an exception occurs while loading the provided
     *     {@link File}
     */
    public static ClassSet loadJar(File jarFile) throws IOException {
        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (ZipException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(Main.getResourceBundle().getString("jarload.invalid"));
            alert.showAndWait();
            return null;
        }

        Set<JarClassEntry> classes = new HashSet<>();

        jar.stream().forEach(entry -> {
            if (!entry.getName().endsWith(".class")) {
                return; // not a class so we can ignore it
            }

            try {
                InputStream entryStream = jar.getInputStream(entry);

                byte[] bytes = new byte[entryStream.available()];
                //noinspection ResultOfMethodCallIgnored
                entryStream.read(bytes);
                JarClassEntry classEntry = new JarClassEntry(entry.getName(), bytes);
                //TODO: detect whether class is already deobfuscated (e.g. this is usually the case for entry classes)
                if (Main.getMappings().getMappings().keySet().contains(entry.getName())) {
                    classEntry.setDebfuscated(true);
                }
                classes.add(classEntry);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (classes.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText(Main.getResourceBundle().getString("jarload.empty"));
            alert.showAndWait();
            return null;
        }

        jar.close(); // release the handle (TODO: is this right since we have the data in memory?)
        return new ClassSet(classes);
    }

}
