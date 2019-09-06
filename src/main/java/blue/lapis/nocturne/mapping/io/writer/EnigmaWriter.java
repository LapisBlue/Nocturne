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

package blue.lapis.nocturne.mapping.io.writer;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_PATTERN;
import static blue.lapis.nocturne.util.Constants.ENIGMA_ROOT_PACKAGE_PREFIX;

import blue.lapis.nocturne.mapping.MappingContext;
import blue.lapis.nocturne.mapping.model.ClassMapping;
import blue.lapis.nocturne.mapping.model.FieldMapping;
import blue.lapis.nocturne.mapping.model.InnerClassMapping;
import blue.lapis.nocturne.mapping.model.MethodMapping;
import blue.lapis.nocturne.mapping.model.MethodParameterMapping;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;

import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.Type;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The mappings writer, for the Enigma format.
 */
public class EnigmaWriter extends MappingsWriter {

    /**
     * Constructs a new {@link EnigmaWriter} which outputs to the given
     * {@link PrintWriter}.
     *
     * @param outputWriter The {@link PrintWriter} to output to
     */
    public EnigmaWriter(PrintWriter outputWriter) {
        super(outputWriter);
    }

    @Override
    public void write(MappingContext mappings) {
        for (TopLevelClassMapping classMapping : mappings.getMappings().values()) {
            this.writeClassMapping(classMapping, 0);
        }
        out.close();
    }

    protected void writeClassMapping(ClassMapping classMapping, int depth) {
        boolean inner = classMapping instanceof InnerClassMapping;
        if (classMapping.getDeobfuscatedName().equals(classMapping.getObfuscatedName())) {
            if (!classMapping.getInnerClassMappings().isEmpty()) {
                out.println(getIndentForDepth(depth) + "CLASS "
                        + (inner ? classMapping.getObfuscatedName() : addNonePrefix(classMapping.getObfuscatedName())));
            }
        } else {
            out.println(getIndentForDepth(depth) + "CLASS "
                    + (inner ? classMapping.getFullObfuscatedName() : addNonePrefix(classMapping.getObfuscatedName()))
                    + " "
                    + (inner ? classMapping.getDeobfuscatedName() : addNonePrefix(classMapping.getDeobfuscatedName())));
        }

        classMapping.getInnerClassMappings().values().forEach(m -> this.writeClassMapping(m, depth + 1));

        classMapping.getFieldMappings().values().stream().filter(NOT_USELESS)
                .forEach(m -> this.writeFieldMapping(m, depth + 1));

        classMapping.getMethodMappings().values().stream().filter(NOT_USELESS)
                .forEach(m -> this.writeMethodMapping(m, depth + 1));
    }

    protected void writeFieldMapping(FieldMapping fieldMapping, int depth) {
        out.println(getIndentForDepth(depth) + "FIELD " + fieldMapping.getObfuscatedName() + " "
                + fieldMapping.getDeobfuscatedName() + " "
                + addNonePrefix(fieldMapping.getObfuscatedType()).toString());
    }

    protected void writeMethodMapping(MethodMapping methodMapping, int depth) {
        if (methodMapping.getDeobfuscatedName().equals(methodMapping.getObfuscatedName())) {
            out.println(getIndentForDepth(depth) + "METHOD " + methodMapping.getObfuscatedName() + " "
                    + addNonePrefixes(methodMapping.getObfuscatedDescriptor()).toString());
        } else {
            out.println(getIndentForDepth(depth) + "METHOD " + methodMapping.getObfuscatedName() + " "
                    + methodMapping.getDeobfuscatedName() + " "
                    + addNonePrefixes(methodMapping.getObfuscatedDescriptor()).toString());
        }

        for (MethodParameterMapping methodParameterMapping : methodMapping.getParamMappings().values()) {
            writeArgumentMapping(methodParameterMapping, depth + 1);
        }
    }

    protected void writeArgumentMapping(MethodParameterMapping argMapping, int depth) {
        out.println(getIndentForDepth(depth) + "ARG " + argMapping.getIndex() + " " + argMapping.getDeobfuscatedName());
    }

    private String getIndentForDepth(int depth) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            builder.append("\t");
        }
        return builder.toString();
    }

    private String addNonePrefix(String str) {
        if (!CLASS_PATH_SEPARATOR_PATTERN.matcher(str).find()) {
            return ENIGMA_ROOT_PACKAGE_PREFIX + str;
        }
        return str;
    }

    @SuppressWarnings("unchecked")
    private <T extends Type> T addNonePrefix(T type) {
        if (type instanceof ObjectType) {
            final ObjectType obj = (ObjectType) type;
            return (T) new ObjectType(addNonePrefix(obj.getClassName()));
        }
        return type;
    }

    private MethodDescriptor addNonePrefixes(MethodDescriptor desc) {
        final List<FieldType> params = new ArrayList<>(desc.getParamTypes().size());
        for (final FieldType param : desc.getParamTypes()) {
            params.add(addNonePrefix(param));
        }
        final Type returnType = addNonePrefix(desc.getReturnType());
        return new MethodDescriptor(params, returnType);
    }

}
