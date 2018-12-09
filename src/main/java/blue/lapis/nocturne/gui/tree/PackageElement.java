//******************************************************************************
// Copyright (c) Jamie Mansfield <https://jamiemansfield.me/>
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//******************************************************************************

package blue.lapis.nocturne.gui.tree;

/**
 * A tree element for packages.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class PackageElement implements TreeElement {

    private final String name;
    private final String simpleName;

    public PackageElement(final String name) {
        this.name = name;
        this.simpleName = name.substring(name.lastIndexOf('/') + 1);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void activate() {
    }

    @Override
    public int compareTo(final TreeElement o) {
        if (o instanceof ClassElement) return -1;
        return this.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return this.simpleName;
    }

}
