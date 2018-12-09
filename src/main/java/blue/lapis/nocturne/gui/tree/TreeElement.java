//******************************************************************************
// Copyright (c) Jamie Mansfield <https://jamiemansfield.me/>
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//******************************************************************************

package blue.lapis.nocturne.gui.tree;

/**
 * An element within a tree.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public interface TreeElement extends Comparable<TreeElement> {

    /**
     * The name of the tree element.
     *
     * @return The element name
     */
    @Override
    String toString();

    /**
     * Invoked when the element is double clicked, or other
     * equivalent action.
     */
    void activate();

}
