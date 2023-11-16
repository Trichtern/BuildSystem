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
package de.eintosti.buildsystem.navigator.settings;

import de.eintosti.buildsystem.api.settings.WorldDisplay;
import de.eintosti.buildsystem.api.settings.WorldSort;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BuildWorldDisplay implements WorldDisplay, ConfigurationSerializable {

    private WorldSort worldSort;
    private final BuildWorldFilter worldFilter;

    public BuildWorldDisplay() {
        this.worldSort = WorldSort.NAME_A_TO_Z;
        this.worldFilter = new BuildWorldFilter();
    }

    public BuildWorldDisplay(WorldSort worldSort, BuildWorldFilter worldFilter) {
        this.worldSort = worldSort;
        this.worldFilter = worldFilter;
    }

    @Override
    public WorldSort getWorldSort() {
        return worldSort;
    }

    @Override
    public void setWorldSort(WorldSort worldSort) {
        this.worldSort = worldSort;
    }

    @Override
    public BuildWorldFilter getWorldFilter() {
        return worldFilter;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> display = new HashMap<>();
        display.put("sort", getWorldSort().toString());
        display.put("filter", getWorldFilter().serialize());
        return display;
    }
}