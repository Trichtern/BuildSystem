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

import org.bukkit.entity.Player;

import java.util.UUID;

public interface Builder {

    /**
     * Returns a unique and persistent id for the builder.
     * Should be equal to the corresponding {@link Player}'s unique id.
     *
     * @return The uuid
     * @see Player#getUniqueId()
     */
    UUID getUuid();

    /**
     * Gets the name of the builder.
     *
     * @return The builder name
     */
    String getName();

    /**
     * Sets the name of the builder.
     *
     * @param name The name to change to
     */
    void setName(String name);
}