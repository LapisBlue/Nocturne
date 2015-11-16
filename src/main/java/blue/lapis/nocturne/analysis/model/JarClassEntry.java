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
package blue.lapis.nocturne.analysis.model;

import java.util.Arrays;

/**
 * Represents an class entry within a JAR file.
 */
//TODO: document (caseif)
public class JarClassEntry {

    private String name;
    private byte[] content;

    public JarClassEntry(String name, byte[] content) {
        this.name = name;
        this.content = new byte[content.length];
        System.arraycopy(content, 0, this.content, 0, content.length);
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isObfuscated() {
        return true; //TODO
    }

    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof JarClassEntry)) {
            return false;
        }
        JarClassEntry other = (JarClassEntry) otherObject;
        return other.getName().equals(getName())
                && Arrays.hashCode(other.getContent()) == Arrays.hashCode(getContent());
    }

}
