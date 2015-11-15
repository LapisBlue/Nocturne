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

import blue.lapis.nocturne.analysis.model.JarClassEntry;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Utility class for loading JAR files.
 */
public class JarLoader {

    public static ImmutableSet<JarClassEntry> loadJar(File jarFile) throws IOException {
        JarInputStream jIs = new JarInputStream(new FileInputStream(jarFile));

        Set<JarClassEntry> classes = new HashSet<>();

        JarEntry entry;
        while ((entry = jIs.getNextJarEntry()) != null) {
            if (entry.getSize() > Integer.MAX_VALUE) {
                System.out.println("Not reading JAR entry " + entry.getName() + " - it's too damn big");
                continue;
            }
            byte[] bytes = new byte[(int) entry.getSize()];
            //noinspection ResultOfMethodCallIgnored
            jIs.read(bytes);
            classes.add(new JarClassEntry(entry.getName(), bytes));
        }
        return ImmutableSet.copyOf(classes);
    }

}
