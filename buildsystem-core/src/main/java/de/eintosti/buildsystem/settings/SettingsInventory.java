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
package de.eintosti.buildsystem.settings;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.settings.NavigatorType;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SettingsInventory implements Listener {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;

    private final InventoryUtils inventoryUtils;
    private final SettingsManager settingsManager;

    public SettingsInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();

        this.inventoryUtils = plugin.getInventoryUtil();
        this.settingsManager = plugin.getSettingsManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory getInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 45, Messages.getString("settings_title", player));
        fillGuiWithGlass(player, inventory);

        CraftSettings settings = settingsManager.getSettings(player);
        addDesignItem(inventory, player);
        addClearInventoryItem(inventory, player);
        addSettingsItem(player, inventory, 13, XMaterial.DIAMOND_AXE, settings.isDisableInteract(), "settings_disableinteract_item", "settings_disableinteract_lore");
        addSettingsItem(player, inventory, 14, XMaterial.ENDER_EYE, settings.isHidePlayers(), "settings_hideplayers_item", "settings_hideplayers_lore");
        addSettingsItem(player, inventory, 15, XMaterial.OAK_SIGN, settings.isInstantPlaceSigns(), "settings_instantplacesigns_item", "settings_instantplacesigns_lore");
        addSettingsItem(player, inventory, 20, XMaterial.SLIME_BLOCK, settings.isKeepNavigator(), "settings_keep_navigator_item", "settings_keep_navigator_lore");
        addSettingsItem(player, inventory, 21, configValues.getNavigatorItem(), settings.getNavigatorType() == NavigatorType.NEW, "settings_new_navigator_item", "settings_new_navigator_lore");
        addSettingsItem(player, inventory, 22, XMaterial.GOLDEN_CARROT, settings.isNightVision(), "settings_nightvision_item", "settings_nightvision_lore");
        addSettingsItem(player, inventory, 23, XMaterial.BRICKS, settings.isNoClip(), "settings_no_clip_item", "settings_no_clip_lore");
        addSettingsItem(player, inventory, 24, XMaterial.IRON_TRAPDOOR, settings.isOpenTrapDoors(), "settings_open_trapdoors_item", "settings_open_trapdoors_lore");
        addSettingsItem(player, inventory, 29, XMaterial.FERN, settings.isPlacePlants(), "settings_placeplants_item", "settings_placeplants_lore");
        addSettingsItem(player, inventory, 30, XMaterial.PAPER, settings.isScoreboard(), configValues.isScoreboard() ? "settings_scoreboard_item" : "settings_scoreboard_disabled_item", configValues.isScoreboard() ? "settings_scoreboard_lore" : "settings_scoreboard_disabled_lore");
        addSettingsItem(player, inventory, 31, getSlabBreakingMaterial(), settings.isSlabBreaking(), "settings_slab_breaking_item", "settings_slab_breaking_lore");
        addSettingsItem(player, inventory, 32, XMaterial.MAGMA_CREAM, settings.isSpawnTeleport(), "settings_spawnteleport_item", "settings_spawnteleport_lore");

        return inventory;
    }

    private XMaterial getSlabBreakingMaterial() {
        return XMaterial.supports(13) ? XMaterial.SMOOTH_STONE_SLAB : XMaterial.STONE_SLAB;
    }

    public void openInventory(Player player) {
        player.openInventory(getInventory(player));
    }

    private void fillGuiWithGlass(Player player, Inventory inventory) {
        for (int i = 0; i <= 44; i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
    }

    private void addSettingsItem(Player player, Inventory inventory, int position, XMaterial material, boolean enabled, String displayNameKey, String loreKey) {
        ItemStack itemStack = material.parseItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(Messages.getString(displayNameKey, player));
        itemMeta.setLore(Messages.getStringList(loreKey, player));
        itemMeta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(itemMeta);

        if (enabled) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        inventory.setItem(position, itemStack);
    }

    private void addClearInventoryItem(Inventory inventory, Player player) {
        CraftSettings settings = settingsManager.getSettings(player);
        XMaterial xMaterial = settings.isClearInventory() ? XMaterial.MINECART : XMaterial.CHEST_MINECART;
        addSettingsItem(player, inventory, 12, xMaterial, settings.isClearInventory(),
                "settings_clear_inventory_item", "settings_clear_inventory_lore"
        );
    }

    private void addDesignItem(Inventory inventory, Player player) {
        ItemStack itemStack = inventoryUtils.getItemStack(inventoryUtils.getColouredGlass(plugin, player), Messages.getString("settings_change_design_item", player));
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.setLore(Messages.getStringList("settings_change_design_lore", player));
        itemStack.setItemMeta(itemMeta);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        inventory.setItem(11, itemStack);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "settings_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftSettings settings = settingsManager.getSettings(player);

        switch (event.getSlot()) {
            case 11:
                plugin.getDesignInventory().openInventory(player);
                XSound.ENTITY_ITEM_PICKUP.play(player);
                return;
            case 12:
                settings.setClearInventory(!settings.isClearInventory());
                break;
            case 13:
                settings.setDisableInteract(!settings.isDisableInteract());
                break;
            case 14:
                settings.setHidePlayers(!settings.isHidePlayers());
                toggleHidePlayers(player, settings);
                break;
            case 15:
                settings.setInstantPlaceSigns(!settings.isInstantPlaceSigns());
                break;

            case 20:
                settings.setKeepNavigator(!settings.isKeepNavigator());
                break;
            case 21:
                if (settings.getNavigatorType().equals(NavigatorType.OLD)) {
                    settings.setNavigatorType(NavigatorType.NEW);
                } else {
                    settings.setNavigatorType(NavigatorType.OLD);
                    plugin.getArmorStandManager().removeArmorStands(player);
                    if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                }
                break;
            case 22:
                if (!settings.isNightVision()) {
                    settings.setNightVision(true);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                } else {
                    settings.setNightVision(false);
                    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    }
                }
                break;
            case 23:
                NoClipManager noClipManager = plugin.getNoClipManager();
                if (!settings.isNoClip()) {
                    settings.setNoClip(true);
                    noClipManager.startNoClip(player);
                } else {
                    settings.setNoClip(false);
                    noClipManager.stopNoClip(player.getUniqueId());
                }
                break;
            case 24:
                settings.setOpenTrapDoors(!settings.isOpenTrapDoors());
                break;

            case 29:
                settings.setPlacePlants(!settings.isPlacePlants());
                break;
            case 30:
                if (!configValues.isScoreboard()) {
                    XSound.ENTITY_ITEM_BREAK.play(player);
                    return;
                }
                if (settings.isScoreboard()) {
                    settings.setScoreboard(false);
                    settingsManager.stopScoreboard(player);
                } else {
                    settings.setScoreboard(true);
                    settingsManager.startScoreboard(player);
                    plugin.getPlayerManager().forceUpdateSidebar(player);
                }
                break;
            case 31:
                settings.setSlabBreaking(!settings.isSlabBreaking());
                break;
            case 32:
                settings.setSpawnTeleport(!settings.isSpawnTeleport());
                break;
            default:
                return;
        }

        XSound.ENTITY_ITEM_PICKUP.play(player);
        plugin.getSettingsInventory().openInventory(player);
    }

    @SuppressWarnings("deprecation")
    private void toggleHidePlayers(Player player, CraftSettings settings) {
        if (settings.isHidePlayers()) {
            Bukkit.getOnlinePlayers().forEach(player::hidePlayer);
        } else {
            Bukkit.getOnlinePlayers().forEach(player::showPlayer);
        }
    }
}