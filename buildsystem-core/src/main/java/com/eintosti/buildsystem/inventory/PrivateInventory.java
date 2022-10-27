/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.inventory;

import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.InventoryManager;
import com.eintosti.buildsystem.manager.PlayerManager;
import com.eintosti.buildsystem.object.world.data.WorldStatus;
import com.eintosti.buildsystem.util.Messages;
import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * @author einTosti
 */
public class PrivateInventory extends FilteredWorldsInventory {

    private final BuildSystem plugin;
    private final InventoryManager inventoryManager;
    private final PlayerManager playerManager;

    public PrivateInventory(BuildSystem plugin) {
        super(plugin, "private_title", "private_no_worlds", Visibility.PRIVATE,
                Sets.newHashSet(WorldStatus.NOT_STARTED, WorldStatus.IN_PROGRESS, WorldStatus.ALMOST_FINISHED, WorldStatus.FINISHED)
        );

        this.plugin = plugin;
        this.inventoryManager = plugin.getInventoryManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    protected Inventory createInventory(Player player) {
        Inventory inventory = super.createInventory(player);
        if (playerManager.canCreateWorld(player, super.getVisibility())) {
            addWorldCreateItem(inventory, player);
        }
        return inventory;
    }

    private void addWorldCreateItem(Inventory inventory, Player player) {
        if (player.hasPermission("buildsystem.create.private")) {
            inventoryManager.addUrlSkull(inventory, 49, Messages.getString("private_create_world"), "3edd20be93520949e6ce789dc4f43efaeb28c717ee6bfcbbe02780142f716");
        } else {
            inventoryManager.addGlassPane(plugin, player, inventory, 49);
        }
    }
}