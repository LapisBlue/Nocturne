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

import blue.lapis.nocturne.mapping.MappingSet;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import java.io.BufferedReader;
import java.util.List;
import java.util.regex.Matcher;
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
    public MappingSet read() {
        MappingSet mappings = new MappingSet();

        List<String> rawClassMappings = reader.lines()
                .filter(s -> s.startsWith(CLASS_MAPPING_KEY) && s.split(" ").length == CLASS_MAPPING_ELEMENT_COUNT)
                .sorted((s1, s2) -> getClassNestingLevel(s1) - getClassNestingLevel(s2))
                .collect(Collectors.toList());
        List<String> rawFieldMappings = reader.lines()
                .filter(s -> s.startsWith(FIELD_MAPPING_KEY) && s.split(" ").length == FIELD_MAPPING_ELEMENT_COUNT)
                .collect(Collectors.toList());
        List<String> rawMethodMappings = reader.lines()
                .filter(s -> s.startsWith(METHOD_MAPPING_KEY) && s.split(" ").length == METHOD_MAPPING_ELEMENT_COUNT)
                .collect(Collectors.toList());

        genClassMappings(mappings, rawClassMappings);
        genFieldMappings(mappings, rawFieldMappings);
        genMethodMappings(mappings, rawMethodMappings);

        return mappings;
    }

    @Override
    protected void genClassMapping(MappingSet mappingSet, String obf, String deobf) {
        if (obf.contains(InnerClassMapping.INNER_CLASS_SEPARATOR_CHAR + "")) {
            // escape the separator char so it doesn't get parsed as regex
            String[] obfSplit = InnerClassMapping.INNER_CLASS_SEPARATOR_PATTERN.split(obf);
            String[] deobfSplit = InnerClassMapping.INNER_CLASS_SEPARATOR_PATTERN.split(deobf);
            if (obfSplit.length != deobfSplit.length) { // non-inner mapped to inner or vice versa
                System.err.println("Unsupported mapping: " + obf + " <-> " + deobf);
                return; // ignore it
            }

            // iteratively get the direct parent class to this inner class
            ClassMapping parent = mappingSet.getMappings().get(obfSplit[0]);
            for (int i = 1; i < obfSplit.length - 1; i++) {
                if (parent == null) {
                    break;
                }
                parent = parent.getInnerClassMappings().get(obfSplit[i]);
            }

            if (parent == null) {
                //TODO: this is a very valid case and needs to be addressed
                // My thought is that we'll create a dummy mapping (where obf == deobf)
                // for any referenced classes that don't have a specific mapping.
                // ~ caseif
                System.err.println("Failed to load mapping for obfuscated class " + obf
                        + ": loading inner class mappings without a mapped parent is not supported at this time");
                return;
            }

            new InnerClassMapping(parent, obfSplit[obfSplit.length - 1],
                    deobfSplit[deobfSplit.length - 1]);
        } else {
            mappingSet.addMapping(new TopLevelClassMapping(mappingSet, obf, deobf));
        }
    }

    @Override
    protected void genFieldMapping(MappingSet mappingSet, String obf, String deobf) {
        //TODO
    }

    @Override
    protected void genMethodMapping(MappingSet mappingSet, String obf, String obfSig, String deobf, String deobfSig) {
        //TODO
    }

    private void genClassMappings(MappingSet mappingSet, List<String> classMappings) {
        for (String mapping : classMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String deobf = arr[2];
            genClassMapping(mappingSet, obf, deobf);
        }
    }

    private void genFieldMappings(MappingSet mappingSet, List<String> fieldMappings) {
        for (String mapping : fieldMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String deobf = arr[2];
            genFieldMapping(mappingSet, obf, deobf);
        }
    }

    private void genMethodMappings(MappingSet mappingSet, List<String> methodMappings) {
        for (String mapping : methodMappings) {
            String[] arr = mapping.split(" ");
            String obf = arr[1];
            String obfSig = arr[2];
            String deobf = arr[3];
            String deobfSig = arr[4];
            genMethodMapping(mappingSet, obf, obfSig, deobf, deobfSig);
        }
    }

}
