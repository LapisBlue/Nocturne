/*
 * Nocturne
 * Copyright (c) 2015-2019, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.gui.scene.control;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.MainController;
import blue.lapis.nocturne.jar.model.hierarchy.Hierarchy;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyElement;
import blue.lapis.nocturne.jar.model.hierarchy.HierarchyNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link TreeView} to display classes and packages, from a
 * {@link Hierarchy}.
 *
 * @author Jamie Mansfield
 */
public class ClassesTreeView extends TreeView<String> {

    private BooleanProperty checkLength;
    public final void setCheckLength(final boolean value) {
        this.checkLengthProperty().set(value);
    }
    public final boolean isCheckLength() {
        return this.checkLength == null || this.checkLength.get();
    }
    public final BooleanProperty checkLengthProperty() {
        if (this.checkLength == null) {
            this.checkLength = new SimpleBooleanProperty(this, "checkLength", true);
        }
        return this.checkLength;
    }

    public ClassesTreeView() {
        this.setShowRoot(false);
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // isDoubleClick
                this.open(event);
            }
        });
        this.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.open(event);
            }
        });
    }

    private void open(final InputEvent event) {
        TreeItem<String> selected = this.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (selected.getChildren().isEmpty()) {
            final String className = ((IdentifiableTreeItem) selected).getId().substring(1);
            if (Main.getLoadedJar() != null) {
                MainController.INSTANCE.openTab(className, selected.getValue());
            }
        } else {
            if (event instanceof MouseEvent == selected.isExpanded()) {
                selected.setExpanded(true);
                while (selected.getChildren().size() == 1) {
                    selected = selected.getChildren().get(0);
                    selected.setExpanded(true);
                }
            } else {
                selected.setExpanded(false);
            }
        }
    }

    public void update(final HierarchyElement element) {
        if (Main.getLoadedJar() != null) {
            final TreeItem<String> root = generateTreeItem(element,
                    getExpandedIds((IdentifiableTreeItem) this.getRoot()), this.isCheckLength());
            root.setExpanded(true);
            this.setRoot(root);
        } else {
            this.setRoot(null);
        }
    }

    public TreeItem<String> generateTreeItem(final HierarchyElement element, final Set<String> expanded,
                                             final boolean checkLength) {
        final IdentifiableTreeItem treeItem;
        if (element instanceof HierarchyNode) {
            HierarchyNode node = (HierarchyNode) element;
            treeItem = new IdentifiableTreeItem((node.isTerminal() ? "C" : "P") + node.getId(), node.getDisplayName());
        } else {
            treeItem = new IdentifiableTreeItem("//root", "(root)");
        }

        if (expanded.contains(treeItem.getId())) {
            treeItem.setExpanded(true);
        }

        if (element instanceof Hierarchy
                || (element instanceof HierarchyNode && !((HierarchyNode) element).isTerminal())) {
            treeItem.getChildren().addAll(element.getChildren().stream()
                    .map(e -> this.generateTreeItem(e, expanded, checkLength)).collect(Collectors.toList()));
        }
        treeItem.getChildren().setAll(treeItem.getChildren().sorted((t1, t2) -> {
            boolean c1 = t1.getChildren().size() > 0;
            boolean c2 = t2.getChildren().size() > 0;
            if (c1 == c2) { // both either terminal or non-terminal
                if (checkLength && t1.getValue().length() != t2.getValue().length()) {
                    return t1.getValue().length() - t2.getValue().length();
                }
                return t1.getValue().compareTo(t2.getValue());
            } else if (c1) { // first is non-terminal, second is terminal
                return -1;
            } else { // first is terminal, second is non-terminal
                return 1;
            }
        }));
        return treeItem;
    }

    private static Map<String, IdentifiableTreeItem> flatten(final IdentifiableTreeItem tree) {
        final Map<String, IdentifiableTreeItem> map = new HashMap<>();
        map.put(tree.getId(), tree);
        if (tree.getChildren().isEmpty()) {
            return map;
        }

        for (final TreeItem<String> child : tree.getChildren()) {
            map.putAll(flatten((IdentifiableTreeItem) child));
        }
        return map;
    }

    private static Set<String> getExpandedIds(final IdentifiableTreeItem tree) {
        if (tree == null) {
            return Collections.emptySet();
        }

        return flatten(tree).entrySet().stream().filter(e -> e.getValue().isExpanded()).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
