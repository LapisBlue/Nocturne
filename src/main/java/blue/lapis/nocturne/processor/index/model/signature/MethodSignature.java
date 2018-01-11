/*
 * Nocturne
 * Copyright (c) 2015-2018, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.processor.index.model.signature;

import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;

import java.util.Objects;

/**
 * Represents the unique signature of a particular method.
 */
public class MethodSignature extends MemberSignature {

    protected final MethodDescriptor descriptor;

    public MethodSignature(String name, MethodDescriptor descriptor) {
        super(name);
        this.descriptor = descriptor;
    }

    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (!(otherObj instanceof MethodSignature)) {
            return false;
        }
        MethodSignature sig = (MethodSignature) otherObj;
        return sig.getName().equals(getName()) && sig.getDescriptor().equals(getDescriptor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor);
    }

}
