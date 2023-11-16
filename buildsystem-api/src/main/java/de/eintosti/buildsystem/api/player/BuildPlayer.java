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
package de.eintosti.buildsystem.api.player;

import de.eintosti.buildsystem.api.settings.Settings;

import java.util.UUID;

public interface BuildPlayer {

    /**
     * Gets the unique-id of the player.
     *
     * @return The player's UUID
     */
    UUID getUniqueId();

    /**
     * Gets the player's per-player settings.
     *
     * @return The player's settings
     */
    Settings getSettings();
}