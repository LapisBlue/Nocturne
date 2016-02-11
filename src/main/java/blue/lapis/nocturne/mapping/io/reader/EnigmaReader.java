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

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.util.helper.MappingsHelper;

import java.io.BufferedReader;
import java.util.stream.Collectors;

/**
 * The mappings reader for the Enigma format.
 */
public class EnigmaReader extends MappingsReader {

    public EnigmaReader(BufferedReader reader) {
        super(reader);
    }

    @Override
    public MappingContext read() {
        MappingContext mappings = new MappingContext();

        String currentClass = null;
        int lineNum = 0;
        for (String line : reader.lines().collect(Collectors.toList())) {
            lineNum++;
            String[] arr = line.trim().split(" ");
            if (arr.length == 0) {
                continue;
            }
            switch (arr[0]) {
                case "CLASS": {
                    if (arr.length < 2 || arr.length > 3) {
                        System.err.println("Cannot parse file: malformed class mapping on line " + lineNum);
                        System.exit(1);
                    }

                    String obf = arr[1].replace("none/", "");
                    String deobf = arr.length == 3 ? arr[2] : obf;
                    MappingsHelper.genClassMapping(Main.getMappingContext(), obf, deobf); // TODO: Handle inner-classes
                    currentClass = obf;
                    break;
                }
                case "FIELD": {
                    if (arr.length != 4) {
                        System.err.println("Cannot parse file: malformed field mapping on line " + lineNum);
                        System.exit(1);
                    }

                    if (currentClass == null) {
                        throw new IllegalArgumentException("Cannot parse file: found field mapping before initial "
                                + "class mapping on line " + lineNum);
                    }

                    String obf = arr[1];
                    String deobf = arr[2];
                    String type = arr[3];
                    MappingsHelper.genFieldMapping(Main.getMappingContext(), currentClass, obf, deobf); // TODO: type
                    break;
                }
                case "METHOD": {
                    if (arr.length == 3) {
                        continue;
                    }

                    if (arr.length != 4) {
                        throw new IllegalArgumentException("Cannot parse file: malformed method mapping on line "
                                + lineNum);
                    }

                    if (currentClass == null) {
                        throw new IllegalArgumentException("Cannot parse file: found method mapping before initial "
                                + "class mapping on line " + lineNum);
                    }

                    String obf = arr[1];
                    String deobf = arr[2];
                    String sig = arr[3];
                    MappingsHelper.genMethodMapping(Main.getMappingContext(), currentClass, obf, deobf, sig);
                    break;
                }
                case "ARG": {
                    break;
                }
                default: {
                    Main.getLogger().warning("Unrecognized mapping on line " + lineNum);
                }
            }
        }

        return mappings;
    }
}
