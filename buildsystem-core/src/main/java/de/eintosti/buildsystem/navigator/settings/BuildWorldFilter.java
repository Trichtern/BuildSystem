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

import de.eintosti.buildsystem.api.settings.WorldFilter;
import de.eintosti.buildsystem.api.world.BuildWorld;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BuildWorldFilter implements WorldFilter, ConfigurationSerializable {

    private Mode mode;
    private String text;

    public BuildWorldFilter() {
        this.mode = Mode.NONE;
        this.text = "";
    }

    public BuildWorldFilter(Mode mode, String text) {
        this.mode = mode;
        this.text = text;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Predicate<BuildWorld> apply() {
        switch (mode) {
            case STARTS_WITH:
                return buildWorld -> buildWorld.getName().startsWith(text);
            case CONTAINS:
                return buildWorld -> buildWorld.getName().contains(text);
            case MATCHES:
                return buildWorld -> buildWorld.getName().matches(text);
            default:
                return buildWorld -> true;
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> display = new HashMap<>();
        display.put("mode", mode.toString());
        display.put("text", text);
        return display;
    }
}