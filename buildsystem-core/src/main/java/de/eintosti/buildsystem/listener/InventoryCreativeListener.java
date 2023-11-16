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
package de.eintosti.buildsystem.listener;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.event.player.PlayerInventoryClearEvent;
import de.eintosti.buildsystem.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public class InventoryCreativeListener implements Listener {

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    public InventoryCreativeListener(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClearInventory(InventoryCreativeEvent event) {
        if (event.getClick() != ClickType.CREATIVE || event.getSlotType() != InventoryType.SlotType.QUICKBAR || event.getAction() != InventoryAction.PLACE_ALL) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        List<Integer> navigatorSlots = inventoryUtils.getNavigatorSlots(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerInventoryClearEvent playerInventoryClearEvent = new PlayerInventoryClearEvent(player, navigatorSlots);
            Bukkit.getServer().getPluginManager().callEvent(playerInventoryClearEvent);
        }, 2L);
    }
}