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
package de.eintosti.buildsystem.internal;

import com.google.common.collect.Lists;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.version.customblocks.CustomBlocks;
import de.eintosti.buildsystem.version.gamerules.GameRules;
import de.eintosti.buildsystem.version.util.MinecraftVersion;
import de.eintosti.buildsystem.version.v1_12_R1.CustomBlocks_1_12_R1;
import de.eintosti.buildsystem.version.v1_12_R1.GameRules_1_12_R1;
import de.eintosti.buildsystem.version.v1_13_R1.CustomBlocks_1_13_R1;
import de.eintosti.buildsystem.version.v1_13_R1.GameRules_1_13_R1;
import de.eintosti.buildsystem.version.v1_14_R1.CustomBlocks_1_14_R1;
import de.eintosti.buildsystem.version.v1_17_R1.CustomBlocks_1_17_R1;
import de.eintosti.buildsystem.version.v1_20_R1.CustomBlocks_1_20_R1;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.List;

public enum CraftBukkitVersion {

    v1_8_R1(-1, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_8_R2(-1, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_8_R3(-1, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_9_R1(169, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_9_R2(184, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_10_R1(512, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_11_R1(922, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_12_R1(1343, CustomBlocks_1_12_R1.class, GameRules_1_12_R1.class),
    v1_13_R1(1519, CustomBlocks_1_13_R1.class, GameRules_1_13_R1.class),
    v1_13_R2(1631, CustomBlocks_1_13_R1.class, GameRules_1_13_R1.class),
    v1_14_R1(1976, CustomBlocks_1_14_R1.class, GameRules_1_13_R1.class),
    v1_15_R1(2230, CustomBlocks_1_14_R1.class, GameRules_1_13_R1.class),
    v1_16_R1(2567, CustomBlocks_1_14_R1.class, GameRules_1_13_R1.class),
    v1_16_R2(2580, CustomBlocks_1_14_R1.class, GameRules_1_13_R1.class),
    v1_16_R3(2586, CustomBlocks_1_14_R1.class, GameRules_1_13_R1.class),
    v1_17_R1(2730, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_18_R1(2865, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_18_R2(2975, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_19_R1(3120, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_19_R2(3218, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_19_R3(3337, CustomBlocks_1_17_R1.class, GameRules_1_13_R1.class),
    v1_20_R1(3465, CustomBlocks_1_20_R1.class, GameRules_1_13_R1.class),
    v1_20_R2(3578, CustomBlocks_1_20_R1.class, GameRules_1_13_R1.class),
    UNKNOWN;

    private final int dataVersion;
    private final Class<? extends CustomBlocks> customBlocks;
    private final Class<? extends GameRules> gameRules;

    private final BuildSystemPlugin plugin = JavaPlugin.getPlugin(BuildSystemPlugin.class);

    CraftBukkitVersion(int dataVersion, Class<? extends CustomBlocks> customBlocks, Class<? extends GameRules> gameRules) {
        this.dataVersion = dataVersion;
        this.customBlocks = customBlocks;
        this.gameRules = gameRules;
    }

    CraftBukkitVersion() {
        this.dataVersion = -1;
        this.customBlocks = null;
        this.gameRules = null;
    }

    public static CraftBukkitVersion matchCraftBukkitVersion(MinecraftVersion version) {
        int minor = version.getMinor();
        int patch = version.getPatch();

        switch (minor) {
            case 8:
                if (patch <= 2) {
                    return v1_8_R1;
                } else if (patch == 3) {
                    return v1_8_R2;
                } else {
                    return v1_8_R3;
                }
            case 9:
                if (patch <= 3) {
                    return v1_9_R1;
                } else {
                    return v1_9_R2;
                }
            case 10:
                return v1_10_R1;
            case 11:
                return v1_11_R1;
            case 12:
                return v1_12_R1;
            case 13:
                if (patch == 0) {
                    return v1_13_R1;
                } else {
                    return v1_13_R2;
                }
            case 14:
                return v1_14_R1;
            case 15:
                return v1_15_R1;
            case 16:
                if (patch <= 1) {
                    return v1_16_R1;
                } else if (patch <= 3) {
                    return v1_16_R2;
                } else {
                    return v1_16_R3;
                }
            case 17:
                return v1_17_R1;
            case 18:
                if (patch <= 1) {
                    return v1_18_R1;
                } else {
                    return v1_18_R2;
                }
            case 19:
                if (patch <= 2) {
                    return v1_19_R1;
                } else if (patch == 3) {
                    return v1_19_R2;
                } else {
                    return v1_19_R3;
                }
            case 20:
                if (patch <= 1) {
                    return v1_20_R1;
                } else {
                    return v1_20_R2;
                }
            default:
                if (Boolean.getBoolean("Paper.ignoreWorldDataVersion")) {
                    // Get latest version if server version is to be ignored
                    return Lists.newArrayList(values()).stream()
                            .sorted()
                            .filter(craftBukkitVersion -> craftBukkitVersion != UNKNOWN)
                            .reduce((first, second) -> second)
                            .orElse(UNKNOWN);
                } else {
                    return UNKNOWN;
                }
        }
    }

    /**
     * Gets the server's data version.
     * <p>
     * "The data version is a positive integer used in a world saved data to denote a specific version, and determines
     * whether the player should be warned about opening that world due to client version incompatibilities."
     *
     * @return The server's data version
     * @see <a href="https://minecraft.wiki/wiki/Data_version">Data version</a>
     */
    public int getDataVersion() {
        return dataVersion;
    }

    @Nullable
    public CustomBlocks initCustomBlocks() {
        if (this == UNKNOWN || customBlocks == null) {
            return null;
        }

        try {
            Constructor<? extends CustomBlocks> constructor = customBlocks.getConstructor(JavaPlugin.class);
            return constructor.newInstance(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public GameRules initGameRules() {
        if (this == UNKNOWN || gameRules == null) {
            return null;
        }

        try {
            Constructor<? extends GameRules> constructor = gameRules.getConstructor(String.class, List.class, List.class, List.class);
            return constructor.newInstance(
                    Messages.getString("worldeditor_gamerules_title", null),
                    Messages.getStringList("worldeditor_gamerules_boolean_enabled", null),
                    Messages.getStringList("worldeditor_gamerules_boolean_disabled", null),
                    Messages.getStringList("worldeditor_gamerules_integer", null)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}