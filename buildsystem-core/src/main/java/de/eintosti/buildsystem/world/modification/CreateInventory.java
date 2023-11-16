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
import de.eintosti.buildsystem.api.world.data.Visibility;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.util.PaginatedInventory;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileFilter;
import java.util.AbstractMap;

public class CreateInventory extends PaginatedInventory implements Listener {

    private static final int MAX_TEMPLATES = 5;

    private final BuildSystemPlugin plugin;
    private final BuildWorldManager worldManager;
    private final InventoryUtils inventoryUtils;

    private int numTemplates = 0;
    private Visibility visibility;
    private boolean createPrivateWorld;

    public CreateInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        this.worldManager = plugin.getWorldManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory getInventory(Player player, Page page) {
        Inventory inventory = Bukkit.createInventory(null, 45, Messages.getString("create_title", player));
        fillGuiWithGlass(player, inventory, page);

        addPageItem(inventory, page, Page.PREDEFINED, inventoryUtils.getUrlSkull(Messages.getString("create_predefined_worlds", player), "2cdc0feb7001e2c10fd5066e501b87e3d64793092b85a50c856d962f8be92c78"));
        addPageItem(inventory, page, Page.GENERATOR, inventoryUtils.getUrlSkull(Messages.getString("create_generators", player), "b2f79016cad84d1ae21609c4813782598e387961be13c15682752f126dce7a"));
        addPageItem(inventory, page, Page.TEMPLATES, inventoryUtils.getUrlSkull(Messages.getString("create_templates", player), "d17b8b43f8c4b5cfeb919c9f8fe93f26ceb6d2b133c2ab1eb339bd6621fd309c"));

        switch (page) {
            case PREDEFINED:
                addPredefinedWorldItem(player, inventory, 29, WorldType.NORMAL, Messages.getString("create_normal_world", player));
                addPredefinedWorldItem(player, inventory, 30, WorldType.FLAT, Messages.getString("create_flat_world", player));
                addPredefinedWorldItem(player, inventory, 31, WorldType.NETHER, Messages.getString("create_nether_world", player));
                addPredefinedWorldItem(player, inventory, 32, WorldType.END, Messages.getString("create_end_world", player));
                addPredefinedWorldItem(player, inventory, 33, WorldType.VOID, Messages.getString("create_void_world", player));
                break;
            case GENERATOR:
                inventoryUtils.addUrlSkull(inventory, 31, Messages.getString("create_generators_create_world", player), "3edd20be93520949e6ce789dc4f43efaeb28c717ee6bfcbbe02780142f716");
                break;
            case TEMPLATES:
                // Template stuff is done during inventory open
                break;
        }

        return inventory;
    }

    public void openInventory(Player player, Page page, Visibility visibility) {
        this.visibility = visibility;
        this.createPrivateWorld = visibility == Visibility.PRIVATE;

        if (page == Page.TEMPLATES) {
            addTemplates(player, page);
            player.openInventory(inventories[getInvIndex(player)]);
        } else {
            player.openInventory(getInventory(player, page));
        }
    }

    private void addPageItem(Inventory inventory, Page currentPage, Page page, ItemStack itemStack) {
        if (currentPage == page) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
        inventory.setItem(page.getSlot(), itemStack);
    }

    private void addPredefinedWorldItem(Player player, Inventory inventory, int position, WorldType worldType, String displayName) {
        XMaterial material = inventoryUtils.getCreateItem(worldType);

        if (!player.hasPermission("buildsystem.create.type." + worldType.name().toLowerCase())) {
            material = XMaterial.BARRIER;
            displayName = "§c§m" + ChatColor.stripColor(displayName);
        }

        inventoryUtils.addItemStack(inventory, position, material, displayName);
    }

    private void addTemplates(Player player, Page page) {
        File[] templateFiles = new File(plugin.getDataFolder() + File.separator + "templates").listFiles(new TemplateFilter());

        int columnTemplate = 29, maxColumnTemplate = 33;
        int fileLength = templateFiles != null ? templateFiles.length : 0;
        this.numTemplates = (fileLength / MAX_TEMPLATES) + (fileLength % MAX_TEMPLATES == 0 ? 0 : 1);
        int numInventories = (numTemplates % MAX_TEMPLATES == 0 ? numTemplates : numTemplates + 1) != 0 ? (numTemplates % MAX_TEMPLATES == 0 ? numTemplates : numTemplates + 1) : 1;

        inventories = new Inventory[numInventories];
        Inventory inventory = getInventory(player, page);

        int index = 0;
        inventories[index] = inventory;
        if (numTemplates == 0) {
            for (int i = 29; i <= 33; i++) {
                inventoryUtils.addItemStack(inventory, i, XMaterial.BARRIER, Messages.getString("create_no_templates", player));
            }
            return;
        }

        if (templateFiles == null) {
            return;
        }

        for (File templateFile : templateFiles) {
            inventoryUtils.addItemStack(inventory, columnTemplate++, XMaterial.FILLED_MAP, Messages.getString("create_template", player, new AbstractMap.SimpleEntry<>("%template%", templateFile.getName())));
            if (columnTemplate > maxColumnTemplate) {
                columnTemplate = 29;
                inventory = getInventory(player, page);
                inventories[++index] = inventory;
            }
        }
    }

