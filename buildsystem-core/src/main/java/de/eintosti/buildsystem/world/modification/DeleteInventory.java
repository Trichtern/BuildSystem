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
package de.eintosti.buildsystem.world.modification;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.AbstractMap;

public class DeleteInventory implements Listener {

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    public DeleteInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory getInventory(Player player, BuildWorld buildWorld) {
        Inventory inventory = Bukkit.createInventory(null, 27, Messages.getString("delete_title", player));
        fillGuiWithGlass(inventory);

        inventoryUtils.addItemStack(inventory, 11, XMaterial.LIME_DYE,
                Messages.getString("delete_world_confirm", player)
        );
        inventoryUtils.addItemStack(inventory, 13, XMaterial.FILLED_MAP,
                Messages.getString("delete_world_name", player, new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName())),
                Messages.getStringList("delete_world_name_lore", player)
        );
        inventoryUtils.addItemStack(inventory, 15, XMaterial.RED_DYE,
                Messages.getString("delete_world_cancel", player)
        );

        return inventory;
    }

    public void openInventory(Player player, BuildWorld buildWorld) {
        player.openInventory(getInventory(player, buildWorld));
    }

    private void fillGuiWithGlass(Inventory inventory) {
        final int[] greenSlots = new int[]{0, 1, 2, 3, 9, 10, 12, 18, 19, 20, 21};
        final int[] blackSlots = new int[]{4, 22};
        final int[] redSlots = new int[]{5, 6, 7, 8, 14, 16, 17, 23, 24, 25, 26};

        for (int slot : greenSlots) {
            inventoryUtils.addItemStack(inventory, slot, XMaterial.LIME_STAINED_GLASS_PANE, "§f");
        }
        for (int slot : blackSlots) {
            inventoryUtils.addItemStack(inventory, slot, XMaterial.BLACK_STAINED_GLASS_PANE, "§f");
        }
        for (int slot : redSlots) {
            inventoryUtils.addItemStack(inventory, slot, XMaterial.RED_STAINED_GLASS_PANE, "§f");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "delete_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        BuildWorld buildWorld = plugin.getPlayerManager().getBuildPlayer(player).getCachedWorld();
        if (buildWorld == null) {
            Messages.sendMessage(player, "worlds_delete_error");
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();
        if (slot == 11) {
            XSound.ENTITY_PLAYER_LEVELUP.play(player);
            player.closeInventory();
            plugin.getWorldManager().deleteWorld(player, buildWorld);
        } else if (slot == 15) {
            XSound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR.play(player);
            player.closeInventory();
            Messages.sendMessage(player, "worlds_delete_canceled", new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()));

        }
    }
}