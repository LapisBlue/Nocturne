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

package blue.lapis.nocturne.processor.index.model;

import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
    private final ImmutableList<String> interfaces;
    private final ImmutableSet<String> fields; //TODO: use full signature instead of just name
    private final ImmutableMap<IndexedMethod.Signature, IndexedMethod> methods;

    public IndexedClass(String name, ImmutableConstantPool constantPool, String superClass, List<String> interfaces,
            List<String> fields, List<IndexedMethod> methods) {
        this.name = name;
        this.constantPool = constantPool;
        this.superClass = superClass;
        this.interfaces = ImmutableList.copyOf(interfaces);
        this.fields = ImmutableSet.copyOf(fields);
        this.methods = ImmutableMap.copyOf(
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

    public ImmutableList<String> getInterfaces() {
        return interfaces;
    }

    public ImmutableSet<String> getFields() {
        return fields;
    }

    public ImmutableMap<IndexedMethod.Signature, IndexedMethod> getMethods() {
        return methods;
    }

    public void clearPool() {
        this.constantPool = null;
    }

}
