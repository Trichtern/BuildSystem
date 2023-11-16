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
package de.eintosti.buildsystem.version.gamerules;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Trichtern
 */
public abstract class AbstractGameRulesInventory implements GameRules {

    private static final int[] SLOTS = new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

    private final Map<UUID, Integer> invIndex;
    private final String inventoryTitle;

    private Inventory[] inventories;
    private int numGameRules = 0;

    public AbstractGameRulesInventory(String inventoryTitle) {
        this.invIndex = new HashMap<>();
        this.inventoryTitle = inventoryTitle;
    }

    private Inventory createInventory() {
        return Bukkit.createInventory(null, 45, inventoryTitle);
    }

    @Override
    public Inventory getInventory(Player player, World world) {
        addGameRules(world);
        return inventories[getInvIndex(player.getUniqueId())];
    }

    @Override
    public void addGameRules(World world) {
        int columnGameRule = 0, maxColumnGameRule = 14;
        setNumGameRules(world);
        int numInventories = (numGameRules % SLOTS.length == 0 ? numGameRules : numGameRules + 1) != 0 ? (numGameRules % SLOTS.length == 0 ? numGameRules : numGameRules + 1) : 1;

        inventories = new Inventory[numInventories];
        Inventory inventory = createInventory();

        int index = 0;
        inventories[index] = inventory;

        for (String gameRule : world.getGameRules()) {
            addGameRuleItem(inventory, SLOTS[columnGameRule++], world, gameRule);

            if (columnGameRule > maxColumnGameRule) {
                columnGameRule = 0;
                inventory = createInventory();
                inventories[++index] = inventory;
            }
        }
    }

    protected abstract void addGameRuleItem(Inventory inventory, int slot, World world, String gameRule);

    protected abstract boolean isEnabled(World world, String gameRule);

    @Override
    public abstract void toggleGameRule(InventoryClickEvent event, World world);

    public boolean isValidSlot(int slot) {
        return Arrays.stream(SLOTS).anyMatch(i -> i == slot);
    }

    @Override
    public void incrementInv(Player player) {
        UUID playerUUID = player.getUniqueId();
        invIndex.put(playerUUID, getInvIndex(playerUUID) + 1);
    }

    @Override
    public void decrementInv(Player player) {
        UUID playerUUID = player.getUniqueId();
        invIndex.put(playerUUID, getInvIndex(playerUUID) - 1);
    }

    @Override
    public int getInvIndex(UUID uuid) {
        if (!invIndex.containsKey(uuid)) {
            resetInvIndex(uuid);
        }
        return invIndex.get(uuid);
    }

    @Override
    public void resetInvIndex(UUID uuid) {
        invIndex.put(uuid, 0);
    }

    @Override
    public int getNumGameRules() {
        return numGameRules;
    }

    private void setNumGameRules(World world) {
        int numWorldGameRules = world.getGameRules().length;
        this.numGameRules = (numWorldGameRules / SLOTS.length) + (numWorldGameRules % SLOTS.length == 0 ? 0 : 1);
    }

    @Override
    public int[] getSlots() {
        return SLOTS;
    }
}