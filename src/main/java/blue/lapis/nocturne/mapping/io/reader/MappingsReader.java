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
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
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

    protected int getClassNestingLevel(String name) {
        return name.split(" ")[1].length()
                - name.split(" ")[1].replace(INNER_CLASS_SEPARATOR_CHAR + "", "").length();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
