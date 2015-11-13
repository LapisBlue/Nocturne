/*
 * Shroud
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

import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import java.io.PrintWriter;

/**
 * The mappings writer, for the SRG format.
 */
public class SrgWriter extends MappingsWriter {

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
    public void write(MappingSet mappings) {
        mappings.getMappings().values().forEach(this::writeClassMapping);
    }

    @Override
    protected void writeClassMapping(ClassMapping classMapping) {
        if (classMapping instanceof TopLevelClassMapping) {
            out.format("CL: %s %s\n",
                    classMapping.getObfuscatedName(), classMapping.getDeobfuscatedName());
        } else if (classMapping instanceof InnerClassMapping) {
            InnerClassMapping mapping = (InnerClassMapping) classMapping;
            out.format("CL: %s %s\n",
                    mapping.getFullObfuscatedName(), mapping.getFullDeobfuscatedName());
        }

        classMapping.getFieldMappings().values().forEach(this::writeFieldMapping);
        classMapping.getMethodMappings().values().forEach(this::writeMethodMapping);
        classMapping.getInnerClassMappings().values().forEach(this::writeClassMapping);
    }

    @Override
    protected void writeFieldMapping(FieldMapping fieldMapping) {
        out.format("FD: %s/%s %s/%s\n",
                fieldMapping.getParent().getObfuscatedName(), fieldMapping.getObfuscatedName(),
                fieldMapping.getParent().getDeobfuscatedName(), fieldMapping.getDeobfuscatedName());
    }

    @Override
    protected void writeMethodMapping(MethodMapping mapping) {
        out.format("MD: %s/%s %s %s/%s %s\n",
                mapping.getParent().getObfuscatedName(), mapping.getObfuscatedName(), mapping.getSignature(),
                mapping.getParent().getDeobfuscatedName(), mapping.getDeobfuscatedName(),
                mapping.getDeobfuscatedSignature());
    }
}
