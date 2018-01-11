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

package blue.lapis.nocturne.mapping;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.io.reader.EnigmaReader;
import blue.lapis.nocturne.mapping.io.reader.JamReader;
import blue.lapis.nocturne.mapping.io.reader.MappingsReader;
import blue.lapis.nocturne.mapping.io.reader.SrgReader;
import blue.lapis.nocturne.mapping.io.writer.EnigmaWriter;
import blue.lapis.nocturne.mapping.io.writer.JamWriter;
import blue.lapis.nocturne.mapping.io.writer.MappingsWriter;
import blue.lapis.nocturne.mapping.io.writer.SrgWriter;
import com.google.common.collect.Maps;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * An enum used to represent the mapping formats supported by Nocturne.
 */
public enum MappingFormat {

    SRG("srg", SrgReader::new, SrgWriter::new),
    JAM("jam", JamReader::new, JamWriter::new),
    ENIGMA("*", EnigmaReader::new, EnigmaWriter::new),
    ;

    private static final Map<FileChooser.ExtensionFilter, MappingFormat> FILTER_TO_FORMAT = Maps.newHashMap();

    static {
        Arrays.asList(values()).forEach(t -> FILTER_TO_FORMAT.put(t.getExtensionFilter(), t));
    }

    /**
     * Gets the {@link MappingFormat} from a given extension filter.
     *
     * @param filter The extension filter
     * @return The mapping format, wrapped in a {@link Optional}
     */
    public static Optional<MappingFormat> fromExtensionFilter(final FileChooser.ExtensionFilter filter) {
        return Optional.ofNullable(FILTER_TO_FORMAT.get(filter));
    }

    private final FileChooser.ExtensionFilter extensionFilter;
    private final Function<BufferedReader, MappingsReader> parserConstructor;
    private final Function<PrintWriter, MappingsWriter> writerConstructor;

    /**
     * Creates a new mapping format, given the constructor of it's parser, and writer.
     *
     * @param extension The extension used by the format
     * @param parserConstructor The constructor of the mapping parser
     * @param writerConstructor The constructor of the mapping writer
     */
    MappingFormat(final String extension,
            final Function<BufferedReader, MappingsReader> parserConstructor,
            final Function<PrintWriter, MappingsWriter> writerConstructor) {
        this.extensionFilter = new FileChooser.ExtensionFilter(
                Main.getResourceBundle().getString("filechooser.type_" + name().toLowerCase()),
                "*." + extension);
        this.parserConstructor = parserConstructor;
        this.writerConstructor = writerConstructor;
    }

    /**
     * Gets the extension filter used by the mapping format.
     *
     * @return The extension filter
     */
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return this.extensionFilter;
    }

    /**
     * Creates a {@link MappingsReader} of the correct type for the format
     * in use.
     *
     * @param reader The reader to read from
     * @return The mappings parser
     */
    public MappingsReader createParser(final BufferedReader reader) {
        return this.parserConstructor.apply(reader);
    }

    /**
     * Creates a new {@link MappingsWriter} of the correct type for the
     * format in use.
     *
     * @param writer The writer to write to
     * @return The mappings writer
     */
    public MappingsWriter createWriter(final PrintWriter writer) {
        return this.writerConstructor.apply(writer);
    }

}
