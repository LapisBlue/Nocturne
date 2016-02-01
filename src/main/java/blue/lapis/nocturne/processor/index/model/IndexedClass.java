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
package blue.lapis.nocturne.processor.index.model;

import blue.lapis.nocturne.processor.constantpool.model.ImmutableConstantPool;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A summary of select info from a processed class file.
 */
public class IndexedClass {

    private final String name;
    private final ImmutableConstantPool constantPool;
    private final String superClass;
    private final ImmutableList<String> interfaces;
    private final ImmutableList<MethodSignature> methods;

    public IndexedClass(String name, ImmutableConstantPool constantPool, String superClass, List<String> interfaces,
            List<MethodSignature> methods) {
        this.name = name;
        this.constantPool = constantPool;
        this.superClass = superClass;
        this.interfaces = ImmutableList.copyOf(interfaces);
        this.methods = ImmutableList.copyOf(methods);
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

    public ImmutableList<MethodSignature> getMethods() {
        return methods;
    }

}