    private void fillGuiWithGlass(Player player, Inventory inventory, Page page) {
        for (int i = 0; i <= 28; i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
        for (int i = 34; i <= 44; i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }

        switch (page) {
            case GENERATOR:
                inventoryUtils.addGlassPane(plugin, player, inventory, 29);
                inventoryUtils.addGlassPane(plugin, player, inventory, 30);
                inventoryUtils.addGlassPane(plugin, player, inventory, 32);
                inventoryUtils.addGlassPane(plugin, player, inventory, 33);
                break;
            case TEMPLATES:
                inventoryUtils.addUrlSkull(inventory, 38, Messages.getString("gui_previous_page", player), "f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2");
                inventoryUtils.addUrlSkull(inventory, 42, Messages.getString("gui_next_page", player), "d34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158");
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "create_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CreateInventory.Page newPage = null;

        switch (event.getSlot()) {
            case 12:
                newPage = CreateInventory.Page.PREDEFINED;
                break;
            case 13:
                newPage = CreateInventory.Page.GENERATOR;
                break;
            case 14:
                newPage = CreateInventory.Page.TEMPLATES;
                break;
        }

        if (newPage != null) {
            openInventory(player, newPage, this.visibility);
            XSound.ENTITY_CHICKEN_EGG.play(player);
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        int slot = event.getSlot();

        switch (Page.getCurrentPage(inventory)) {
            case PREDEFINED: {
                WorldType worldType = null;

                switch (slot) {
                    case 29:
                        worldType = WorldType.NORMAL;
                        break;
                    case 30:
                        worldType = WorldType.FLAT;
                        break;
                    case 31:
                        worldType = WorldType.NETHER;
                        break;
                    case 32:
                        worldType = WorldType.END;
                        break;
                    case 33:
                        worldType = WorldType.VOID;
                        break;
                }

                if (worldType == null || !player.hasPermission("buildsystem.create.type." + worldType.name().toLowerCase())) {
                    XSound.ENTITY_ITEM_BREAK.play(player);
                    return;
                }

                worldManager.startWorldNameInput(player, worldType, null, createPrivateWorld);
                XSound.ENTITY_CHICKEN_EGG.play(player);
                break;
            }

            case GENERATOR: {
                if (slot == 31) {
                    worldManager.startWorldNameInput(player, WorldType.CUSTOM, null, createPrivateWorld);
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                }
                break;
            }

            case TEMPLATES: {
                ItemStack itemStack = event.getCurrentItem();
                if (itemStack == null) {
                    return;
                }

                XMaterial xMaterial = XMaterial.matchXMaterial(itemStack);
                switch (xMaterial) {
                    case FILLED_MAP:
                        worldManager.startWorldNameInput(player, WorldType.TEMPLATE, itemStack.getItemMeta().getDisplayName(), createPrivateWorld);
                        break;
                    case PLAYER_HEAD:
                        if (slot == 38 && !decrementInv(player, numTemplates, MAX_TEMPLATES)) {
                            return;
                        } else if (slot == 42 && !incrementInv(player, numTemplates, MAX_TEMPLATES)) {
                            return;
                        }
                        openInventory(player, CreateInventory.Page.TEMPLATES, visibility);
                        break;
                    default:
                        return;
                }
                break;
            }
        }
    }

    public enum Page {
        PREDEFINED(12),
        GENERATOR(13),
        TEMPLATES(14);

        private final int slot;

        Page(int slot) {
            this.slot = slot;
        }

        public static Page getCurrentPage(Inventory inventory) {
            for (Page page : Page.values()) {
                ItemStack itemStack = inventory.getItem(page.getSlot());
                if (itemStack != null && itemStack.containsEnchantment(Enchantment.DURABILITY)) {
                    return page;
                }
            }
            return Page.PREDEFINED;
        }

        public int getSlot() {
            return slot;
        }
    }

    private static class TemplateFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return file.isDirectory() && !file.isHidden();
        }
    }
}