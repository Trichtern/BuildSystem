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

public enum WorldSort {
    NAME_A_TO_Z("world_sort_name_az"),
    NAME_Z_TO_A("world_sort_name_za"),
    PROJECT_A_TO_Z("world_sort_project_az"),
    PROJECT_Z_TO_A("world_sort_project_za"),
    STATUS_NOT_STARTED("world_sort_status_not_started"),
    STATUS_FINISHED("world_sort_status_finished"),
    NEWEST_FIRST("world_sort_date_newest"),
    OLDEST_FIRST("world_sort_date_oldest");

    private final String loreKey;

    WorldSort(String loreKey) {
        this.loreKey = loreKey;
    }

    public String getLoreKey() {
        return loreKey;
    }

    public static WorldSort matchWorldSort(String type) {
        if (type == null) {
            return NAME_A_TO_Z;
        }

        for (WorldSort value : values()) {
            if (value.toString().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return NAME_A_TO_Z;
    }

    public WorldSort getNext() {
        switch (this) {
            default: // NAME_A_TO_Z
                return NAME_Z_TO_A;
            case NAME_Z_TO_A:
                return PROJECT_A_TO_Z;
            case PROJECT_A_TO_Z:
                return PROJECT_Z_TO_A;
            case PROJECT_Z_TO_A:
                return STATUS_NOT_STARTED;
            case STATUS_NOT_STARTED:
                return STATUS_FINISHED;
            case STATUS_FINISHED:
                return NEWEST_FIRST;
            case NEWEST_FIRST:
                return OLDEST_FIRST;
            case OLDEST_FIRST:
                return NAME_A_TO_Z;
        }
    }

    public WorldSort getPrevious() {
        switch (this) {
            default: // NAME_A_TO_Z
                return OLDEST_FIRST;
            case NAME_Z_TO_A:
                return NAME_A_TO_Z;
            case PROJECT_A_TO_Z:
                return NAME_Z_TO_A;
            case PROJECT_Z_TO_A:
                return PROJECT_A_TO_Z;
            case STATUS_NOT_STARTED:
                return PROJECT_Z_TO_A;
            case STATUS_FINISHED:
                return STATUS_NOT_STARTED;
            case NEWEST_FIRST:
                return STATUS_FINISHED;
            case OLDEST_FIRST:
                return NEWEST_FIRST;
        }
    }
}