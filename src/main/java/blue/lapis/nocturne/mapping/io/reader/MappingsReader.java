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
package blue.lapis.nocturne.mapping.io.reader;

import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_PATTERN;

import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import java.io.BufferedReader;

/**
 * Superclass for all reader classes.
 */
public abstract class MappingsReader {

    protected BufferedReader reader;

    protected MappingsReader(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Reads from the given {@link BufferedReader}.
     *
     * @return A {@link MappingSet} from the {@link BufferedReader}.
     */
    public abstract MappingSet read();

    protected abstract void genClassMapping(MappingSet mappingSet, String obf, String deobf);

    protected abstract void genFieldMapping(MappingSet mappingSet, String obf, String deobf);

    protected abstract void genMethodMapping(MappingSet mappingSet, String obf, String obfSig, String deobf,
            String deobfSig);

    protected int getClassNestingLevel(String name) {
        return name.split(" ")[1].length()
                - name.split(" ")[1].replace(INNER_CLASS_SEPARATOR_CHAR + "", "").length();
    }

    /**
     * Gets the {@link ClassMapping} for the given qualified name, iteratively
     * creating mappings for both outer and inner classes as needed if they do
     * not exist.
     *
     * @param mappingSet The {@link MappingSet} to use
     * @param qualifiedName The fully-qualified name of the class to get a
     *     mapping for
     * @return The retrieved or created {@link ClassMapping}
     */
    protected static ClassMapping getOrCreateClassMapping(MappingSet mappingSet, String qualifiedName) {
        String[] arr = INNER_CLASS_SEPARATOR_PATTERN.split(qualifiedName);

        ClassMapping mapping = mappingSet.getMappings().get(arr[0]);
        if (mapping == null) {
            mapping = new TopLevelClassMapping(mappingSet, arr[0], arr[0]);
            mappingSet.addMapping((TopLevelClassMapping) mapping);
        }

        for (int i = 1; i < arr.length; i++) {
            ClassMapping child = mapping.getInnerClassMappings().get(arr[i]);
            if (child == null) {
                child = new InnerClassMapping(mapping, arr[i], arr[i]);
            }
            mapping = child;
        }

        return mapping;
    }

}
