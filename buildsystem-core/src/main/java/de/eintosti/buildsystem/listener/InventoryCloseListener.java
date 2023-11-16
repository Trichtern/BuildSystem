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

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.util.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCloseListener implements Listener {

    private final InventoryUtils inventoryUtils;

    public InventoryCloseListener(BuildSystemPlugin plugin) {
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSetupInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(Messages.getString("setup_title", (Player) event.getPlayer()))) {
            return;
        }
        setNewItems(event);
    }

    private void setNewItems(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        ItemStack normalCreateItem = inventory.getItem(11);
        ItemStack flatCreateItem = inventory.getItem(12);
        ItemStack netherCreateItem = inventory.getItem(13);
        ItemStack endCreateItem = inventory.getItem(14);
        ItemStack voidCreateItem = inventory.getItem(15);

        inventoryUtils.setCreateItem(WorldType.NORMAL, normalCreateItem != null ? XMaterial.matchXMaterial(normalCreateItem) : null);
        inventoryUtils.setCreateItem(WorldType.FLAT, flatCreateItem != null ? XMaterial.matchXMaterial(flatCreateItem) : null);
        inventoryUtils.setCreateItem(WorldType.NETHER, netherCreateItem != null ? XMaterial.matchXMaterial(netherCreateItem) : null);
        inventoryUtils.setCreateItem(WorldType.END, endCreateItem != null ? XMaterial.matchXMaterial(endCreateItem) : null);
        inventoryUtils.setCreateItem(WorldType.VOID, voidCreateItem != null ? XMaterial.matchXMaterial(voidCreateItem) : null);

        ItemStack normalDefaultItem = inventory.getItem(20);
        ItemStack flatDefaultItem = inventory.getItem(21);
        ItemStack netherDefaultItem = inventory.getItem(22);
        ItemStack endDefaultItem = inventory.getItem(23);
        ItemStack voidDefaultItem = inventory.getItem(24);
        ItemStack importedDefaultItem = inventory.getItem(25);

        inventoryUtils.setDefaultItem(WorldType.NORMAL, normalDefaultItem != null ? XMaterial.matchXMaterial(normalDefaultItem) : null);
        inventoryUtils.setDefaultItem(WorldType.FLAT, flatDefaultItem != null ? XMaterial.matchXMaterial(flatDefaultItem) : null);
        inventoryUtils.setDefaultItem(WorldType.NETHER, netherDefaultItem != null ? XMaterial.matchXMaterial(netherDefaultItem) : null);
        inventoryUtils.setDefaultItem(WorldType.END, endDefaultItem != null ? XMaterial.matchXMaterial(endDefaultItem) : null);
        inventoryUtils.setDefaultItem(WorldType.VOID, voidDefaultItem != null ? XMaterial.matchXMaterial(voidDefaultItem) : null);
        inventoryUtils.setDefaultItem(WorldType.IMPORTED, importedDefaultItem != null ? XMaterial.matchXMaterial(importedDefaultItem) : null);

        ItemStack notStartedStatusItem = inventory.getItem(29);
        ItemStack inProgressStatusItem = inventory.getItem(30);
        ItemStack almostFinishedStatusItem = inventory.getItem(31);
        ItemStack finishedStatusItem = inventory.getItem(32);
        ItemStack archiveStatusItem = inventory.getItem(33);
        ItemStack hiddenStatusItem = inventory.getItem(34);

        inventoryUtils.setStatusItem(WorldStatus.NOT_STARTED, notStartedStatusItem != null ? XMaterial.matchXMaterial(notStartedStatusItem) : null);
        inventoryUtils.setStatusItem(WorldStatus.IN_PROGRESS, inProgressStatusItem != null ? XMaterial.matchXMaterial(inProgressStatusItem) : null);
        inventoryUtils.setStatusItem(WorldStatus.ALMOST_FINISHED, almostFinishedStatusItem != null ? XMaterial.matchXMaterial(almostFinishedStatusItem) : null);
        inventoryUtils.setStatusItem(WorldStatus.FINISHED, finishedStatusItem != null ? XMaterial.matchXMaterial(finishedStatusItem) : null);
        inventoryUtils.setStatusItem(WorldStatus.ARCHIVE, archiveStatusItem != null ? XMaterial.matchXMaterial(archiveStatusItem) : null);
        inventoryUtils.setStatusItem(WorldStatus.HIDDEN, hiddenStatusItem != null ? XMaterial.matchXMaterial(hiddenStatusItem) : null);
    }
}