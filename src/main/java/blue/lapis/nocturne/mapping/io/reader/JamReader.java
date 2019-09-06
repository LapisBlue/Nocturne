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

package blue.lapis.nocturne.mapping.io.reader;

import static blue.lapis.nocturne.util.Constants.SPACE_PATTERN;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.processor.index.model.signature.FieldSignature;
import blue.lapis.nocturne.processor.index.model.signature.MethodSignature;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The mappings reader, for the SRG format.
 */
public class JamReader extends MappingsReader {

    private static final String CLASS_MAPPING_KEY = "CL";
    private static final String FIELD_MAPPING_KEY = "FD";
    private static final String METHOD_MAPPING_KEY = "MD";
    private static final String PARAM_MAPPING_KEY = "MP";

    private static final int CLASS_MAPPING_ELEMENT_COUNT = 3;
    private static final int FIELD_MAPPING_ELEMENT_COUNT = 5;
    private static final int METHOD_MAPPING_ELEMENT_COUNT = 5;
    private static final int PARAM_MAPPING_ELEMENT_COUNT = 7;

    public JamReader(BufferedReader reader) {
        super(reader);
    }

    @Override
    public MappingContext read() {
        MappingContext mappings = new MappingContext();

        Pattern spacePattern = Pattern.compile(" ", Pattern.LITERAL);
        List<String> rawClassMappings = new ArrayList<>();
        List<String> rawFieldMappings = new ArrayList<>();
        List<String> rawMethodMappings = new ArrayList<>();
        List<String> rawParamMappings = new ArrayList<>();

        for (String line : reader.lines().collect(Collectors.toList())) {
            String trim = line.trim();
            if (trim.charAt(0) == '#' || trim.isEmpty()) {
                continue;
            }

            if (line.length() < 4) {
                Main.getLogger().warning("Found bogus line in mappings file - ignoring");
                continue;
            }

            int len = spacePattern.split(line).length;

            String key = line.substring(0, 2);
            if (key.equals(CLASS_MAPPING_KEY) && len == CLASS_MAPPING_ELEMENT_COUNT) {
                rawClassMappings.add(line);
            } else if (key.equals(FIELD_MAPPING_KEY) && len == FIELD_MAPPING_ELEMENT_COUNT) {
                rawFieldMappings.add(line);
            } else if (key.equals(METHOD_MAPPING_KEY) && len == METHOD_MAPPING_ELEMENT_COUNT) {
                rawMethodMappings.add(line);
            } else if (key.equals(PARAM_MAPPING_KEY) && len == PARAM_MAPPING_ELEMENT_COUNT) {
                rawParamMappings.add(line);
            } else {
                Main.getLogger().warning("Discovered unrecognized key \"" + key + "\" in mappings file - ignoring");
            }
        }

        // we need to sort the class mappings in order of ascending nesting level
        rawClassMappings.sort((s1, s2) -> getClassNestingLevel(s1) - getClassNestingLevel(s2));

        genClassMappings(mappings, rawClassMappings);
        genFieldMappings(mappings, rawFieldMappings);
        genMethodMappings(mappings, rawMethodMappings);
        genMethodParamMappings(mappings, rawParamMappings);

        return mappings;
    }

    private void genClassMappings(MappingContext context, List<String> classMappings) {
        for (String mapping : classMappings) {
            String[] arr = SPACE_PATTERN.split(mapping);
            String obf = arr[1];
            String deobf = arr[2];
            MappingsHelper.genClassMapping(context, obf, deobf, false);
        }
    }

    private void genFieldMappings(MappingContext context, List<String> fieldMappings) {
        for (String mapping : fieldMappings) {
            String[] arr = SPACE_PATTERN.split(mapping);
            String owningClass = arr[1];
            String obf = arr[2];
            String desc = arr[3];
            String deobf = arr[4];
            MappingsHelper.genFieldMapping(context, owningClass, new FieldSignature(obf, Type.fromString(desc)), deobf);
        }
    }

    private void genMethodMappings(MappingContext context, List<String> methodMappings) {
        for (String mapping : methodMappings) {
            String[] arr = SPACE_PATTERN.split(mapping);
            String owningClass = arr[1];
            String obf = arr[2];
            String desc = arr[3];
            String deobf = arr[4];
            MappingsHelper.genMethodMapping(context, owningClass,
                    new MethodSignature(obf, MethodDescriptor.fromString(desc)), deobf, false);
        }
    }

    private void genMethodParamMappings(MappingContext context, List<String> paramMappings) {
        for (String mapping : paramMappings) {
            String[] arr = SPACE_PATTERN.split(mapping);
            String owningClass = arr[1];
            String owningMethod = arr[2];
            String owningMethodDesc = arr[3]; //TODO: *stretching collar* oooooh...
            Optional<ClassMapping> classMapping = MappingsHelper.getClassMapping(context, owningClass);
            if (!classMapping.isPresent()) {
                Main.getLogger().warning("Discovered orphaned method parameter mapping (class) - ignoring");
                continue;
            }
            MethodMapping methodMapping = classMapping.get().getMethodMappings()
                    .get(new MethodSignature(owningMethod, MethodDescriptor.fromString(owningMethodDesc)));
            if (methodMapping == null) {
                methodMapping = new MethodMapping(classMapping.get(),
                        new MethodSignature(owningMethod, MethodDescriptor.fromString(owningMethodDesc)), owningMethod,
                        false);
            }
            int index;
            try {
                index = Integer.parseInt(arr[4]);
            } catch (NumberFormatException ex) {
                Main.getLogger().warning("Discovered invalid method parameter mapping (index) - ignoring");
                continue;
            }

            String deobf;
            if (arr.length == 7) {
                deobf = arr[6];
            } else {
                deobf = arr[5];
            }

            MappingsHelper.genParamMapping(context, methodMapping, index, deobf);
        }
    }

}
