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

import de.eintosti.buildsystem.api.settings.NavigatorType;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.Builder;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the different kinds of navigator types that can be opened.
 *
 * @author einTosti
 */
public enum NavigatorInventoryType {
    /**
     * The navigator inventory which contains all {@link BuildWorld}s that are still being built.
     */
    NAVIGATOR("§aWorld Navigator"),

    /**
     * The navigator inventory which contains all archived {@link BuildWorld}s.
     *
     * @see WorldStatus#ARCHIVE
     */
    ARCHIVE("§6World Archive"),

    /**
     * The navigator inventory which contains all private {@link BuildWorld}s that can only be modified by the world's creator
     * and all players who have been added as a {@link Builder}.
     *
     * @see WorldData#privateWorld()
     */
    PRIVATE("§bPrivate Worlds");

    private final String armorStandName;

    NavigatorInventoryType(String armorStandName) {
        this.armorStandName = armorStandName;
    }

    @Nullable
    public static NavigatorInventoryType matchInventoryType(Player player, String customName) {
        String typeName = customName.replace(player.getName() + " × ", "");

        for (NavigatorInventoryType navigatorInventoryType : values()) {
            if (navigatorInventoryType.getArmorStandName().equalsIgnoreCase(typeName)) {
                return navigatorInventoryType;
            }
        }

        return null;
    }

    /**
     * When opening the {@link NavigatorType#NEW} navigator, each armor stand has a custom name which is unique for every player
     * and each {@link NavigatorInventoryType}.<br>
     * Gets said name.
     *
     * @return The name which an armor stand uses to represent a navigator inventory type
     */
    public String getArmorStandName() {
        return armorStandName;
    }
}