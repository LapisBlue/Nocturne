/*
 * Nocturne
 * Copyright (c) 2015-2019, Lapis <https://github.com/LapisBlue>
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
import blue.lapis.nocturne.mapping.model.Mapping;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Predicate;

/**
 * Superclass for all writer classes.
 */
public abstract class MappingsWriter implements Closeable {

    protected static final Predicate<Mapping> NOT_USELESS
            = mapping -> !mapping.getObfuscatedName().equals(mapping.getDeobfuscatedName());

    protected final PrintWriter out;

    /**
     * Constructs a new {@link MappingsWriter} which outputs to the given
     * {@link PrintWriter}.
     *
     * @param outputWriter The {@link PrintWriter} to output to
     */
    protected MappingsWriter(PrintWriter outputWriter) {
        this.out = outputWriter;
    }

    /**
     * Writes the given {@link MappingContext} to this {@link MappingsWriter}'s
     * {@link PrintWriter}.
     *
     * @param mappings The {@link MappingContext} to write.
     */
    public abstract void write(MappingContext mappings);

    @Override
    public void close() throws IOException {
        out.close();
    }

}
