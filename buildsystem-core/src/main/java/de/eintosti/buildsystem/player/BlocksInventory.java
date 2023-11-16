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

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.version.customblocks.CustomBlock;
import de.eintosti.buildsystem.version.util.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlocksInventory implements Listener {

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    public BlocksInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory getInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 45, Messages.getString("blocks_title", player));
        fillGuiWithGlass(player, inventory);

        setCustomBlock(inventory, player, 1, CustomBlock.FULL_OAK_BARCH);
        setCustomBlock(inventory, player, 2, CustomBlock.FULL_SPRUCE_BARCH);
        setCustomBlock(inventory, player, 3, CustomBlock.FULL_BIRCH_BARCH);
        setCustomBlock(inventory, player, 4, CustomBlock.FULL_JUNGLE_BARCH);
        setCustomBlock(inventory, player, 5, CustomBlock.FULL_ACACIA_BARCH);
        setCustomBlock(inventory, player, 6, CustomBlock.FULL_DARK_OAK_BARCH);

        setCustomBlock(inventory, player, 10, CustomBlock.RED_MUSHROOM);
        setCustomBlock(inventory, player, 11, CustomBlock.BROWN_MUSHROOM);
        setCustomBlock(inventory, player, 12, CustomBlock.FULL_MUSHROOM_STEM);
        setCustomBlock(inventory, player, 13, CustomBlock.MUSHROOM_STEM);
        setCustomBlock(inventory, player, 14, CustomBlock.MUSHROOM_BLOCK);

        setCustomBlock(inventory, player, 19, CustomBlock.SMOOTH_STONE);
        setCustomBlock(inventory, player, 20, CustomBlock.DOUBLE_STONE_SLAB);
        setCustomBlock(inventory, player, 21, CustomBlock.SMOOTH_SANDSTONE);
        setCustomBlock(inventory, player, 22, CustomBlock.SMOOTH_RED_SANDSTONE);

        setCustomBlock(inventory, player, 28, CustomBlock.POWERED_REDSTONE_LAMP);
        setCustomBlock(inventory, player, 29, CustomBlock.BURNING_FURNACE);
        setCustomBlock(inventory, player, 30, CustomBlock.PISTON_HEAD);
        setCustomBlock(inventory, player, 31, CustomBlock.COMMAND_BLOCK);
        setCustomBlock(inventory, player, 32, CustomBlock.BARRIER);
        setCustomBlock(inventory, player, 33, CustomBlock.INVISIBLE_ITEM_FRAME);

        setCustomBlock(inventory, player, 37, CustomBlock.MOB_SPAWNER);
        setCustomBlock(inventory, player, 38, CustomBlock.NETHER_PORTAL);
        setCustomBlock(inventory, player, 39, CustomBlock.END_PORTAL);
        setCustomBlock(inventory, player, 40, CustomBlock.DRAGON_EGG);
        setCustomBlock(inventory, player, 41, CustomBlock.DEBUG_STICK);

        return inventory;
    }

    private void setCustomBlock(Inventory inventory, Player player, int position, CustomBlock customBlock) {
        if (MinecraftVersion.getCurrent().isEqualOrHigherThan(customBlock.getVersion())) {
            inventoryUtils.addUrlSkull(inventory, position, Messages.getString(customBlock.getKey(), player), customBlock.getSkullUrl());
        }
    }

    public void openInventory(Player player) {
        player.openInventory(getInventory(player));
    }

    private void fillGuiWithGlass(Player player, Inventory inventory) {
        int[] glassSlots = {0, 8, 9, 17, 18, 26, 27, 35, 36, 44};
        for (int i : glassSlots) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "blocks_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        switch (event.getSlot()) {
            case 1:
                giveCustomBlock(player, CustomBlock.FULL_OAK_BARCH);
                break;
            case 2:
                giveCustomBlock(player, CustomBlock.FULL_SPRUCE_BARCH);
                break;
            case 3:
                giveCustomBlock(player, CustomBlock.FULL_BIRCH_BARCH);
                break;
            case 4:
                giveCustomBlock(player, CustomBlock.FULL_JUNGLE_BARCH);
                break;
            case 5:
                giveCustomBlock(player, CustomBlock.FULL_ACACIA_BARCH);
                break;
            case 6:
                giveCustomBlock(player, CustomBlock.FULL_DARK_OAK_BARCH);
                break;

            case 10:
                giveCustomBlock(player, CustomBlock.RED_MUSHROOM);
                break;
            case 11:
                giveCustomBlock(player, CustomBlock.BROWN_MUSHROOM);
                break;
            case 12:
                giveCustomBlock(player, CustomBlock.FULL_MUSHROOM_STEM);
                break;
            case 13:
                giveCustomBlock(player, CustomBlock.MUSHROOM_STEM);
                break;
            case 14:
                giveCustomBlock(player, CustomBlock.MUSHROOM_BLOCK);
                break;

            case 19:
                giveCustomBlock(player, CustomBlock.SMOOTH_STONE);
                break;
            case 20:
                giveCustomBlock(player, CustomBlock.DOUBLE_STONE_SLAB);
                break;
            case 21:
                giveCustomBlock(player, CustomBlock.SMOOTH_SANDSTONE);
                break;
            case 22:
                giveCustomBlock(player, CustomBlock.SMOOTH_RED_SANDSTONE);
                break;

            case 28:
                giveCustomBlock(player, CustomBlock.POWERED_REDSTONE_LAMP);
                break;
            case 29:
                giveCustomBlock(player, CustomBlock.BURNING_FURNACE);
                break;
            case 30:
                giveCustomBlock(player, CustomBlock.PISTON_HEAD);
                break;
            case 31:
                giveCustomBlock(player, CustomBlock.COMMAND_BLOCK);
                break;
            case 32:
                giveCustomBlock(player, CustomBlock.BARRIER, inventoryUtils.getItemStack(XMaterial.BARRIER, Messages.getString(CustomBlock.BARRIER.getKey(), player)));
                break;
            case 33:
                ItemStack itemStack = inventoryUtils.getItemStack(XMaterial.ITEM_FRAME, Messages.getString(CustomBlock.INVISIBLE_ITEM_FRAME.getKey(), player));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                // Inline imports to allow backwards compatibility
                itemMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "invisible-itemframe"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                itemStack.setItemMeta(itemMeta);
                giveCustomBlock(player, CustomBlock.INVISIBLE_ITEM_FRAME, itemStack);
                break;

            case 37:
                giveCustomBlock(player, CustomBlock.MOB_SPAWNER);
                break;
            case 38:
                giveCustomBlock(player, CustomBlock.NETHER_PORTAL);
                break;
            case 39:
                giveCustomBlock(player, CustomBlock.END_PORTAL);
                break;
            case 40:
                giveCustomBlock(player, CustomBlock.DRAGON_EGG);
                break;
            case 41:
                giveCustomBlock(player, CustomBlock.DEBUG_STICK, inventoryUtils.getItemStack(XMaterial.DEBUG_STICK, Messages.getString(CustomBlock.DEBUG_STICK.getKey(), player)));
                break;
        }
    }

    private void giveCustomBlock(Player player, CustomBlock customBlock, ItemStack itemStack) {
        if (MinecraftVersion.getCurrent().isEqualOrHigherThan(customBlock.getVersion())) {
            player.getInventory().addItem(itemStack);
        }
    }

    private void giveCustomBlock(Player player, CustomBlock customBlock) {
        giveCustomBlock(player, customBlock, inventoryUtils.getUrlSkull(Messages.getString(customBlock.getKey(), player), customBlock.getSkullUrl()));
    }
}