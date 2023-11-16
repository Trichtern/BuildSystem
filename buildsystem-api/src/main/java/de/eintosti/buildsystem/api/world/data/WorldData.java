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
package de.eintosti.buildsystem.api.world.data;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.api.world.BuildWorld;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public interface WorldData {

    /**
     * Gets the {@link BuildWorld}'s custom spawn in the format {@code x;y;z;yaw;pitch}.
     *
     * @return The custom spawn as a string
     * @see #getCustomSpawnLocation()
     */
    Type<String> customSpawn();

    /**
     * Gets the {@link BuildWorld}'s custom spawn as a location.
     *
     * @return The custom spawn as a location
     */
    @Nullable
    Location getCustomSpawnLocation();

    /**
     * Gets the permission needed to enter the {@link BuildWorld}.
     *
     * @return The permission
     */
    Type<String> permission();

    /**
     * Gets the project description of the {@link BuildWorld}.
     *
     * @return The project description
     */
    Type<String> project();

    /**
     * Gets the difficulty of the {@link BuildWorld}.
     *
     * @return The bukkit world's difficulty
     */
    Type<Difficulty> difficulty();

    /**
     * Gets the material used to display the {@link BuildWorld} in the navigator.
     *
     * @return The material
     */
    Type<XMaterial> material();

    /**
     * Gets the current status of the {@link BuildWorld}.
     *
     * @return The current status
     */
    Type<WorldStatus> status();

    /**
     * Gets whether blocks can be broken in the {@link BuildWorld}.
     *
     * @return {@code true} if allowed, otherwise {@code false}
     */
    Type<Boolean> blockBreaking();

    /**
     * Gets whether blocks interaction are enabled in the {@link BuildWorld}.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    Type<Boolean> blockInteractions();

    /**
     * Gets whether blocks can be placed in the {@link BuildWorld}.
     *
     * @return {@code true} if allowed, otherwise {@code false}
     */
    Type<Boolean> blockPlacement();

    /**
     * Gets whether the "builders feature" is enabled in the {@link BuildWorld}.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    Type<Boolean> buildersEnabled();

    /**
     * Gets whether explosions are enabled in the {@link BuildWorld}.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    Type<Boolean> explosions();

    /**
     * Gets whether entities have an AI in the {@link BuildWorld}.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    Type<Boolean> mobAi();

    /**
     * Gets whether physics is applied to blocks in the {@link BuildWorld}.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    Type<Boolean> physics();

    /**
     * Gets whether the visibility of the {@link BuildWorld} is set to private.
     *
     * @return {@code true} if private, otherwise {@code false}
     */
    Type<Boolean> privateWorld();

    /**
     * Gets the timestamp of the last time the {@link BuildWorld} was last edited.
     *
     * @return The difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    Type<Long> lastEdited();

    /**
     * Gets the timestamp of the last time the {@link BuildWorld} was last loaded.
     *
     * @return The difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    Type<Long> lastLoaded();

    /**
     * Gets the timestamp of the last time the {@link BuildWorld} was last unloaded.
     *
     * @return The difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    Type<Long> lastUnloaded();

    interface Type<T> {

        /**
         * Gets the current value.
         *
         * @return The current value
         */
        T get();

        /**
         * Sets the current value.
         *
         * @param value The value to set to
         */
        void set(T value);
    }
}