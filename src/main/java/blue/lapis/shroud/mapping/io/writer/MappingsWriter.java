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
package blue.lapis.shroud.mapping.io.writer;

import blue.lapis.shroud.mapping.MappingSet;
import blue.lapis.shroud.mapping.model.ClassMapping;
import blue.lapis.shroud.mapping.model.FieldMapping;
import blue.lapis.shroud.mapping.model.MethodMapping;

import java.io.PrintWriter;

/**
 * Super-interface for all writer classes.
 */
public abstract class MappingsWriter {

    //TODO: need to write docs for this when I have time (caseif)

    protected PrintWriter out;

    /**
     * Constructs a new {@link MappingsWriter} which outputs to the given
     * {@link PrintWriter}.
     *
     * @param out The {@link PrintWriter} to output to
     */
    protected MappingsWriter(PrintWriter out) {
        this.out = out;
    }

    /**
     * Writes the given {@link MappingSet} to this {@link MappingsWriter}'s
     * {@link PrintWriter}.
     *
     * @param mappings The {@link MappingSet} to write.
     */
    public abstract void write(MappingSet mappings);

    protected abstract void writeClassMapping(ClassMapping classMapping);

    protected abstract void writeFieldMapping(FieldMapping fieldMapping);

    protected abstract void writeMethodMapping(MethodMapping mapping);

}
