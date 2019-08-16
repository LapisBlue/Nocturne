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

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_PATTERN;
import static blue.lapis.nocturne.util.Constants.ENIGMA_ROOT_PACKAGE_PREFIX;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;

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
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * The mappings reader for the Enigma format.
 */
public class EnigmaReader extends MappingsReader {

    private static final String CLASS_MAPPING_KEY = "CLASS";
    private static final String FIELD_MAPPING_KEY = "FIELD";
    private static final String METHOD_MAPPING_KEY = "METHOD";
    private static final String ARG_MAPPING_KEY = "ARG";

    public EnigmaReader(BufferedReader reader) {
        super(reader);
    }

    @Override
    public MappingContext read() {
        MappingContext mappings = new MappingContext();

        Stack<ClassMapping> classStack = new Stack<>();
        MethodMapping currentMethod = null;
        int lineNum = 0;
        int lastIndentLevel = -1;

        for (String line : reader.lines().collect(Collectors.toList())) {
            lineNum++;

            // Remove comments
            final int commentPos = line.indexOf('#');
            if (commentPos >= 0) {
                line = line.substring(0, commentPos);
            }

            final String[] arr = line.trim().split(" ");

            // Skip empty lines
            if (arr.length == 0) {
                continue;
            }

            // The indentation level of the line
            int indentLevel = 0;
            for (int i = 0; i < line.length(); i++) {
                // Check if the char is a tab
                if (line.charAt(i) != '\t') {
                    break;
                }
                indentLevel++;
            }

            if (lastIndentLevel != -1 && indentLevel < lastIndentLevel && !classStack.empty()) {
                classStack.pop();
            }

            switch (arr[0]) {
                case CLASS_MAPPING_KEY: {
                    if (arr.length < 2 || arr.length > 3) {
                        throw new IllegalArgumentException("Cannot parse file: malformed class mapping on line "
                                + lineNum);
                    }

                    String obf = removeNonePrefix(arr[1]);
                    String deobf = arr.length == 3 ? removeNonePrefix(arr[2]) : obf;

                    if (lastIndentLevel != -1 && indentLevel > lastIndentLevel) {
                        deobf = classStack.peek().getFullDeobfuscatedName() + INNER_CLASS_SEPARATOR_CHAR + deobf;
                    }
                    classStack.push(MappingsHelper.genClassMapping(mappings, obf, deobf, false));
                    currentMethod = null;
                    break;
                }
                case FIELD_MAPPING_KEY: {
                    if (classStack.empty() || classStack.peek() == null) {
                        continue;
                    }

                    if (arr.length != 4) {
                        throw new IllegalArgumentException("Cannot parse file: malformed field mapping on line "
                                + lineNum);
                    }

                    if (classStack.isEmpty()) {
                        throw new IllegalArgumentException("Cannot parse file: found field mapping before initial "
                                + "class mapping on line " + lineNum);
                    }

                    String obf = arr[1];
                    String deobf = arr[2];
                    Type type = removeNonePrefix(Type.fromString(arr[3]));
                    MappingsHelper.genFieldMapping(mappings, classStack.peek().getFullObfuscatedName(),
                            new FieldSignature(obf, type), deobf);
                    currentMethod = null;
                    break;
                }
                case METHOD_MAPPING_KEY: {
                    if (classStack.isEmpty() || classStack.peek() == null) {
                        continue;
                    }

                    if (classStack.isEmpty()) {
                        throw new IllegalArgumentException("Cannot parse file: found method mapping before initial "
                                + "class mapping on line " + lineNum);
                    }

                    String obf = arr[1];
                    String deobf;
                    String descStr;
                    if (arr.length == 3) {
                        deobf = obf;
                        descStr = arr[2];
                    } else if (arr.length == 4) {
                        deobf = arr[2];
                        descStr = arr[3];
                    } else {
                        throw new IllegalArgumentException("Cannot parse file: malformed method mapping on line "
                                + lineNum);
                    }

                    MethodDescriptor desc = removeNonePrefixes(MethodDescriptor.fromString(descStr));

                    currentMethod = MappingsHelper.genMethodMapping(mappings, classStack.peek().getFullObfuscatedName(),
                            new MethodSignature(obf, desc), deobf, true);
                    break;
                }
                case ARG_MAPPING_KEY: {
                    if (classStack.empty() || classStack.peek() == null) {
                        continue;
                    }

                    if (arr.length != 3) {
                        throw new IllegalArgumentException("Cannot parse file: malformed argument mapping on line "
                                + lineNum);
                    }

                    if (currentMethod == null) {
                        throw new IllegalArgumentException("Cannot parse file: found argument mapping before initial "
                                + "method mapping on line " + lineNum);
                    }

                    int index = Integer.parseInt(arr[1]);
                    String deobf = arr[2];

                    MappingsHelper.genArgumentMapping(mappings, currentMethod, index, deobf);
                    break;
                }
                default: {
                    Main.getLogger().warning("Unrecognized mapping on line " + lineNum);
                }
            }
            lastIndentLevel = indentLevel;
        }

        return mappings;
    }

    private String removeNonePrefix(String str) {
        if (str.length() < 6) {
            return str;
        }
        String substr = str.substring(5);
        if (str.startsWith(ENIGMA_ROOT_PACKAGE_PREFIX)
                && !CLASS_PATH_SEPARATOR_PATTERN.matcher(str.substring(5)).find()) {
            return substr;
        }
        return str;
    }

    private Type removeNonePrefix(Type type) {
        return type.isPrimitive() ? type : Type.fromString("L" + removeNonePrefix(type.getClassName()) + ";");
    }

    private MethodDescriptor removeNonePrefixes(MethodDescriptor desc) {
        Type[] params = new Type[desc.getParamTypes().length];
        for (int i = 0; i < params.length; i++) {
            params[i] = removeNonePrefix(desc.getParamTypes()[i]);
        }
        Type returnType = removeNonePrefix(desc.getReturnType());
        return new MethodDescriptor(returnType, params);
    }

}
