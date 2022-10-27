/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.object.world.generator;

import org.bukkit.generator.ChunkGenerator;

public class CustomGenerator {

    private final String name;
    private final ChunkGenerator chunkGenerator;

    public CustomGenerator(String name, ChunkGenerator chunkGenerator) {
        this.name = name;
        this.chunkGenerator = chunkGenerator;
    }

    public String getName() {
        return name;
    }

    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }
}