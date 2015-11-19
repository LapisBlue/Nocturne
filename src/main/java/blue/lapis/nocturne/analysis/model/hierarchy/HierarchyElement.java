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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an object pertaining to a hierarchy structure.
 */
public abstract class HierarchyElement implements Iterable<HierarchyNode> {

    protected Map<String, HierarchyNode> children = new HashMap<>();

    /**
     * Attempts to get the child element by the given name from the current one.
     *
     * @param name The name of the child element
     * @return The child element if it exists, or {@link Optional#empty()}
     *     otherwise
     */
    public Optional<HierarchyNode> getChild(String name) {
        if (children.containsKey(name)) {
            return Optional.of(children.get(name));
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link ImmutableSet} of all children of this
     * {@link HierarchyNode}.
     *
     * @return An {@link ImmutableSet} of all children of this
     *     {@link HierarchyNode}
     * @throws IllegalStateException If this element is
     *     {@link HierarchyNode#isTerminal()}
     */
    public ImmutableList<HierarchyNode> getChildren() throws IllegalStateException {
        return ImmutableList.copyOf(children.values());
    }

    /**
     * Adds a child {@link HierarchyNode} to the current one
     *
     * @param element The child element to add
     * @throws IllegalArgumentException If the current element is
     *     {@link HierarchyNode#isTerminal() terminal}
     * @throws IllegalStateException If an element by the given name is already
     *     a child of the current one
     */
    protected void addChild(HierarchyNode element) throws IllegalArgumentException, IllegalStateException {
        checkArgument(!getChild(element.getName()).isPresent(),
                "Element by name " + element.getName() + " already present in hierarchy");

        children.put(element.getName(), element);
    }

    @Override
    public Iterator<HierarchyNode> iterator() {
        return getChildren().iterator();
    }

}
