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

package blue.lapis.nocturne.processor.index.model;

import me.jamiemansfield.bombe.type.signature.MethodSignature;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a method serialized by Nocturne's class indexer.
 */
public class IndexedMethod extends Hierarchical<IndexedClass> {

    private final MethodSignature signature;
    private final Visibility visibility;

    public IndexedMethod(MethodSignature signature, Visibility visibility) {
        this.signature = signature;
        this.visibility = visibility;
    }

    public MethodSignature getSignature() {
        return signature;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Represents the visibility level of a particular method.
     */
    public enum Visibility {

        PACKAGE  ((byte) 0b000),
        PUBLIC   ((byte) 0b001),
        PRIVATE  ((byte) 0b010),
        PROTECTED((byte) 0b100);

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
