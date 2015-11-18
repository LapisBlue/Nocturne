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
package blue.lapis.nocturne.mapping.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Represents an item in a hierarchy (either a package or a class).
 */
public class HierarchyElement {

    private String name;
    private boolean terminal;

    private HierarchyElement parent;
    private Map<String, HierarchyElement> children;

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

    public HierarchyElement(String name, boolean terminal) {
        this(name, terminal, null);
    }

    public String getName() {
        return name;
    }

    public void addChild(HierarchyElement element) throws IllegalArgumentException, IllegalStateException {
        checkState(!isTerminal(), "addChild called on terminal HierarchyElement");
        checkArgument(!getChild(element.getName()).isPresent(),
                "Element by name " + name + " already present in hierarchy");

        children.put(element.getName(), element);
    }

    public Optional<HierarchyElement> getChild(String name) {
        if (children.containsKey(name)) {
            return Optional.of(children.get(name));
        }
        return Optional.empty();
    }

    public ImmutableList<HierarchyElement> getChildren() throws IllegalStateException {
        checkState(!isTerminal(), "getChildren called on terminal HierarchyElement");

        return ImmutableList.copyOf(children.values());
    }

    public Optional<HierarchyElement> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return (getParent().isPresent() ? getParent().get().toString() + "/" : "") + getName();
    }

}
