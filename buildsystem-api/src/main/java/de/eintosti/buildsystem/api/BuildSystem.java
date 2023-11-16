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
package de.eintosti.buildsystem.api;

import de.eintosti.buildsystem.api.player.BuildPlayer;
import de.eintosti.buildsystem.api.player.PlayerManager;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.WorldManager;

/**
 * The BuildSystem API.
 */
public interface BuildSystem {

    /**
     * Gets the {@link WorldManager}, responsible for managing {@link BuildWorld} instances.
     *
     * @return The world manager
     */
    WorldManager getWorldManager();

    /**
     * Gets the {@link PlayerManager}, responsible for managing {@link BuildPlayer} instances.
     *
     * @return The player manager
     */
    PlayerManager getPlayerManager();
}