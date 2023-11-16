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
import de.eintosti.buildsystem.api.world.Builder;
import de.eintosti.buildsystem.command.subcommand.worlds.AddBuilderSubCommand;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.util.PaginatedInventory;
import de.eintosti.buildsystem.util.StringUtils;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;

public class BuilderInventory extends PaginatedInventory implements Listener {

    private static final int MAX_BUILDERS = 9;

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;

    private int numBuilders = 0;

    public BuilderInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory createInventory(BuildWorld buildWorld, Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Messages.getString("worldeditor_builders_title", player));
        fillGuiWithGlass(inventory, player);

        addCreatorInfoItem(inventory, buildWorld, player);
        addBuilderAddItem(inventory, buildWorld, player);

        inventoryUtils.addUrlSkull(inventory, 18, Messages.getString("gui_previous_page", player), "f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2");
        inventoryUtils.addUrlSkull(inventory, 26, Messages.getString("gui_next_page", player), "d34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158");

        return inventory;
    }

    private void addCreatorInfoItem(Inventory inventory, BuildWorld buildWorld, Player player) {
        String creatorName = buildWorld.getCreator();
        if (creatorName == null || creatorName.equalsIgnoreCase("-")) {
            inventoryUtils.addItemStack(inventory, 4, XMaterial.BARRIER, Messages.getString("worldeditor_builders_no_creator_item", player));
        } else {
            inventoryUtils.addSkull(inventory, 4, Messages.getString("worldeditor_builders_creator_item", player),
                    buildWorld.getCreator(), Messages.getString("worldeditor_builders_creator_lore", player, new AbstractMap.SimpleEntry<>("%creator%", buildWorld.getCreator()))
            );
        }
    }

    private void addBuilderAddItem(Inventory inventory, BuildWorld buildWorld, Player player) {
        UUID creatorId = buildWorld.getCreatorId();
        if ((creatorId != null && creatorId.equals(player.getUniqueId())) || player.hasPermission(BuildSystemPlugin.ADMIN_PERMISSION)) {
            inventoryUtils.addUrlSkull(inventory, 22, Messages.getString("worldeditor_builders_add_builder_item", player),
                    "3edd20be93520949e6ce789dc4f43efaeb28c717ee6bfcbbe02780142f716"
            );
        } else {
            inventoryUtils.addGlassPane(plugin, player, inventory, 22);
        }
    }

    private void addItems(BuildWorld buildWorld, Player player) {
        List<Builder> builders = buildWorld.getBuilders();
        this.numBuilders = builders.size();
        int numInventories = (numBuilders % MAX_BUILDERS == 0 ? numBuilders : numBuilders + 1) != 0 ? (numBuilders % MAX_BUILDERS == 0 ? numBuilders : numBuilders + 1) : 1;

        int index = 0;

        Inventory inventory = createInventory(buildWorld, player);

        inventories = new Inventory[numInventories];
        inventories[index] = inventory;

        int columnSkull = 9, maxColumnSkull = 17;
        for (Builder builder : builders) {
            String builderName = builder.getName();
            inventoryUtils.addSkull(inventory, columnSkull++,
                    Messages.getString("worldeditor_builders_builder_item", player, new AbstractMap.SimpleEntry<>("%builder%", builderName)),
                    builderName, Messages.getStringList("worldeditor_builders_builder_lore", player)
            );

            if (columnSkull > maxColumnSkull) {
                columnSkull = 9;
                inventory = createInventory(buildWorld, player);
                inventories[++index] = inventory;
            }
        }
    }

    public void openInventory(BuildWorld buildWorld, Player player) {
        player.openInventory(getInventory(buildWorld, player));
    }

    private Inventory getInventory(BuildWorld buildWorld, Player player) {
        addItems(buildWorld, player);
        return inventories[getInvIndex(player)];
    }

    private void fillGuiWithGlass(Inventory inventory, Player player) {
        for (int i = 0; i <= 8; i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
        for (int i = 18; i <= 26; i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "worldeditor_builders_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftBuildWorld buildWorld = plugin.getPlayerManager().getBuildPlayer(player).getCachedWorld();
        if (buildWorld == null) {
            player.closeInventory();
            Messages.sendMessage(player, "worlds_addbuilder_error");
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        Material material = itemStack.getType();
        if (material != XMaterial.PLAYER_HEAD.parseMaterial()) {
            XSound.BLOCK_CHEST_OPEN.play(player);
            plugin.getEditInventory().openInventory(player, buildWorld);
            return;
        }

        int slot = event.getSlot();
        switch (slot) {
            case 18:
                if (decrementInv(player, numBuilders, MAX_BUILDERS)) {
                    openInventory(buildWorld, player);
                }
                break;
            case 22:
                XSound.ENTITY_CHICKEN_EGG.play(player);
                new AddBuilderSubCommand(plugin, buildWorld.getName()).getAddBuilderInput(player, buildWorld, false);
                return;
            case 26:
                if (incrementInv(player, numBuilders, MAX_BUILDERS)) {
                    openInventory(buildWorld, player);
                }
                break;
            default:
                if (slot == 4) {
                    return;
                }
                if (!itemMeta.hasDisplayName()) {
                    return;
                }
                if (!event.isShiftClick()) {
                    return;
                }

                String template = Messages.getString("worldeditor_builders_builder_item", player, new AbstractMap.SimpleEntry<>("%builder%", ""));
                String builderName = StringUtils.difference(template, itemMeta.getDisplayName());
                UUID builderId = UUIDFetcher.getUUID(builderName);
                buildWorld.removeBuilder(builderId);

                XSound.ENTITY_ENDERMAN_TELEPORT.play(player);
                Messages.sendMessage(player, "worlds_removebuilder_removed", new AbstractMap.SimpleEntry<>("%builder%", builderName));
        }

        XSound.ENTITY_CHICKEN_EGG.play(player);
        player.openInventory(getInventory(buildWorld, player));
    }
}