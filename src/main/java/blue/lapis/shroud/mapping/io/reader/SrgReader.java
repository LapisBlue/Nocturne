/*
 * Shroud
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
package blue.lapis.shroud.mapping.io.reader;

import blue.lapis.shroud.mapping.MappingSet;
import blue.lapis.shroud.mapping.model.ClassMapping;
import blue.lapis.shroud.mapping.model.FieldMapping;
import blue.lapis.shroud.mapping.model.InnerClassMapping;
import blue.lapis.shroud.mapping.model.MethodMapping;
import blue.lapis.shroud.mapping.model.TopLevelClassMapping;

import java.io.BufferedReader;
import java.util.Scanner;

/**
 * The mappings reader, for the SRG format.
 */
public class SrgReader extends MappingsReader {

    public SrgReader(BufferedReader reader) {
        super(reader);
    }

    @Override
    public MappingSet read() {
        MappingSet mappings = new MappingSet();

        Scanner scanner = new Scanner(this.reader);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] split = line.substring(4).split(" ");

            if (line.startsWith("CL: ")) {
                mappings.addMapping(this.readClassMapping(mappings, split[0], split[1]));
            } else if (line.startsWith("FD: ")) {
                this.readFieldMapping(mappings, split[0], split[1]);
            } else if (line.startsWith("MD: ")) {
                this.readMethodMapping(mappings, split[0], split[1], split[2], split[3]);
            }
        }

        return mappings;
    }

    @Override
    protected TopLevelClassMapping readClassMapping(MappingSet mappings, String obf, String deobf) {
        String[] obfClassSplit = obf.split(InnerClassMapping.INNER_CLASS_SEPARATOR_CHAR + "");
        String[] deobfClassSplit = deobf.split(InnerClassMapping.INNER_CLASS_SEPARATOR_CHAR + "");

        TopLevelClassMapping topLevelClassMapping =
                new TopLevelClassMapping(mappings, obfClassSplit[0], deobfClassSplit[0]);

        // TODO: read inner classes

        return topLevelClassMapping;
    }

    @Override
    protected ClassMapping getClassMapping(MappingSet mappings, String obf, String deobf) {
        String[] obfClassSplit = obf.split(InnerClassMapping.INNER_CLASS_SEPARATOR_CHAR + "");

        TopLevelClassMapping topLevelClassMapping = mappings.getMappings().get(obfClassSplit[0]);

        // TODO: the rest

        return null;
    }

    @Override
    protected FieldMapping readFieldMapping(MappingSet mappings, String obf, String deobf) {
        return null;
    }

    @Override
    protected MethodMapping readMethodMapping(MappingSet mappings, String obf, String obfSignature, String deobf,
            String deobfSignature) {
        return null;
    }
}
