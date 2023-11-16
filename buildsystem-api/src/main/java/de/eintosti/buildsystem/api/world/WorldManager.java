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

import de.eintosti.buildsystem.api.world.data.Visibility;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.api.world.generator.Generator;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

public interface WorldManager {

    /**
     * Gets the {@link BuildWorld} by the given name.
     *
     * @param worldName The name of the world
     * @return The world object if one was found, {@code null} otherwise
     */
    BuildWorld getBuildWorld(String worldName);

    /**
     * Gets the {@link BuildWorld} by the given {@link World}.
     *
     * @param world The bukkit world object
     * @return The world object if one was found, {@code null} otherwise
     */
    BuildWorld getBuildWorld(World world);

    /**
     * Gets a list of all {@link BuildWorld}s.
     *
     * @return A list of all worlds
     */
    @Unmodifiable
    Collection<BuildWorld> getBuildWorlds();

    /**
     * Gets a list of all {@link BuildWorld}s created by the given player.
     *
     * @param player The player who created the world
     * @return A list of all worlds created by the given player.
     */
    List<BuildWorld> getBuildWorldsCreatedByPlayer(Player player);

    /**
     * Gets a list of all {@link BuildWorld}s created by the given player.
     *
     * @param player     The player who created the world
     * @param visibility The visibility the world should have
     * @return A list of all worlds created by the given player.
     */
    List<BuildWorld> getBuildWorldsCreatedByPlayer(Player player, Visibility visibility);

    /**
     * Checks if a world with the given name already exists.
     *
     * @param worldName The name of the world
     * @return Whether if a world with the given name already exists
     */
    boolean worldExists(String worldName);

    /**
     * Change the name of a {@link BuildWorld} to a given name.
     *
     * @param buildWorld The build world object
     * @param newName    The name the world should be renamed to
     */
    void renameWorld(BuildWorld buildWorld, String newName);

    /**
     * Gets the {@link ChunkGenerator} for the generation of a {@link BuildWorld} with {@link WorldType#CUSTOM}.
     *
     * @param plugin      The name of the plugin providing the generator
     * @param generatorId Unique ID, if any, that was specified to indicate which generator was requested
     * @param worldName   Name of the world that the chunk generator should be applied to.
     * @return The chunk generator for use in the world generation
     */
    @Nullable
    ChunkGenerator getChunkGenerator(String plugin, String generatorId, String worldName);

    /**
     * Gets a builder for creating new {@link BuildWorld}s.
     *
     * @param worldName The name of the world to create
     * @return The world creator
     */
    BuildWorldCreator newWorldCreator(String worldName);

    /**
     * Import a {@link BuildWorld} from a world directory.
     *
     * @param worldName     The name of the world to import
     * @param creator       The builder who should be set as the creator
     * @param generator     The generator type used by the world
     * @param generatorName The name of the custom generator if generator type is {@link Generator#CUSTOM}
     * @return {@code true} if the world was successfully imported, otherwise {@code false}
     */
    boolean importWorld(String worldName, Builder creator, Generator generator, String generatorName);

    /**
     * Delete an existing {@link BuildWorld}.
     * In comparison to {@link #unimportWorld(BuildWorld, boolean)}, deleting a world deletes the world's directory.
     *
     * @param buildWorld The world to be deleted
     */
    void deleteWorld(BuildWorld buildWorld);

    /**
     * Unimport an existing {@link BuildWorld}.
     * In comparison to {@link #deleteWorld(BuildWorld)}, unimporting a world does not delete the world's directory.
     *
     * @param buildWorld The build world object
     * @param save       Should the world be saved before unimporting
     */
    void unimportWorld(BuildWorld buildWorld, boolean save);

    /**
     * Teleport a player to a {@link BuildWorld}.
     *
     * @param player    The player to be teleported
     * @param worldName The name of the world
     */
    void teleport(Player player, String worldName);

    /**
     * Checks if the provided player is allowed to enter the {@link BuildWorld}.
     *
     * @param player     The player
     * @param buildWorld The world
     * @return {@code true} if the player is allowed to enter, otherwise {@code false}
     */
    boolean canEnter(Player player, BuildWorld buildWorld);
}