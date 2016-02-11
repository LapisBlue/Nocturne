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

import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a method serialized by Nocturne's class indexer.
 */
public class IndexedMethod {

    private final Signature signature;
    private final Visibility visibility;

    private final Set<String> bases = new HashSet<>();
    private final Set<String> overrides = new HashSet<>();

    public IndexedMethod(Signature signature, Visibility visibility) {
        this.signature = signature;
        this.visibility = visibility;
    }

    public Signature getSignature() {
        return signature;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Set<String> getBaseDefinitions() {
        return bases;
    }

    public Set<String> getOverrides() {
        return this.overrides;
    }

    /**
     * Represents the unique signature of a particular method.
     */
    public static class Signature {

        private final String name;
        private final MethodDescriptor descriptor;

        public Signature(String name, MethodDescriptor descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        public String getName() {
            return name;
        }

        public MethodDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public boolean equals(Object otherObj) {
            if (!(otherObj instanceof Signature)) {
                return false;
            }
            Signature sig = (Signature) otherObj;
            return sig.getName().equals(getName()) && sig.getDescriptor().equals(getDescriptor());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, descriptor);
        }

    }

    /**
     * Represents the visibility level of a particular method.
     */
    public enum Visibility {

        PACKAGE((byte) 0x00),
        PUBLIC((byte) 0x01),
        PRIVATE((byte) 0x02),
        PROTECTED((byte) 0x04);

        private static Map<Byte, Visibility> visMap;

        private final byte tag;

        Visibility(byte tag) {
            this.tag = tag;
            register();
        }

        public byte getTag() {
            return tag;
        }

        private void register() {
            if (visMap == null) {
                visMap = new HashMap<>();
            }
            visMap.put(getTag(), this);
        }

        public static Visibility fromAccessFlags(short flags) {
            return visMap.get((byte) (flags & 0b111)); // we're only interested in the last 3 bytes
        }

    }
}
