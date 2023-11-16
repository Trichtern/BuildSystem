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
package de.eintosti.buildsystem.version.util;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public final class DirectionUtil {

    public static BlockFace getPlayerDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        yaw %= 360;
        int i = (int) ((yaw + 8) / 22.5);
        switch (i) {
            case 1:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 2:
                return BlockFace.SOUTH_WEST;
            case 3:
                return BlockFace.WEST_SOUTH_WEST;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.WEST_NORTH_WEST;
            case 6:
                return BlockFace.NORTH_WEST;
            case 7:
                return BlockFace.NORTH_NORTH_WEST;
            case 8:
                return BlockFace.NORTH;
            case 9:
                return BlockFace.NORTH_NORTH_EAST;
            case 10:
                return BlockFace.NORTH_EAST;
            case 11:
                return BlockFace.EAST_NORTH_EAST;
            case 12:
                return BlockFace.EAST;
            case 13:
                return BlockFace.EAST_SOUTH_EAST;
            case 14:
                return BlockFace.SOUTH_EAST;
            case 15:
                return BlockFace.SOUTH_SOUTH_EAST;
            default:
                return BlockFace.SOUTH;
        }
    }

    /**
     * Gets the direction the block should be facing.
     *
     * @param player           The player placing the block
     * @param allowNonCardinal Should the block be allowed to face {@link BlockFace#UP} and {@link BlockFace#DOWN}
     * @return The direction the block should be facing
     */
    public static BlockFace getBlockDirection(Player player, boolean allowNonCardinal) {
        if (!allowNonCardinal) {
            return getCardinalDirection(player);
        }

        float pitch = player.getLocation().getPitch();
        if (pitch <= -45) {
            return BlockFace.DOWN;
        } else if (pitch >= 45) {
            return BlockFace.UP;
        } else {
            return getCardinalDirection(player);
        }
    }

    public static BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        yaw %= 360;

        int i = (int) ((yaw + 8) / 22.5);
        switch (i) {
            case 15:
            case 0:
            case 1:
            case 2:
                return BlockFace.NORTH;
            case 3:
            case 4:
            case 5:
            case 6:
                return BlockFace.EAST;
            case 7:
            case 8:
            case 9:
            case 10:
                return BlockFace.SOUTH;
            case 11:
            case 12:
            case 13:
            case 14:
                return BlockFace.WEST;
        }

        return BlockFace.NORTH;
    }
}