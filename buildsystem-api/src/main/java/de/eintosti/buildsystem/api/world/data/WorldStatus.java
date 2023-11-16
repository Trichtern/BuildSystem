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

import de.eintosti.buildsystem.api.world.BuildWorld;

public enum WorldStatus {

    /**
     * Represent a world that has not been modified.
     */
    NOT_STARTED("status_not_started", 1),

    /**
     * Represents a world that is currently being built.
     * <p>
     * The status is automatically switched to this when a block is placed/broken.
     */
    IN_PROGRESS("status_in_progress", 2),

    /**
     * Represents a world that has almost been completed.
     */
    ALMOST_FINISHED("status_almost_finished", 3),

    /**
     * Represents a world that has completed its building phase.
     */
    FINISHED("status_finished", 4),

    /**
     * Represents an old world that has been finished for a while. Blocks cannot be placed/broken in archived worlds.
     */
    ARCHIVE("status_archive", 5),

    /**
     * Represents a world that is not shown in the navigator.
     */
    HIDDEN("status_hidden", 6);

    private final String key;
    private final int stage;

    WorldStatus(String key, int stage) {
        this.key = key;
        this.stage = stage;
    }

    /**
     * Gets the display name of the status.
     *
     * @return The type's display name
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the permission needed to change the status.
     *
     * @return The permission needed to change the status
     */
    public String getPermission() {
        return "buildsystem.setstatus." + name().toLowerCase().replace("_", "");
    }

    /**
     * Gets the stage in which the {@link BuildWorld} is currently in.
     * A higher value means the world is further in development.
     *
     * @return the stage in which the world is currently in.
     */
    public int getStage() {
        return stage;
    }
}