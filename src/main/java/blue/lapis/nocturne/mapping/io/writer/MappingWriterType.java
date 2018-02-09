/*
 * Nocturne
 * Copyright (c) 2015-2016, Lapis <https://github.com/LapisBlue>
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

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.io.MappingFormatType;
import com.google.common.collect.Maps;
import javafx.stage.FileChooser;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public enum MappingWriterType {

    SRG(MappingFormatType.SRG, SrgWriter.class),
    JAM(MappingFormatType.JAM, JamWriter.class),
    ENIGMA(MappingFormatType.ENIGMA, EnigmaWriter.class);

    private final MappingFormatType type;

    private static final Map<FileChooser.ExtensionFilter, MappingWriterType> filterToType = Maps.newHashMap();
    private static final Map<MappingFormatType, MappingWriterType> formatToType = Maps.newHashMap();

    static {
        Arrays.asList(values()).forEach(t -> filterToType.put(t.getExtensionFilter(), t));
        Arrays.asList(values()).forEach(t -> formatToType.put(t.getFormatType(), t));
    }

    private final FileChooser.ExtensionFilter extensionFilter;
    private final Constructor<? extends MappingsWriter> writerCtor;

    MappingWriterType(MappingFormatType mappingType, Class<? extends MappingsWriter> readerClass) {
        this.type = mappingType;
        this.extensionFilter = new FileChooser.ExtensionFilter(Main.getResourceBundle()
                .getString("filechooser.type_" + mappingType.name().toLowerCase()),
                "*." + mappingType.getFileExtension());
        try {
            this.writerCtor = readerClass.getConstructor(PrintWriter.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Failed to initialize reader type for class " + readerClass.getName(), ex);
        }
    }

    public MappingFormatType getFormatType() {
        return this.type;
    }

    public FileChooser.ExtensionFilter getExtensionFilter() {
        return this.extensionFilter;
    }

    public MappingsWriter constructWriter(PrintWriter writer) {
        try {
            return writerCtor.newInstance(writer);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to construct writer with class "
                    + writerCtor.getDeclaringClass().getName(), ex);
        }
    }

    public static MappingWriterType fromExtensionFilter(FileChooser.ExtensionFilter filter) {
        return filterToType.get(filter);
    }

    public static MappingWriterType fromFormatType(final MappingFormatType formatType) {
        return formatToType.get(formatType);
    }
}
