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

public enum Visibility {
    /**
     * Public worlds are displayed in the world navigator.
     */
    PUBLIC,

    /**
     * Private worlds are displayed in an extra menu - the private world navigator.
     */
    PRIVATE,

    /**
     * Used for when the visibility of a world can be ignored.
     */
    IGNORE;

    public static Visibility matchVisibility(boolean isPrivateWorld) {
        return isPrivateWorld ? PRIVATE : PUBLIC;
    }
}