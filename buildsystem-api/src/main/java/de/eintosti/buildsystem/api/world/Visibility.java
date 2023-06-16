/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.api.world;

public enum Visibility {
    PRIVATE,
    PUBLIC,
    IGNORE;

    public static Visibility matchVisibility(boolean isPrivateWorld) {
        return isPrivateWorld ? PRIVATE : PUBLIC;
    }
}