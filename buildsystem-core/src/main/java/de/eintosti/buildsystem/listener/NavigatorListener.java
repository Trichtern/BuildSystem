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
import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.settings.NavigatorType;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.navigator.ArmorStandManager;
import de.eintosti.buildsystem.navigator.settings.NavigatorInventoryType;
import de.eintosti.buildsystem.player.BuildPlayerManager;
import de.eintosti.buildsystem.player.CachedValues;
import de.eintosti.buildsystem.settings.CraftSettings;
import de.eintosti.buildsystem.settings.SettingsManager;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class NavigatorListener implements Listener {

    private static final double MAX_HEIGHT = 2.074631929397583;
    private static final double MIN_HEIGHT = 1.4409877061843872;

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;

    private final ArmorStandManager armorStandManager;
    private final InventoryUtils inventoryUtils;
    private final BuildPlayerManager playerManager;
    private final SettingsManager settingsManager;
    private final BuildWorldManager worldManager;

    public NavigatorListener(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();

        this.armorStandManager = plugin.getArmorStandManager();
        this.inventoryUtils = plugin.getInventoryUtil();
        this.playerManager = plugin.getPlayerManager();
        this.settingsManager = plugin.getSettingsManager();
        this.worldManager = plugin.getWorldManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called whenever the player interacts with the navigator item.
     *
     * @param event The event which calls this method
     */
    @EventHandler
    public void manageNavigatorItemInteraction(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        if (isCloseNavigatorItem(player, itemStack)) {
            event.setCancelled(true);
            playerManager.closeNavigator(player);
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            return;
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(itemStack);
        if (xMaterial != configValues.getNavigatorItem() || !itemMeta.getDisplayName().equals(Messages.getString("navigator_item", player))) {
            return;
        }

        if (!player.hasPermission("buildsystem.navigator.item")) {
            plugin.sendPermissionMessage(player);
            return;
        }

        event.setCancelled(true);
        openNavigator(player);
    }

    private void openNavigator(Player player) {
        CraftSettings settings = settingsManager.getSettings(player);

        if (settings.getNavigatorType() == NavigatorType.OLD) {
            plugin.getNavigatorInventory().openInventory(player);
            XSound.BLOCK_CHEST_OPEN.play(player);
        } else { // NEW
            if (playerManager.getOpenNavigator().contains(player)) {
                Messages.sendMessage(player, "worlds_navigator_open");
                return;
            }

            summonNewNavigator(player);

            String findItemName = Messages.getString("navigator_item", player);
            ItemStack replaceItem = inventoryUtils.getItemStack(XMaterial.BARRIER, Messages.getString("barrier_item", player));
            inventoryUtils.replaceItem(player, findItemName, configValues.getNavigatorItem(), replaceItem);
        }
    }

    private void summonNewNavigator(Player player) {
        CachedValues cachedValues = playerManager.getBuildPlayer(player).getCachedValues();
        cachedValues.saveWalkSpeed(player.getWalkSpeed());
        cachedValues.saveFlySpeed(player.getFlySpeed());

        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 250, false, false));

        armorStandManager.spawnArmorStands(player);
        playerManager.getOpenNavigator().add(player);
    }

    /**
     * Manages a player's interaction with the {@link NavigatorType#NEW} navigator.
     *
     * @param event The event which calls this method
     */
    @EventHandler
    public void manageNewNavigatorInteraction(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        disableArchivedWorlds(player, event);

        if (!playerManager.getOpenNavigator().contains(player) || entity.getType() != EntityType.ARMOR_STAND) {
            return;
        }

        if (isCloseNavigatorItem(player, player.getItemInHand())) {
            event.setCancelled(true);
            playerManager.closeNavigator(player);
            return;
        }

        String customName = entity.getCustomName();
        if (customName == null || !customName.contains(" × ")) {
            return;
        }

        event.setCancelled(true);
        Vector clickedPosition = event.getClickedPosition();
        if (clickedPosition.getY() > MIN_HEIGHT && clickedPosition.getY() < MAX_HEIGHT) {
            if (!customName.startsWith(player.getName())) {
                return;
            }

            NavigatorInventoryType inventoryType = NavigatorInventoryType.matchInventoryType(player, customName);
            if (inventoryType == null) {
                return;
            }

            switch (inventoryType) {
                case NAVIGATOR:
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getWorldsInventory().openInventory(player);
                    break;
                case ARCHIVE:
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getArchiveInventory().openInventory(player);
                    break;
                case PRIVATE:
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getPrivateInventory().openInventory(player);
                    break;
            }
        }
    }

    /**
     * Disables players from manipulating with armor stands which make up the {@link NavigatorType#NEW} navigator.
     *
     * @param event The event which calls this method
     */
    @EventHandler
    public void manageNewNavigatorManipulation(PlayerArmorStandManipulateEvent event) {
        ArmorStand armorStand = event.getRightClicked();
        String customName = armorStand.getCustomName();
        if (customName == null || !customName.contains(" × ")) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * Prevents players from dropping the item which is used to close the {@link NavigatorType#NEW} navigator.
     *
     * @param event The event which calls this method
     */
    @EventHandler
    public void preventBarrierDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!playerManager.getOpenNavigator().contains(player)) {
            return;
        }

        if (isCloseNavigatorItem(player, event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks if the given item is the item which is used to close the {@link NavigatorType#NEW} navigator.
     *
     * @param player    The player used to get the item name
     * @param itemStack The item stack to check
     * @return {@code true} if the item is the navigator close item, {@code false} otherwise
     */
    private boolean isCloseNavigatorItem(Player player, ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            return false;
        }

        if (XMaterial.matchXMaterial(itemStack) != XMaterial.BARRIER) {
            return false;
        }

        return itemMeta.getDisplayName().equals(Messages.getString("barrier_item", player));
    }

    /**
     * Cancels an event if the player is in an archived world.
     *
     * @param player      The player object
     * @param cancellable The event to cancel
     */
    private void disableArchivedWorlds(Player player, Cancellable cancellable) {
        World bukkitWorld = player.getWorld();
        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());
        if (buildWorld == null || buildWorld.getData().status().get() != WorldStatus.ARCHIVE) {
            return;
        }

        if (playerManager.isInBuildMode(player)) {
            cancellable.setCancelled(true);
        }
    }
}