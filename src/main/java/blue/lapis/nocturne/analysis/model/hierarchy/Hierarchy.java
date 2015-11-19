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
package blue.lapis.nocturne.analysis.model.hierarchy;

import blue.lapis.nocturne.util.Constants;

import java.util.Set;

/**
 * Represents a complete hierarchy.
 */
public class Hierarchy extends HierarchyElement {

    /**
     * Generates a hierarchy from the given string {@link Set}.
     *
     * @param strings The strings to generate a hierarchy from
     * @return The generated entry {@link HierarchyNode}
     */
    public static Hierarchy fromSet(Set<String> strings) {
        Hierarchy root = new Hierarchy();

        for (String str : strings) {
            String[] arr = Constants.CLASS_PATH_SEPARATOR_PATTERN.split(str);

            HierarchyElement parent = root;
            for (int i = 0; i < arr.length - 1; i++) {
                if (parent != null && parent.getChild(arr[i]).isPresent()) {
                    parent = parent.getChild(arr[i]).get();
                } else {
                    parent = new HierarchyNode(arr[i], false, parent);
                }
            }
            new HierarchyNode(arr[arr.length - 1], true, parent);
        }

        return root;
    }

}
