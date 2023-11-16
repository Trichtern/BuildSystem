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
package de.eintosti.buildsystem.navigator.inventory;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.settings.WorldDisplay;
import de.eintosti.buildsystem.api.settings.WorldFilter;
import de.eintosti.buildsystem.api.settings.WorldSort;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.Visibility;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.navigator.settings.BuildWorldFilter;
import de.eintosti.buildsystem.settings.CraftSettings;
import de.eintosti.buildsystem.settings.SettingsManager;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.util.PaginatedInventory;
import de.eintosti.buildsystem.util.PlayerChatInput;
import de.eintosti.buildsystem.world.BuildWorldManager;
import de.eintosti.buildsystem.world.modification.CreateInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FilteredWorldsInventory extends PaginatedInventory implements Listener {

    private static final int MAX_WORLDS = 36;

    private final BuildSystemPlugin plugin;
    private final InventoryUtils inventoryUtils;
    private final SettingsManager settingsManager;
    private final BuildWorldManager worldManager;

    private final String inventoryName;
    private final String noWorldsText;
    private final Visibility visibility;
    private final Set<WorldStatus> validStatus;

    public FilteredWorldsInventory(BuildSystemPlugin plugin, String inventoryName, String noWorldsText, Visibility visibility, Set<WorldStatus> validStatus) {
        this.plugin = plugin;
        this.inventoryUtils = plugin.getInventoryUtil();
        this.settingsManager = plugin.getSettingsManager();
        this.worldManager = plugin.getWorldManager();

        this.inventoryName = inventoryName;
        this.noWorldsText = noWorldsText;
        this.visibility = visibility;
        this.validStatus = validStatus;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    protected Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, Messages.getString(inventoryName, player));

        int numOfPages = (numOfWorlds(player) / MAX_WORLDS) + (numOfWorlds(player) % MAX_WORLDS == 0 ? 0 : 1);
        inventoryUtils.fillMultiInvWithGlass(plugin, inventory, player, getInvIndex(player), numOfPages);

        addWorldSortItem(inventory, player);
        addWorldFilterItem(inventory, player);

        inventoryUtils.addUrlSkull(inventory, 52, Messages.getString("gui_previous_page", player), "86971dd881dbaf4fd6bcaa93614493c612f869641ed59d1c9363a3666a5fa6");
        inventoryUtils.addUrlSkull(inventory, 53, Messages.getString("gui_next_page", player), "f32ca66056b72863e98f7f32bd7d94c7a0d796af691c9ac3a9136331352288f9");

        return inventory;
    }

    /**
     * Gets the amount of worlds that are to be displayed in the inventory.
     *
     * @param player The player to show the inventory to
     * @return The amount of worlds
     */
    private int numOfWorlds(Player player) {
        return (int) worldManager.getBuildWorlds().stream()
                .filter(buildWorld -> isValidWorld(player, buildWorld))
                .count();
    }

    /**
     * Gets the visibility of the worlds that will be displayed.
     *
     * @return The visibility of the worlds that will be displayed
     */
    protected Visibility getVisibility() {
        return visibility;
    }

    public void openInventory(Player player) {
        player.openInventory(getInventory(player));
    }

    private Inventory getInventory(Player player) {
        addWorlds(player);
        return inventories[getInvIndex(player)];
    }

    private void addWorlds(Player player) {
        int numWorlds = numOfWorlds(player);
        int numInventories = (numWorlds % MAX_WORLDS == 0 ? numWorlds : numWorlds + 1) != 0 ? (numWorlds % MAX_WORLDS == 0 ? numWorlds : numWorlds + 1) : 1;

        inventories = new Inventory[numInventories];
        Inventory inventory = createInventory(player);

        int index = 0;
        inventories[index] = inventory;
        if (numWorlds == 0) {
            inventoryUtils.addUrlSkull(inventory, 22, Messages.getString(noWorldsText, player), "2e3f50ba62cbda3ecf5479b62fedebd61d76589771cc19286bf2745cd71e47c6");
            return;
        }

        int columnWorld = 9, maxColumnWorld = 44;
        for (BuildWorld buildWorld : inventoryUtils.getDisplayOrder(worldManager, plugin.getSettingsManager().getSettings(player))) {
            if (isValidWorld(player, buildWorld)) {
                inventoryUtils.addWorldItem(player, inventory, columnWorld++, buildWorld);
            }

            if (columnWorld > maxColumnWorld) {
                columnWorld = 9;
                inventory = createInventory(player);
                inventories[++index] = inventory;
            }
        }
    }

    private void addWorldSortItem(Inventory inventory, Player player) {
        CraftSettings settings = settingsManager.getSettings(player);
        WorldSort worldSort = settings.getWorldDisplay().getWorldSort();
        inventoryUtils.addItemStack(inventory, 45, XMaterial.BOOK,
                Messages.getString("world_sort_title", player),
                Messages.getStringList(worldSort.getLoreKey(), player)
        );
    }

    private void addWorldFilterItem(Inventory inventory, Player player) {
        CraftSettings settings = settingsManager.getSettings(player);
        WorldFilter worldFilter = settings.getWorldDisplay().getWorldFilter();

        List<String> lore = new ArrayList<>();
        lore.add(Messages.getString(worldFilter.getMode().getLoreKey(), player, new AbstractMap.SimpleEntry<>("%text%", worldFilter.getText())));
        lore.addAll(Messages.getStringList("world_filter_lore", player));

        inventoryUtils.addItemStack(inventory, 46, XMaterial.HOPPER, Messages.getString("world_filter_title", player), lore);
    }

    /**
     * Gets if the world should be shown to the player in the navigator.
     * <p>
     * The following logic is applied to determine the above:
     * <ul>
     *   <li>Does the player have the admin bypass permission?</li>
     *   <li>Is the player the creator of the world?</li>
     *   <li>Has the player been added as a builder?</li>
     *   <li>Does the player have the permission to see the world?</li>
     * </ul>
     *
     * @param player     The player who the world will be shown to
     * @param buildWorld The world to show
     * @return {@code true} if the world should be shown to the player in the navigator, {@code false} otherwise
     */
    private boolean isValidWorld(Player player, BuildWorld buildWorld) {
        WorldData worldData = buildWorld.getData();
        if (!worldManager.isCorrectVisibility(worldData.privateWorld().get(), visibility)) {
            return false;
        }

        if (!validStatus.contains(worldData.status().get())) {
            return false;
        }

        if (player.hasPermission(BuildSystemPlugin.ADMIN_PERMISSION)) {
            return true;
        }

        if (buildWorld.isCreator(player) || buildWorld.isBuilder(player)) {
            return true;
        }

        String worldPermission = worldData.permission().get();
        if (!worldPermission.equalsIgnoreCase("-") && !player.hasPermission(worldPermission)) {
            return false;
        }

        return Bukkit.getWorld(buildWorld.getName()) != null || !buildWorld.isLoaded();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, inventoryName)) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftSettings settings = settingsManager.getSettings(player);
        WorldDisplay worldDisplay = settings.getWorldDisplay();

        switch (event.getSlot()) {
            case 45:
                WorldSort newSort = event.isLeftClick() ? worldDisplay.getWorldSort().getNext() : worldDisplay.getWorldSort().getPrevious();
                worldDisplay.setWorldSort(newSort);
                openInventory(player);
                return;
            case 46:
                WorldFilter worldFilter = worldDisplay.getWorldFilter();
                WorldFilter.Mode currentMode = worldFilter.getMode();
                if (event.isShiftClick()) {
                    worldFilter.setMode(BuildWorldFilter.Mode.NONE);
                    worldFilter.setText("");
                    openInventory(player);
                } else if (event.isLeftClick()) {
                    new PlayerChatInput(plugin, player, "world_filter_title", input -> {
                        worldFilter.setText(input.replace("\"", ""));
                        openInventory(player);
                    });
                } else if (event.isRightClick()) {
                    worldFilter.setMode(currentMode.getNext());
                    openInventory(player);
                }
                return;
            case 49:
                if (itemStack.getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    plugin.getCreateInventory().openInventory(player, CreateInventory.Page.PREDEFINED, visibility);
                    return;
                }
                break;
            case 52:
                if (decrementInv(player, numOfWorlds(player), MAX_WORLDS)) {
                    openInventory(player);
                }
                return;
            case 53:
                if (incrementInv(player, numOfWorlds(player), MAX_WORLDS)) {
                    openInventory(player);
                }
                return;
        }

        inventoryUtils.manageInventoryClick(event, player, itemStack);
    }
}