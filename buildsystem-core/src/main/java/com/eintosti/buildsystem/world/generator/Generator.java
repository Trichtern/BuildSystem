/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.world.generator;

import com.eintosti.buildsystem.world.data.WorldType;

/**
 * @author einTosti
 */
public enum Generator {
    NORMAL(WorldType.NORMAL),
    FLAT(WorldType.FLAT),
    VOID(WorldType.VOID),
    CUSTOM(WorldType.IMPORTED);

    private final WorldType worldType;

    Generator(WorldType worldType) {
        this.worldType = worldType;
    }

    public WorldType getWorldType() {
        return worldType;
    }
}