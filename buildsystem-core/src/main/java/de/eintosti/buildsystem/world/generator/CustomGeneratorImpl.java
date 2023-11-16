/*
 * Copyright (c) 2018-2023, Thomas Meaney
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.eintosti.buildsystem.world.generator;

import de.eintosti.buildsystem.api.world.generator.CustomGenerator;
import org.bukkit.generator.ChunkGenerator;

public class CustomGeneratorImpl implements CustomGenerator {

    private final String name;
    private final ChunkGenerator chunkGenerator;

    public CustomGeneratorImpl(String name, ChunkGenerator chunkGenerator) {
        this.name = name;
        this.chunkGenerator = chunkGenerator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }
}