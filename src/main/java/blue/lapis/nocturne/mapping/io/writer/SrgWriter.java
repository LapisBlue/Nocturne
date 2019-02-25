/*
 * Nocturne
 * Copyright (c) 2015-2018, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.mapping.io.writer;

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.function.Function;

/**
 * The mappings writer, for the SRG format.
 */
public class SrgWriter extends MappingsWriter {

    protected static final Comparator<ClassMapping> ALPHABETISE_CLASSES =
            comparingLength(ClassMapping::getFullObfuscatedName);

    protected static final Comparator<FieldMapping> ALPHABETISE_FIELDS =
            Comparator.comparing(mapping -> mapping.getObfuscatedName() + mapping.getObfuscatedType());

    protected static final Comparator<MethodMapping> ALPHABETISE_METHODS =
            Comparator.comparing(mapping -> mapping.getObfuscatedName() + mapping.getObfuscatedDescriptor().toString());

    private static <T> Comparator<T> comparingLength(final Function<? super T, String> keyExtractor) {
        return (c1, c2) -> {
            final String key1 = keyExtractor.apply(c1);
            final String key2 = keyExtractor.apply(c2);

            final String redacted1 = key1.contains("$") ? key1.substring(0, key1.indexOf('$')) : key1;
            final String redacted2 = key1.contains("$") ? key2.substring(0, key2.indexOf('$')) : key2;

            if (redacted1.length() != redacted2.length()) {
                return redacted1.length() - redacted2.length();
            }

            return key1.compareTo(key2);
        };
    }

    private final ByteArrayOutputStream clOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream fdOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream mdOut = new ByteArrayOutputStream();

    private final PrintWriter clWriter = new PrintWriter(clOut);
    private final PrintWriter fdWriter = new PrintWriter(fdOut);
    private final PrintWriter mdWriter = new PrintWriter(mdOut);

    /**
     * Constructs a new {@link SrgWriter} which outputs to the given
     * {@link PrintWriter}.
     *
     * @param out The {@link PrintWriter} to output to
     */
    public SrgWriter(PrintWriter out) {
        super(out);
    }

    @Override
    public void write(MappingContext mappingContext) {
        mappingContext.getMappings().values().stream().sorted(ALPHABETISE_CLASSES).forEach(this::writeClassMapping);
        clWriter.close();
        fdWriter.close();
        mdWriter.close();
        out.write(clOut.toString());
        out.write(fdOut.toString());
        out.write(mdOut.toString());
        out.close();
        try {
            clOut.close();
            fdOut.close();
            mdOut.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Writes the given {@link ClassMapping} to the {@link SrgWriter}'s
     * {@link PrintWriter}.
     *
     * @param classMapping The {@link ClassMapping} to write
     */
    protected void writeClassMapping(ClassMapping classMapping) {
        if (NOT_USELESS.test(classMapping)) {
            clWriter.format("CL: %s %s\n",
                    classMapping.getFullObfuscatedName(), classMapping.getFullDeobfuscatedName());
        }

        classMapping.getInnerClassMappings().values().stream()
                .sorted(ALPHABETISE_CLASSES)
                .forEach(this::writeClassMapping);
        classMapping.getFieldMappings().values().stream()
                .filter(NOT_USELESS)
                .sorted(ALPHABETISE_FIELDS)
                .forEach(this::writeFieldMapping);
        classMapping.getMethodMappings().values().stream()
                .filter(NOT_USELESS)
                .sorted(ALPHABETISE_METHODS)
                .forEach(this::writeMethodMapping);
    }

    /**
     * Writes the given {@link FieldMapping} to the {@link SrgWriter}'s
     * {@link PrintWriter}.
     *
     * @param fieldMapping The {@link FieldMapping} to write
     */
    protected void writeFieldMapping(FieldMapping fieldMapping) {
        fdWriter.format("FD: %s/%s %s/%s\n",
                fieldMapping.getParent().getFullObfuscatedName(), fieldMapping.getObfuscatedName(),
                fieldMapping.getParent().getFullDeobfuscatedName(), fieldMapping.getDeobfuscatedName());
    }

    /**
     * Writes the given {@link MethodMapping} to the {@link SrgWriter}'s
     * {@link PrintWriter}.
     *
     * @param mapping The {@link MethodMapping} to write
     */
    protected void writeMethodMapping(MethodMapping mapping) {
        mdWriter.format("MD: %s/%s %s %s/%s %s\n",
                mapping.getParent().getFullObfuscatedName(), mapping.getObfuscatedName(),
                mapping.getObfuscatedDescriptor(),
                mapping.getParent().getFullDeobfuscatedName(), mapping.getDeobfuscatedName(),
                mapping.getDeobfuscatedDescriptor());
    }
}
