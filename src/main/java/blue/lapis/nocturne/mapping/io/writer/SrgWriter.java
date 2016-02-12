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
package blue.lapis.nocturne.mapping.io.writer;

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.Mapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Predicate;

/**
 * The mappings writer, for the SRG format.
 */
public class SrgWriter extends MappingsWriter {

    private static final Predicate<Mapping> NOT_USELESS
            = mapping -> !mapping.getObfuscatedName().equals(mapping.getDeobfuscatedName());

    private final ByteArrayOutputStream clOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream fdOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream mdOut = new ByteArrayOutputStream();

    private final PrintWriter clWriter = new PrintWriter(clOut, true);
    private final PrintWriter fdWriter = new PrintWriter(fdOut, true);
    private final PrintWriter mdWriter = new PrintWriter(mdOut, true);

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
        mappingContext.getMappings().values().forEach(this::writeClassMapping);
        out.write(clOut.toString());
        out.write(fdOut.toString());
        out.write(mdOut.toString());
        out.flush();
    }

    @Override
    protected void writeClassMapping(ClassMapping classMapping) {
        if (!classMapping.getObfuscatedName().equals(classMapping.getDeobfuscatedName())) {
            if (classMapping instanceof TopLevelClassMapping) {
                clWriter.format("CL: %s %s\n",
                        classMapping.getObfuscatedName(), classMapping.getDeobfuscatedName());
            } else if (classMapping instanceof InnerClassMapping) {
                InnerClassMapping mapping = (InnerClassMapping) classMapping;
                clWriter.format("CL: %s %s\n",
                        mapping.getFullObfuscatedName(), mapping.getFullDeobfuscatedName());
            }
        }

        classMapping.getInnerClassMappings().values().stream().filter(NOT_USELESS).forEach(this::writeClassMapping);
        classMapping.getFieldMappings().values().stream().filter(NOT_USELESS).forEach(this::writeFieldMapping);
        classMapping.getMethodMappings().values().stream().filter(NOT_USELESS).forEach(this::writeMethodMapping);
    }

    @Override
    protected void writeFieldMapping(FieldMapping fieldMapping) {
        fdWriter.format("FD: %s/%s %s/%s\n",
                fieldMapping.getParent().getObfuscatedName(), fieldMapping.getObfuscatedName(),
                fieldMapping.getParent().getDeobfuscatedName(), fieldMapping.getDeobfuscatedName());
    }

    @Override
    protected void writeMethodMapping(MethodMapping mapping) {
        mdWriter.format("MD: %s/%s %s %s/%s %s\n",
                mapping.getParent().getObfuscatedName(), mapping.getObfuscatedName(), mapping.getObfuscatedDescriptor(),
                mapping.getParent().getDeobfuscatedName(), mapping.getDeobfuscatedName(),
                mapping.getDeobfuscatedDescriptor());
    }
}
