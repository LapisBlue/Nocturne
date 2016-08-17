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

package blue.lapis.nocturne.mapping.io.reader;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The mappings reader, for the SRG format.
 */
public class SrgReader extends MappingsReader {

    private static final String CLASS_MAPPING_KEY = "CL: ";
    private static final String FIELD_MAPPING_KEY = "FD: ";
    private static final String METHOD_MAPPING_KEY = "MD: ";

    private static final int CLASS_MAPPING_ELEMENT_COUNT = 3;
    private static final int FIELD_MAPPING_ELEMENT_COUNT = 3;
    private static final int METHOD_MAPPING_ELEMENT_COUNT = 5;

    public SrgReader(BufferedReader reader) {
        super(reader);
    }

    @Override
    public MappingContext read() {
        MappingContext mappings = new MappingContext();

        Pattern spacePattern = Pattern.compile(" ", Pattern.LITERAL);
        List<String> rawClassMappings = new ArrayList<>();
        List<String> rawFieldMappings = new ArrayList<>();
        List<String> rawMethodMappings = new ArrayList<>();

        for (String line : reader.lines().collect(Collectors.toList())) {
            int len = spacePattern.split(line).length;
            if (line.startsWith(CLASS_MAPPING_KEY) && len == CLASS_MAPPING_ELEMENT_COUNT) {
                rawClassMappings.add(line);
            } else if (line.startsWith(FIELD_MAPPING_KEY) && len == FIELD_MAPPING_ELEMENT_COUNT) {
                rawFieldMappings.add(line);
            } else if (line.startsWith(METHOD_MAPPING_KEY) && len == METHOD_MAPPING_ELEMENT_COUNT) {
                rawMethodMappings.add(line);
            }
        }

        // we need to sort the class mappings in order of ascending nesting level
        rawClassMappings.sort((s1, s2) -> getClassNestingLevel(s1) - getClassNestingLevel(s2));

        genClassMappings(mappings, rawClassMappings);
        genFieldMappings(mappings, rawFieldMappings);
        genMethodMappings(mappings, rawMethodMappings);

        return mappings;
    }

    private void genClassMappings(MappingContext context, List<String> classMappings) {
        for (String mapping : classMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String deobf = arr[2];
            MappingsHelper.genClassMapping(context, obf, deobf, false);
        }
    }

    private void genFieldMappings(MappingContext context, List<String> fieldMappings) {
        for (String mapping : fieldMappings) {
            String[] arr = mapping.split(" ");
            int lastIndex = arr[1].lastIndexOf(CLASS_PATH_SEPARATOR_CHAR);
            String owningClass = arr[1].substring(0, lastIndex);
            String obf = arr[1].substring(lastIndex + 1);
            String deobf = arr[2].substring(arr[2].lastIndexOf(CLASS_PATH_SEPARATOR_CHAR) + 1);
            // SRG doesn't support field types so we just pass a null type arg and let the helper method figure it out
            MappingsHelper.genFieldMapping(context, owningClass, obf, deobf, null);
        }
    }

    private void genMethodMappings(MappingContext context, List<String> methodMappings) {
        for (String mapping : methodMappings) {
            String[] arr = mapping.split(" ");
            int lastIndex = arr[1].lastIndexOf(CLASS_PATH_SEPARATOR_CHAR);
            String owningClass = arr[1].substring(0, lastIndex);
            String obf = arr[1].substring(lastIndex + 1);
            String descriptor = arr[2];
            String deobf = arr[3].substring(arr[3].lastIndexOf(CLASS_PATH_SEPARATOR_CHAR) + 1);
            MappingsHelper.genMethodMapping(context, owningClass, obf, deobf, descriptor, false);
        }
    }

}
