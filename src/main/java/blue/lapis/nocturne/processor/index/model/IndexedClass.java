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

package blue.lapis.nocturne.processor.index.model;

import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;

import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A summary of select info from a processed class file.
 */
public class IndexedClass extends Hierarchical<IndexedClass> {

    public static final Map<String, IndexedClass> INDEXED_CLASSES = new HashMap<>();

    private final String name;
    private ImmutableConstantPool constantPool;
    private final String superClass;
    private final List<String> interfaces;
    private final Map<FieldSignature, IndexedField> fields;
    private final Map<MethodSignature, IndexedMethod> methods;

    public IndexedClass(String name, ImmutableConstantPool constantPool, String superClass, List<String> interfaces,
                        List<IndexedField> fields, List<IndexedMethod> methods) {
        this.name = name;
        this.constantPool = constantPool;
        this.superClass = superClass;
        this.interfaces = Collections.unmodifiableList(interfaces);
        this.fields = Collections.unmodifiableMap(
                fields.stream().collect(Collectors.toMap(IndexedField::getSignature, f -> f)));
        this.methods = Collections.unmodifiableMap(
                methods.stream().collect(Collectors.toMap(IndexedMethod::getSignature, m -> m))
        );
    }

    public String getName() {
        return name;
    }

    public ImmutableConstantPool getConstantPool() {
        return constantPool;
    }

    public String getSuperclass() {
        return superClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public Map<FieldSignature, IndexedField> getFields() {
        return fields;
    }

    public Map<MethodSignature, IndexedMethod> getMethods() {
        return methods;
    }

    public void clearPool() {
        this.constantPool = null;
    }

}
