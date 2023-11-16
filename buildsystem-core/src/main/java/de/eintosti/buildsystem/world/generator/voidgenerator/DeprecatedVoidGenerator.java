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
package de.eintosti.buildsystem.world.generator.voidgenerator;

import org.bukkit.World;
import org.bukkit.block.Biome;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * The {@link VoidGenerator} that is used for servers which run on Spigot 1.13-1.16.
 */
@SuppressWarnings("deprecation")
public class DeprecatedVoidGenerator extends VoidGenerator {

    public DeprecatedVoidGenerator() {
        super(Biome.PLAINS);
    }

    @Nonnull
    @Override
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biome) {
        biome.setBiome(x, z, super.getBiome());
        return createChunkData(world);
    }
}