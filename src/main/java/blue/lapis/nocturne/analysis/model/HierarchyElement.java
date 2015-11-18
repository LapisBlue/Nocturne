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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import blue.lapis.nocturne.util.Constants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an item in a hierarchy (either a package or a class).
 */
public class HierarchyElement {

    private String name;
    private boolean terminal;

    private HierarchyElement parent;
    private Map<String, HierarchyElement> children;

    /**
     * Constructs a new {@link HierarchyElement} with the given parameters.
     *
     * @param name The name of the {@link HierarchyElement}
     * @param terminal Whether this element is terminal (terminal elements
     *     may not contain children)
     * @param parent The {@link HierarchyElement} parenting the new one
     */
    public HierarchyElement(String name, boolean terminal, HierarchyElement parent) {
        this.name = name;
        this.terminal = terminal;

        if (!isTerminal()) {
            children = new HashMap<>();
        }

        if (parent != null) {
            checkArgument(!parent.isTerminal(), "Parent element must not be terminal");
            this.parent = parent;
            parent.addChild(this);
        }
    }

    /**
     * Constructs a new entry {@link HierarchyElement} with the given
     * parameters.
     *
     * @param name The name of the {@link HierarchyElement}
     * @param terminal Whether this element is terminal (terminal elements
     *     may not contain children)
     */
    public HierarchyElement(String name, boolean terminal) {
        this(name, terminal, null);
    }

    /**
     * Returns the name of this {@link HierarchyElement}.
     *
     * @return The name of this {@link HierarchyElement}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this {@link HierarchyElement} is terminal. A terminal
     * element marks the end of its respective branch, and may not be assigned
     * children.
     *
     * @return Whether this {@link HierarchyElement} is terminal
     */
    // in other words, it's kind of a loser
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Adds a child {@link HierarchyElement} to the current one
     *
     * @param element The child element to add
     * @throws IllegalArgumentException If the current element is
     *     {@link HierarchyElement#isTerminal() terminal}
     * @throws IllegalStateException If an element by the given name is already
     *     a child of the current one
     */
    public void addChild(HierarchyElement element) throws IllegalArgumentException, IllegalStateException {
        checkState(!isTerminal(), "addChild called on terminal HierarchyElement");
        checkArgument(!getChild(element.getName()).isPresent(),
                "Element by name " + name + " already present in hierarchy");

        children.put(element.getName(), element);
    }

    /**
     * Attempts to get the child element by the given name from the current one.
     *
     * @param name The name of the child element
     * @return The child element if it exists, or {@link Optional#empty()}
     *     otherwise
     */
    public Optional<HierarchyElement> getChild(String name) {
        if (children.containsKey(name)) {
            return Optional.of(children.get(name));
        }
        return Optional.empty();
    }


    /**
     * Returns an {@link ImmutableSet} of all children of this
     * {@link HierarchyElement}.
     *
     * @return An {@link ImmutableSet} of all children of this
     *     {@link HierarchyElement}
     * @throws IllegalStateException If this element is
     *     {@link HierarchyElement#isTerminal()}
     */
    public ImmutableList<HierarchyElement> getChildren() throws IllegalStateException {
        checkState(!isTerminal(), "getChildren called on terminal HierarchyElement");

        return ImmutableList.copyOf(children.values());
    }

    /**
     * Returns the parent of this {@link HierarchyElement}, if applicable.
     *
     * @return The parent of this {@link HierarchyElement}, or
     *     {@link Optional#empty()}
     */
    public Optional<HierarchyElement> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public String toString() {
        return (getParent().isPresent() ? getParent().get().toString() + Constants.CLASS_PATH_SEPARATOR_CHAR : "")
                + getName();
    }

    /**
     * Generates a hierarchy from the given string {@link Set}.
     *
     * @param strings The strings to generate a hierarchy from
     * @return The generated entry {@link HierarchyElement}
     */
    public static HierarchyElement fromSet(Set<String> strings) {
        HierarchyElement superElement = new HierarchyElement(null, false);

        for (String str : strings) {
            String[] arr = Constants.CLASS_PATH_SEPARATOR_PATTERN.split(str);

            HierarchyElement parent = superElement;
            for (int i = 0; i < arr.length - 1; i++) {
                if (parent != null && parent.getChild(arr[i]).isPresent()) {
                    parent = parent.getChild(arr[i]).get();
                } else {
                    parent = new HierarchyElement(arr[i], false, parent);
                }
            }
            new HierarchyElement(arr[arr.length - 1], true, parent);
        }

        return superElement;
    }

}
