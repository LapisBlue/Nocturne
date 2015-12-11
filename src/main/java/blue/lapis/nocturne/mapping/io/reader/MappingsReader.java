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

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.mapping.model.attribute.MethodSignature;
import blue.lapis.nocturne.util.Constants;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * Superclass for all reader classes.
 */
public abstract class MappingsReader implements Closeable {

    protected BufferedReader reader;

    protected MappingsReader(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Reads from the given {@link BufferedReader}.
     *
     * @return A {@link MappingContext} from the {@link BufferedReader}.
     */
    public abstract MappingContext read();

    protected void genClassMapping(MappingContext context, String obf, String deobf) {
        if (obf.contains(INNER_CLASS_SEPARATOR_CHAR + "")) {
            // escape the separator char so it doesn't get parsed as regex
            String[] obfSplit = INNER_CLASS_SEPARATOR_PATTERN.split(obf);
            String[] deobfSplit = INNER_CLASS_SEPARATOR_PATTERN.split(deobf);
            if (obfSplit.length != deobfSplit.length) { // non-inner mapped to inner or vice versa
                Main.getLogger().warning("Unsupported mapping: " + obf + " <-> " + deobf);
                return; // ignore it
            }

            // iteratively get the direct parent class to this inner class
            ClassMapping parent = getOrCreateClassMapping(context,
                    obf.substring(0, obf.lastIndexOf(INNER_CLASS_SEPARATOR_CHAR)));

            new InnerClassMapping(parent, obfSplit[obfSplit.length - 1],
                    deobfSplit[deobfSplit.length - 1]);
        } else {
            context.addMapping(new TopLevelClassMapping(context, obf, deobf));
        }
    }

    protected void genFieldMapping(MappingContext context, String obf, String deobf) {
        int lastIndex = obf.lastIndexOf(Constants.CLASS_PATH_SEPARATOR_CHAR);
        String owningClass = obf.substring(0, lastIndex);
        String obfName = obf.substring(lastIndex + 1);

        String deobfName = deobf.substring(deobf.lastIndexOf(Constants.CLASS_PATH_SEPARATOR_CHAR) + 1);

        ClassMapping parent = getOrCreateClassMapping(context, owningClass);
        new FieldMapping(parent, obfName, deobfName, null);
    }

    protected void genMethodMapping(MappingContext context, String obf, String obfSig, String deobf,
            String deobfSig) {
        int lastIndex = obf.lastIndexOf(Constants.CLASS_PATH_SEPARATOR_CHAR);
        String owningClass = obf.substring(0, lastIndex);
        String obfName = obf.substring(lastIndex + 1);

        String deobfName = deobf.substring(deobf.lastIndexOf(Constants.CLASS_PATH_SEPARATOR_CHAR) + 1);

        ClassMapping parent = getOrCreateClassMapping(context, owningClass);
        new MethodMapping(parent, obfName, deobfName, new MethodSignature(obfSig));
    }

    protected int getClassNestingLevel(String name) {
        return name.split(" ")[1].length()
                - name.split(" ")[1].replace(INNER_CLASS_SEPARATOR_CHAR + "", "").length();
    }

    private static Optional<ClassMapping> getClassMapping(MappingContext context, String qualifiedName,
                                                          boolean create) {
        String[] arr = INNER_CLASS_SEPARATOR_PATTERN.split(qualifiedName);

        ClassMapping mapping = context.getMappings().get(arr[0]);
        if (mapping == null) {
            if (create) {
                mapping = new TopLevelClassMapping(context, arr[0], arr[0]);
                context.addMapping((TopLevelClassMapping) mapping);
            } else {
                return Optional.empty();
            }
        }

        for (int i = 1; i < arr.length; i++) {
            ClassMapping child = mapping.getInnerClassMappings().get(arr[i]);
            if (child == null) {
                if (create) {
                    child = new InnerClassMapping(mapping, arr[i], arr[i]);
                } else {
                    return Optional.empty();
                }
            }
            mapping = child;
        }

        return Optional.of(mapping);
    }

    /**
     * Gets the {@link ClassMapping} for the given qualified name, iteratively
     * creating mappings for both outer and inner classes as needed if they do
     * not exist.
     *
     * @param context The {@link MappingContext} to use
     * @param qualifiedName The fully-qualified name of the class to get a
     *     mapping for
     * @return The retrieved or created {@link ClassMapping}
     */
    public static ClassMapping getOrCreateClassMapping(MappingContext context, String qualifiedName) {
        return getClassMapping(context, qualifiedName, true).get();
    }

    public static Optional<ClassMapping> getClassMapping(MappingContext context, String qualifiedName) {
        return getClassMapping(context, qualifiedName, false);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
