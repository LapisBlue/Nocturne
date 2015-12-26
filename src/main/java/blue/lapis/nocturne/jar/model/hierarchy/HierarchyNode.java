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
package blue.lapis.nocturne.jar.model.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import blue.lapis.nocturne.util.Constants;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Optional;

/**
 * Represents an item in a hierarchy (either a package or a class).
 */
public class HierarchyNode extends HierarchyElement {

    private String name;
    private boolean terminal;

    private HierarchyElement parent;

    /**
     * Constructs a new {@link HierarchyNode} with the given parameters.
     *
     * @param name The name of the {@link HierarchyNode}
     * @param terminal Whether this element is terminal (terminal elements
     *     may not contain children)
     * @param parent The {@link HierarchyElement} parenting the new one
     */
    public HierarchyNode(String name, boolean terminal, HierarchyElement parent) {
        this.name = name;
        this.terminal = terminal;

        if (!isTerminal()) {
            children = new HashMap<>();
        }

        if (parent != null) {
            checkArgument(!(parent instanceof HierarchyNode) || !((HierarchyNode) parent).isTerminal(),
                    "Parent element must not be terminal");
            this.parent = parent;
            parent.addChild(this);
        }
    }

    /**
     * Returns the name of this {@link HierarchyNode}.
     *
     * @return The name of this {@link HierarchyNode}
     */
    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return getParent().isPresent() && getParent().get() instanceof HierarchyNode
                ? ((HierarchyNode) getParent().get()).getQualifiedName() + "/" + getName()
                : getName();
    }

    /**
     * Returns whether this {@link HierarchyNode} is terminal. A terminal
     * element marks the end of its respective branch, and may not be assigned
     * children.
     *
     * @return Whether this {@link HierarchyNode} is terminal
     */
    // in other words, it's kind of a loser
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Returns the parent of this {@link HierarchyNode}, if applicable.
     *
     * @return The parent of this {@link HierarchyNode}, or
     *     {@link Optional#empty()}
     */
    public Optional<HierarchyElement> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional<HierarchyNode> getChild(String name) {
        checkState(!isTerminal(), "getChild called on terminal HierarchyNode");
        return super.getChild(name);
    }

    @Override
    public ImmutableList<HierarchyNode> getChildren() throws IllegalStateException {
        checkState(!isTerminal(), "getChildren called on terminal HierarchyNode");
        return super.getChildren();
    }

    @Override
    protected void addChild(HierarchyNode element) throws IllegalArgumentException, IllegalStateException {
        checkState(!isTerminal(), "addChild called on terminal HierarchyNode");
        super.addChild(element);
    }

    @Override
    public String toString() {
        return (getParent().isPresent() && getParent().get() instanceof HierarchyNode
                ? getParent().get().toString() + Constants.CLASS_PATH_SEPARATOR_CHAR
                : "") + getName();
    }

}
