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

package blue.lapis.nocturne.jar.io;

import static blue.lapis.nocturne.processor.index.model.IndexedClass.INDEXED_CLASSES;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.ClassSet;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.processor.index.ClassHierarchyBuilder;
import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.util.Constants;

import com.google.common.collect.Sets;
import javafx.scene.control.Alert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipException;

/**
 * Utility class for loading JAR files.
 */
public class JarLoader {

    /**
     * Loads a JAR from the given {@link InputStream} for use with Nocturne.
     *
     * @param jarFile The {@link InputStream} containing the JAR file to load
     * @return A {@link ClassSet} representing the JAR file
     * @throws IOException If an exception occurs while loading the provided
     *     {@link File}
     */
    public static ClassSet loadJar(InputStream jarFile) throws IOException {
        IndexedClass.INDEXED_CLASSES.clear();

        JarInputStream jar;
        try {
            jar = new JarInputStream(jarFile);
        } catch (ZipException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(Main.getResourceBundle().getString("jarload.invalid"));
            alert.showAndWait();
            return null;
        }

        Set<JarClassEntry> classes = new HashSet<>();

        JarEntry entry;
        while ((entry = jar.getNextJarEntry()) != null) {
            if (!entry.getName().endsWith(".class")) {
                continue; // not a class so we can ignore it
            }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int b;
                while ((b = jar.read()) != -1) {
                    baos.write(b);
                }
                byte[] bytes = baos.toByteArray();

                String className = entry.getName();
                if (className.endsWith(Constants.CLASS_FILE_NAME_TAIL)) {
                    className = className.substring(0, className.length() - Constants.CLASS_FILE_NAME_TAIL.length());
                }

                JarClassEntry classEntry = new JarClassEntry(className, bytes);

                //TODO: detect whether class is already deobfuscated (e.g. this is usually the case for entry classes)
                ClassMapping mapping = Main.getMappingContext().getMappings().get(className);
                if (mapping != null && !mapping.getObfuscatedName().equals(mapping.getDeobfuscatedName())) {
                    classEntry.setDeobfuscated(true);
                }
                classes.add(classEntry);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (classes.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText(Main.getResourceBundle().getString("jarload.empty"));
            alert.showAndWait();
            return null;
        }

        jar.close(); // release the resource
        ClassSet cs = new ClassSet(classes);
        Main.setLoadedJar(cs);
        cs.getClasses().stream().forEach(JarClassEntry::index);
        new ClassHierarchyBuilder(Sets.newHashSet(INDEXED_CLASSES.values())).buildHierarchies();
        cs.getClasses().stream().forEach(JarClassEntry::process);
        INDEXED_CLASSES.values().forEach(IndexedClass::clearPool);
        return cs;
    }

}
