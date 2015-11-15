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

import blue.lapis.nocturne.mapping.MappingContext;

import java.io.BufferedReader;
import java.util.List;
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

        List<String> mappingList = reader.lines().collect(Collectors.toList());

        List<String> rawClassMappings = mappingList.stream()
                .filter(s -> s.startsWith(CLASS_MAPPING_KEY) && s.split(" ").length == CLASS_MAPPING_ELEMENT_COUNT)
                .sorted((s1, s2) -> getClassNestingLevel(s1) - getClassNestingLevel(s2))
                .collect(Collectors.toList());
        List<String> rawFieldMappings = mappingList.stream()
                .filter(s -> s.startsWith(FIELD_MAPPING_KEY) && s.split(" ").length == FIELD_MAPPING_ELEMENT_COUNT)
                .collect(Collectors.toList());
        List<String> rawMethodMappings = mappingList.stream()
                .filter(s -> s.startsWith(METHOD_MAPPING_KEY) && s.split(" ").length == METHOD_MAPPING_ELEMENT_COUNT)
                .collect(Collectors.toList());

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
            genClassMapping(context, obf, deobf);
        }
    }

    private void genFieldMappings(MappingContext context, List<String> fieldMappings) {
        for (String mapping : fieldMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String deobf = arr[2];
            genFieldMapping(context, obf, deobf);
        }
    }

    private void genMethodMappings(MappingContext context, List<String> methodMappings) {
        for (String mapping : methodMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String obfSig = arr[2];
            String deobf = arr[3];
            String deobfSig = arr[4];
            genMethodMapping(context, obf, obfSig, deobf, deobfSig);
        }
    }

}
