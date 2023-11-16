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
package de.eintosti.buildsystem.api.world;

import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.api.world.generator.CustomGenerator;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public interface BuildWorldCreator {

    /**
     * Sets the name of the world.
     *
     * @param name The world name
     * @return The world creator object
     */
    BuildWorldCreator setName(String name);

    /**
     * Sets the creator of the world.
     *
     * @param creator The creator, may be {@code null}
     * @return The world creator object
     */
    BuildWorldCreator setCreator(@Nullable Builder creator);

    /**
     * Sets the template which the world should be copied from.
     * <p>
     * Only used if the world type is {@link WorldType#TEMPLATE}
     *
     * @param template The template name
     * @return The creator object
     */
    BuildWorldCreator setTemplate(String template);

    /**
     * Sets the type of the world.
     *
     * @param type The world type
     * @return The world creator object
     */
    BuildWorldCreator setType(WorldType type);

    /**
     * Sets the custom {@link ChunkGenerator} of the world.
     *
     * @param customGenerator The custom chunk generator
     * @return The world creator object
     */
    BuildWorldCreator setCustomGenerator(CustomGenerator customGenerator);

    BuildWorldCreator setPrivate(boolean privateWorld);

    BuildWorldCreator setDifficulty(Difficulty difficulty);

    BuildWorldCreator setCreationDate(long creationDate);

    /**
     * Depending on the {@link BuildWorld}'s {@link WorldType}, the corresponding {@link World} will be generated in a different way.
     * Then, if the creation of the world was successful and the config is set accordingly, the player is teleported to the world.
     *
     * @param player The player who is creating the world
     */
    void createWorld(Player player);

    /**
     * Imports an existing world as a {@link BuildWorld}.
     *
     * @param player   The player who is importing the world
     * @param teleport Should the player be teleported to the world after importing is finished
     */
    void importWorld(Player player, boolean teleport);

    @Nullable
    World generateBukkitWorld();

    /**
     * Generate the {@link World} linked to a {@link BuildWorld}.
     *
     * @param checkVersion Should the world version be checked
     * @return The world object
     */
    @Nullable
    World generateBukkitWorld(boolean checkVersion);
}