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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Provides static access to the {@link BuildSystem} API.
 */
public class BuildSystemProvider {

    private static BuildSystem instance = null;

    /**
     * Gets an instance of the {@link BuildSystem} API.
     *
     * @return An instance of the BuildSystem API
     * @throws IllegalStateException if the API is not loaded yet
     */
    @NotNull
    public static BuildSystem get() {
        BuildSystem instance = BuildSystemProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("BuildSystem has not loaded yet!");
        }
        return instance;
    }

    @ApiStatus.Internal
    static void register(BuildSystem instance) {
        BuildSystemProvider.instance = instance;
    }

    @ApiStatus.Internal
    static void unregister() {
        BuildSystemProvider.instance = null;
    }

    @ApiStatus.Internal
    private BuildSystemProvider() {
        throw new AssertionError();
    }
}