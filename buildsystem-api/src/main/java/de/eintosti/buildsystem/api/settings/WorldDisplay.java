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
package de.eintosti.buildsystem.api.settings;

import de.eintosti.buildsystem.api.world.BuildWorld;

public interface WorldDisplay {

    /**
     * Gets the order in which the {@link BuildWorld}s are sorted.
     *
     * @return The world sort order
     */
    WorldSort getWorldSort();

    /**
     * Sets the order in which the {@link BuildWorld}s are sorted.
     *
     * @param worldSort The world sort order
     */
    void setWorldSort(WorldSort worldSort);

    /**
     * Gets the filter which removed non-matching {@link BuildWorld}s from the navigator
     *
     * @return The world filter
     */
    WorldFilter getWorldFilter();
}