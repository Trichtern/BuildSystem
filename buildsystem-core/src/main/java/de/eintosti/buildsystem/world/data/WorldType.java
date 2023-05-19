/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.world.data;

import de.eintosti.buildsystem.Messages;
import org.bukkit.World.Environment;

public enum WorldType {
    /**
     * The equivalent to a default Minecraft world with {@link Environment#NORMAL}.
     */
    NORMAL("type_normal"),

    /**
     * The equivalent to a super-flat Minecraft world.
     */
    FLAT("type_flat"),

    /**
     * The equivalent to a default Minecraft world with {@link Environment#NETHER}.
     */
    NETHER("type_nether"),

    /**
     * The equivalent to a default Minecraft world with {@link Environment#THE_END}.
     */
    END("type_end"),

    /**
     * A completely empty world with no blocks at all, except the block a player spawns on.
     */
    VOID("type_void"),

    /**
     * A world which is an identical copy of a provided template.
     */
    TEMPLATE("type_template"),

    /**
     * A world which by default cannot be modified by any player except for the creator.
     */
    PRIVATE("type_private"),

    /**
     * A world which was not created by BuildSystem but was imported, so it can be used by the plugin.
     */
    IMPORTED(null),

    /**
     * A world with a custom chunk generator
     */
    CUSTOM("type_custom"),

    /**
     * A world with an unknown type.
     */
    UNKNOWN(null);

    private final String typeNameKey;

    WorldType(String typeNameKey) {
        this.typeNameKey = typeNameKey;
    }

    /**
     * Get the display name of the {@link WorldType}.
     *
     * @return The type's display name
     */
    public String getName() {
        if (typeNameKey == null) {
            return "-";
        }
        return Messages.getString(typeNameKey);
    }
}