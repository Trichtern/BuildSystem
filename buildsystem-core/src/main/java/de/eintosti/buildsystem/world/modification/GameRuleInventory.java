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
import de.eintosti.buildsystem.version.gamerules.GameRules;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.UUID;

public class GameRuleInventory implements Listener {

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    public GameRuleInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openInventory(Player player, BuildWorld buildWorld) {
        World bukkitWorld = Bukkit.getWorld(buildWorld.getName());

        Inventory inventory = plugin.getGameRules().getInventory(player, bukkitWorld);
        fillGuiWithGlass(player, inventory);

        player.openInventory(inventory);
    }

    private void fillGuiWithGlass(Player player, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!isValidSlot(i)) {
                inventoryUtils.addGlassPane(plugin, player, inventory, i);
            }
        }

        UUID playerUUID = player.getUniqueId();
        GameRules gameRules = plugin.getGameRules();
        int numGameRules = gameRules.getNumGameRules();
        int invIndex = gameRules.getInvIndex(playerUUID);

        if (numGameRules > 1 && invIndex > 0) {
            inventoryUtils.addUrlSkull(inventory, 36, Messages.getString("gui_previous_page", player), "f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2");
        } else {
            inventoryUtils.addGlassPane(plugin, player, inventory, 36);
        }

        if (numGameRules > 1 && invIndex < (numGameRules - 1)) {
            inventoryUtils.addUrlSkull(inventory, 44, Messages.getString("gui_next_page", player), "d34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158");
        } else {
            inventoryUtils.addGlassPane(plugin, player, inventory, 44);
        }
    }

    private boolean isValidSlot(int slot) {
        return Arrays.stream(plugin.getGameRules().getSlots()).anyMatch(i -> i == slot);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "worldeditor_gamerules_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftBuildWorld buildWorld = plugin.getPlayerManager().getBuildPlayer(player).getCachedWorld();
        if (buildWorld == null) {
            player.closeInventory();
            Messages.sendMessage(player, "worlds_edit_error");
            return;
        }

        GameRules gameRules = plugin.getGameRules();

        switch (XMaterial.matchXMaterial(event.getCurrentItem())) {
            case PLAYER_HEAD:
                int slot = event.getSlot();
                if (slot == 36) {
                    gameRules.decrementInv(player);
                } else if (slot == 44) {
                    gameRules.incrementInv(player);
                }
                break;

            case FILLED_MAP:
            case MAP:
                World bukkitWorld = Bukkit.getWorld(buildWorld.getName());
                gameRules.toggleGameRule(event, bukkitWorld);
                break;

            default:
                XSound.BLOCK_CHEST_OPEN.play(player);
                plugin.getEditInventory().openInventory(player, buildWorld);
                return;
        }

        XSound.ENTITY_CHICKEN_EGG.play(player);
        openInventory(player, buildWorld);
    }
}