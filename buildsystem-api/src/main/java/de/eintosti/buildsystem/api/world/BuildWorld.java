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

import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.api.world.generator.CustomGenerator;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface BuildWorld {

    /**
     * Get the bukkit world linked to the build-world.
     *
     * @return The bukkit world
     */
    World getWorld();

    /**
     * Get the name of the world.
     *
     * @return The world's name
     */
    String getName();

    /**
     * Set the name of the world.
     *
     * @param name The name to set to
     */
    void setName(String name);

    /**
     * Gets whether the world has a creator
     *
     * @return {@code true} if the world has a creator, {@code false} otherwise
     */
    boolean hasCreator();

    /**
     * Get the name of the player who created the world.
     * <p>
     * In older versions of the plugin, the creator was not saved which is why {@code null} can be returned.
     *
     * @return The name of the player who created the world
     */
    @Nullable
    String getCreator();

    /**
     * Set the name of the creator.
     *
     * @param creator The name of the creator
     */
    void setCreator(String creator);

    /**
     * Get the unique-id of the player who created the world.
     * <p>
     * In older versions of the plugin, the creator was not saved which is why {@code null} can be returned.
     *
     * @return The unique-id of the player who created the world
     */
    @Nullable
    UUID getCreatorId();

    /**
     * Set the unique-id of the creator.
     *
     * @param creatorId The unique-id of the creator
     */
    void setCreatorId(UUID creatorId);

    /**
     * Gets whether the given player is the creator of the world.
     *
     * @param player The player to check
     * @return {@code true} if the player is the creator, {@code false} otherwise
     */
    boolean isCreator(Player player);

    /**
     * Get world's type.
     *
     * @return The {@link WorldType} of the world
     */
    WorldType getType();

    /**
     * Gets the world's data.
     *
     * @return The {@link WorldData} of the world
     */
    WorldData getData();

    /**
     * Get the creation date of the world.
     *
     * @return The amount of milliseconds that have passed since {@code January 1, 1970 UTC}, until the world was created.
     */
    long getCreationDate();

    /**
     * Get the custom chunk generator used to generate the world.
     *
     * @return The custom chunk generator used to generate the world.
     */
    @Nullable
    CustomGenerator getCustomGenerator();

    /**
     * Cycles to the next {@link Difficulty}.
     */
    void cycleDifficulty();

    /**
     * Get a list of all builders who can modify the world.
     *
     * @return the list of all builders
     */
    List<Builder> getBuilders();

    /**
     * Get a list of all {@link Builder} names
     *
     * @return A list of all builder names
     */
    List<String> getBuilderNames();

    /**
     * Get a builder by the given uuid.
     *
     * @param uuid The player's unique-id
     * @return The builder object, if any, or {@code null}
     */
    @Nullable
    Builder getBuilder(UUID uuid);

    /**
     * Get whether the given uuid matches that of an added builder.
     *
     * @param uuid The unique-id of the player to be checked
     * @return Whether the player is a builder
     */
    boolean isBuilder(UUID uuid);

    /**
     * Get whether the given player has been added as a {@link Builder}.
     *
     * @param player The player to be checked
     * @return Whether the {@link Player} is a builder
     * @see #isBuilder(UUID)
     */
    boolean isBuilder(Player player);

    /**
     * Add a {@link Builder} to the current list of builders
     *
     * @param builder The builder object
     */
    void addBuilder(Builder builder);

    /**
     * Remove a {@link Builder} from the current list of builders
     *
     * @param builder The builder object
     */
    void removeBuilder(Builder builder);

    /**
     * Add a {@link Builder} to the current list of builders
     *
     * @param uuid The builder's unique ID
     * @see #removeBuilder(Builder)
     */
    void removeBuilder(UUID uuid);

    /**
     * Get whether the world has been loaded, allowing a player to enter it.
     *
     * @return {@code true} if the world is loaded, otherwise {@code false}
     */
    boolean isLoaded();

    /**
     * Unloads the world.
     * <p>
     * In order for the world to be unloaded, the following criteria must be met:
     * <ul>
     *   <li>There cannot be any players in the world</li>
     *   <li>The world is not allowed to be the spawn world</li>
     *   <li>The world is not allowed to be on the blacklist of worlds which cannot be unloaded</li>
     * </ul>
     */
    void unload();

    /**
     * Attempts to unload the world without checking if players are in the world.
     * <p>
     * As with {@link #unload()}, in order for the world to be unloaded, the following criteria must be met:
     * <ul>
     *   <li>The world is not allowed to be the spawn world</li>
     *   <li>The world is not allowed to be on the blacklist of worlds which cannot be unloaded</li>
     * </ul>
     *
     * @param save Should chunks be saved before unloading
     */
    void forceUnload(boolean save);

    /**
     * Loads the world.
     */
    void load();
}