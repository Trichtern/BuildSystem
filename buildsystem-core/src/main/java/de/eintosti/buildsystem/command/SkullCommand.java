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
package de.eintosti.buildsystem.command;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.util.InventoryUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

public class SkullCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    public SkullCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getCommand("skull").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildsystem.skull")) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        switch (args.length) {
            case 0:
                player.getInventory().addItem(inventoryUtils.getSkull("§b" + player.getName(), player.getName()));
                Messages.sendMessage(player, "skull_player_received", new AbstractMap.SimpleEntry<>("%player%", player.getName()));
                break;
            case 1:
                String skullName = args[0];
                if (skullName.length() > 16) {
                    ItemStack customSkull = inventoryUtils.getUrlSkull(Messages.getString("custom_skull_item", player), skullName);
                    player.getInventory().addItem(customSkull);
                    Messages.sendMessage(player, "skull_custom_received");
                } else {
                    player.getInventory().addItem(inventoryUtils.getSkull("§b" + skullName, skullName));
                    Messages.sendMessage(player, "skull_player_received", new AbstractMap.SimpleEntry<>("%player%", skullName));
                }
                break;
            default:
                Messages.sendMessage(player, "skull_usage");
                break;
        }
        return true;
    }
}