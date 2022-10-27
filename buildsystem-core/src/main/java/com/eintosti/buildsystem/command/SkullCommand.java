/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.command;

import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.InventoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author einTosti
 */
public class SkullCommand implements CommandExecutor {

    private final BuildSystem plugin;
    private final InventoryManager inventoryManager;

    public SkullCommand(BuildSystem plugin) {
        this.plugin = plugin;
        this.inventoryManager = plugin.getInventoryManager();
        plugin.getCommand("skull").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(plugin.getString("sender_not_player"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildsystem.skull")) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        switch (args.length) {
            case 0:
                player.getInventory().addItem(inventoryManager.getSkull("§b" + player.getName(), player.getName()));
                player.sendMessage(plugin.getString("skull_player_received").replace("%player%", player.getName()));
                break;
            case 1:
                String skullName = args[0];
                if (skullName.length() > 16) {
                    ItemStack customSkull = inventoryManager.getUrlSkull(plugin.getString("custom_skull_item"), skullName);
                    player.getInventory().addItem(customSkull);
                    player.sendMessage(plugin.getString("skull_custom_received"));
                } else {
                    player.getInventory().addItem(inventoryManager.getSkull("§b" + skullName, skullName));
                    player.sendMessage(plugin.getString("skull_player_received").replace("%player%", skullName));
                }
                break;
            default:
                player.sendMessage(plugin.getString("skull_usage"));
                break;
        }
        return true;
    }
}