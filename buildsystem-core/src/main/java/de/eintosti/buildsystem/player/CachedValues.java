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
package de.eintosti.buildsystem.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CachedValues {

    private GameMode gameMode;
    private ItemStack[] inventory;

    private Float walkSpeed;
    private Float flySpeed;

    public void saveGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void resetGameModeIfPresent(Player player) {
        if (this.gameMode == null) {
            return;
        }
        player.setGameMode(gameMode);
        this.gameMode = null;
    }

    public void saveInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public void resetInventoryIfPresent(Player player) {
        if (this.inventory == null) {
            return;
        }
        player.getInventory().clear();
        player.getInventory().setContents(this.inventory);
        this.inventory = null;
    }

    public void saveWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public void resetWalkSpeedIfPresent(Player player) {
        if (this.walkSpeed == null) {
            return;
        }
        player.setWalkSpeed(walkSpeed);
        this.walkSpeed = null;
    }

    public void saveFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public void resetFlySpeedIfPresent(Player player) {
        if (this.flySpeed == null) {
            return;
        }
        player.setFlySpeed(flySpeed);
        this.flySpeed = null;
    }

    public void resetCachedValues(Player player) {
        resetGameModeIfPresent(player);
        resetInventoryIfPresent(player);
        resetWalkSpeedIfPresent(player);
        resetFlySpeedIfPresent(player);
    }
}