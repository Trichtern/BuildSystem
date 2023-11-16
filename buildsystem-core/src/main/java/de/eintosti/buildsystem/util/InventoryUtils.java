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
package de.eintosti.buildsystem.util;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.settings.WorldDisplay;
import de.eintosti.buildsystem.api.settings.WorldSort;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.Builder;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.config.SetupConfig;
import de.eintosti.buildsystem.navigator.inventory.FilteredWorldsInventory;
import de.eintosti.buildsystem.navigator.inventory.NavigatorInventory;
import de.eintosti.buildsystem.navigator.settings.BuildWorldFilter;
import de.eintosti.buildsystem.player.BuildPlayerManager;
import de.eintosti.buildsystem.settings.CraftSettings;
import de.eintosti.buildsystem.settings.SettingsManager;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import de.eintosti.buildsystem.world.BuildWorldManager;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import de.eintosti.buildsystem.world.modification.EditInventory;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryUtils {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;
    private final SetupConfig setupConfig;

    private final BuildPlayerManager playerManager;

    private XMaterial normalCreateItem, flatCreateItem, netherCreateItem, endCreateItem, voidCreateItem, customCreateItem;
    private XMaterial normalDefaultItem, flatDefaultItem, netherDefaultItem, endDefaultItem, voidDefaultItem, importedDefaultItem;
    private XMaterial notStartedItem, inProgressItem, almostFinishedItem, finishedItem, archivedItem, hiddenItem;

    public InventoryUtils(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.setupConfig = new SetupConfig(plugin);

        this.playerManager = plugin.getPlayerManager();
    }

    public boolean isNavigator(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != configValues.getNavigatorItem().parseMaterial()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getDisplayName().equals(Messages.getString("navigator_item", player));
    }

    public boolean inventoryContainsNavigator(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (isNavigator(player, itemStack)) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getNavigatorSlots(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        List<Integer> navigatorSlots = new ArrayList<>();

        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack currentItem = playerInventory.getItem(i);
            if (isNavigator(player, currentItem)) {
                navigatorSlots.add(i);
            }
        }

        return navigatorSlots;
    }

    public void replaceItem(Player player, String findItemName, XMaterial findItemType, ItemStack replaceItem) {
        PlayerInventory playerInventory = player.getInventory();
        int slot = -1;

        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack currentItem = playerInventory.getItem(i);
            if (currentItem != null && currentItem.getType() == findItemType.parseMaterial()) {
                ItemMeta itemMeta = currentItem.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getDisplayName().equals(findItemName)) {
                        slot = i;
                    }
                }
            }
        }

        if (slot != -1) {
            playerInventory.setItem(slot, replaceItem);
        } else {
            ItemStack slot8 = playerInventory.getItem(8);
            if (slot8 == null || slot8.getType() == XMaterial.AIR.parseMaterial()) {
                playerInventory.setItem(8, replaceItem);
            } else {
                playerInventory.addItem(replaceItem);
            }
        }
    }

    public ItemStack getItemStack(XMaterial material, String displayName, List<String> lore) {
        ItemStack itemStack = material.parseItem();
        if (itemStack == null) {
            plugin.getLogger().warning("Unknown material found (" + material + ")");
            plugin.getLogger().warning("Defaulting back to BEDROCK.");
            itemStack = XMaterial.BEDROCK.parseItem();
        }
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.values());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getItemStack(XMaterial material, String displayName, String... lore) {
        return getItemStack(material, displayName, Arrays.asList(lore));
    }

    public void addItemStack(Inventory inventory, int position, XMaterial material, String displayName, List<String> lore) {
        ItemStack itemStack = getItemStack(material, displayName, lore);
        inventory.setItem(position, itemStack);
    }

    public void addItemStack(Inventory inventory, int position, XMaterial material, String displayName, String... lore) {
        addItemStack(inventory, position, material, displayName, Arrays.asList(lore));
    }

    public void addGlassPane(BuildSystemPlugin plugin, Player player, Inventory inventory, int position) {
        addItemStack(inventory, position, getColouredGlassPane(plugin, player), " ");
    }

    public ItemStack getSkull(String displayName, String skullOwner, List<String> lore) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta skullMeta = SkullUtils.applySkin(skull.getItemMeta(), skullOwner);

        skullMeta.setDisplayName(displayName);
        skullMeta.setLore(lore);
        skull.setItemMeta(skullMeta);

        skull.setItemMeta(skullMeta);
        return skull;
    }

    public ItemStack getSkull(String displayName, String skullOwner, String... lore) {
        return getSkull(displayName, skullOwner, Arrays.asList(lore));
    }

    public void addSkull(Inventory inventory, int position, String displayName, String skullOwner, List<String> lore) {
        inventory.setItem(position, getSkull(displayName, skullOwner, lore));
    }

    public void addSkull(Inventory inventory, int position, String displayName, String skullOwner, String... lore) {
        addSkull(inventory, position, displayName, skullOwner, Arrays.asList(lore));
    }

    public ItemStack getUrlSkull(String displayName, String url, List<String> lore) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta skullMeta = SkullUtils.applySkin(skull.getItemMeta(), url);

        skullMeta.setDisplayName(displayName);
        skullMeta.setLore(lore);
        skullMeta.addItemFlags(ItemFlag.values());
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public ItemStack getUrlSkull(String displayName, String url, String... lore) {
        return getUrlSkull(displayName, url, Arrays.asList(lore));
    }

    public void addUrlSkull(Inventory inventory, int position, String displayName, String url, List<String> lore) {
        inventory.setItem(position, getUrlSkull(displayName, url, lore));
    }

    public void addUrlSkull(Inventory inventory, int position, String displayName, String url, String... lore) {
        addUrlSkull(inventory, position, displayName, url, Arrays.asList(lore));
    }

    public boolean checkIfValidClick(InventoryClickEvent event, String titleKey) {
        if (!event.getView().getTitle().equals(Messages.getString(titleKey, (Player) event.getWhoClicked()))) {
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR || !itemStack.hasItemMeta()) {
            return false;
        }

        event.setCancelled(true);
        return true;
    }

    public void addWorldItem(Player player, Inventory inventory, int position, BuildWorld buildWorld) {
        String worldName = buildWorld.getName();
        String displayName = Messages.getString("world_item_title", player, new AbstractMap.SimpleEntry<>("%world%", worldName));
        XMaterial material = buildWorld.getData().material().get();

        if (material == XMaterial.PLAYER_HEAD) {
            addSkull(inventory, position, displayName, worldName, getLore(player, buildWorld));
        } else {
            addItemStack(inventory, position, material, displayName, getLore(player, buildWorld));
        }
    }

    /**
     * Manage clicking in a {@link FilteredWorldsInventory}.
     * <p>
     * If the clicked item is the icon of a {@link BuildWorld}, the click is managed by {@link InventoryUtils#manageWorldItemClick(InventoryClickEvent, Player, ItemMeta, CraftBuildWorld)}.
     * Otherwise, the {@link NavigatorInventory} is opened if the glass pane at the bottom of the inventory is clicked.
     *
     * @param event     The click event object to modify
     * @param player    The player who clicked
     * @param itemStack The clicked item
     */
    public void manageInventoryClick(InventoryClickEvent event, Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) {
            return;
        }

        int slot = event.getSlot();
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta.getDisplayName();

        if (slot == 22 &&
                displayName.equals(Messages.getString("world_navigator_no_worlds", player))
                || displayName.equals(Messages.getString("archive_no_worlds", player))
                || displayName.equals(Messages.getString("private_no_worlds", player))) {
            return;
        }

        if (slot >= 9 && slot <= 44) {
            CraftBuildWorld buildWorld = plugin.getWorldManager().getBuildWorld(getWorldName(player, displayName));
            manageWorldItemClick(event, player, itemMeta, buildWorld);
            return;
        }

        if (slot >= 45 && slot <= 53 && itemStack.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) {
            XSound.BLOCK_CHEST_OPEN.play(player);
            plugin.getNavigatorInventory().openInventory(player);
        }
    }

    /**
     * Manage the clicking of an {@link ItemStack} that represents a {@link BuildWorld}.
     * <p>
     * If the click is a...
     * <ul>
     *   <l>...left click, the world is loaded (if previously unloaded) and the player is teleported to said world.</li>
     *   <li>...right click and the player is permitted to edit the world {@link BuildWorldManager#isPermitted(Player, String, String)},
     *       the {@link EditInventory} for the world is opened for said player.
     *       If the player does not the the required permission the click is handled as a normal left click.</li>
     * </ul>
     *
     * @param event      The click event to modify
     * @param player     The player who clicked
     * @param itemMeta   The item meta of the clicked item
     * @param buildWorld The world represents by the clicked item
     */
    private void manageWorldItemClick(InventoryClickEvent event, Player player, ItemMeta itemMeta, CraftBuildWorld buildWorld) {
        if (event.isLeftClick() || !plugin.getWorldManager().isPermitted(player, WorldsTabComplete.WorldsArgument.EDIT.getPermission(), buildWorld.getName())) {
            performNonEditClick(player, itemMeta);
            return;
        }

        if (buildWorld.isLoaded()) {
            playerManager.getBuildPlayer(player).setCachedWorld(buildWorld);
            XSound.BLOCK_CHEST_OPEN.play(player);
            plugin.getEditInventory().openInventory(player, buildWorld);
        } else {
            player.closeInventory();
            XSound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR.play(player);
            Titles.sendTitle(player, 5, 70, 20, " ", Messages.getString("world_not_loaded", player));
        }
    }

    /**
     * A "non-edit click" is a click (i.e. a right click) which does not open the {@link EditInventory}.
     *
     * @param player   The player who clicked
     * @param itemMeta The item meta of the clicked item
     */
    private void performNonEditClick(Player player, ItemMeta itemMeta) {
        playerManager.closeNavigator(player);
        teleport(player, getWorldName(player, itemMeta.getDisplayName()));
    }

    private void teleport(Player player, String worldName) {
        BuildWorldManager worldManager = plugin.getWorldManager();
        CraftBuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            return;
        }
        worldManager.teleport(player, buildWorld);
    }

    /**
     * Parse the name of a world from the given input.
     *
     * @param player The player used to parse the placeholders
     * @param input  The string to parse the name from
     * @return The name of the world
     */
    private String getWorldName(Player player, String input) {
        String template = Messages.getString("world_item_title", player, new AbstractMap.SimpleEntry<>("%world%", ""));
        return StringUtils.difference(template, input);
    }

    /**
     * Gets the worlds in the order they are to be displayed.
     * First, the {@link BuildWorldFilter} is applied. Then, the list of worlds is sorted using the {@link WorldSort}.
     *
     * @param worldManager The world manager object
     * @param settings     The settings that provide the sorting method
     * @return The list of sorted worlds
     */
    public List<BuildWorld> getDisplayOrder(BuildWorldManager worldManager, CraftSettings settings) {
        WorldDisplay worldDisplay = settings.getWorldDisplay();
        List<BuildWorld> buildWorlds = worldManager.getBuildWorlds().stream()
                .filter(worldDisplay.getWorldFilter().apply())
                .collect(Collectors.toList());

        switch (worldDisplay.getWorldSort()) {
            default: // NAME_A_TO_Z
                buildWorlds.sort(Comparator.comparing(worldA -> worldA.getName().toLowerCase()));
                break;
            case NAME_Z_TO_A:
                buildWorlds.sort(Comparator.comparing(worldA -> worldA.getName().toLowerCase()));
                Collections.reverse(buildWorlds);
                break;
            case PROJECT_A_TO_Z:
                buildWorlds.sort(Comparator.comparing(worldA -> worldA.getData().project().get().toLowerCase()));
                break;
            case PROJECT_Z_TO_A:
                buildWorlds.sort(Comparator.comparing(worldA -> worldA.getData().project().get().toLowerCase()));
                Collections.reverse(buildWorlds);
                break;
            case STATUS_NOT_STARTED:
                buildWorlds.sort(new WorldStatusComparator());
                break;
            case STATUS_FINISHED:
                buildWorlds.sort(new WorldStatusComparator().reversed());
                break;
            case NEWEST_FIRST:
                buildWorlds.sort(new WorldCreationComparator().reversed());
                break;
            case OLDEST_FIRST:
                buildWorlds.sort(new WorldCreationComparator());
                break;
        }
        return buildWorlds;
    }

    /**
     * Get the lore which will be displayed in an inventory.
     *
     * @param player     The player whom the lore will be shown to
     * @param buildWorld The world the lore displays information about
     * @return The formatted lore
     */
    private List<String> getLore(Player player, BuildWorld buildWorld) {
        WorldData worldData = buildWorld.getData();
        Map.Entry<String, Object>[] placeholders = new Map.Entry[]{
                new AbstractMap.SimpleEntry<>("%status%", Messages.getDataString(worldData.status().get().getKey(), player)),
                new AbstractMap.SimpleEntry<>("%project%", worldData.project().get()),
                new AbstractMap.SimpleEntry<>("%permission%", worldData.permission().get()),
                new AbstractMap.SimpleEntry<>("%creator%", buildWorld.hasCreator() ? buildWorld.getCreator() : "-"),
                new AbstractMap.SimpleEntry<>("%creation%", Messages.formatDate(buildWorld.getCreationDate())),
                new AbstractMap.SimpleEntry<>("%lastedited%", Messages.formatDate(worldData.lastEdited().get())),
                new AbstractMap.SimpleEntry<>("%lastloaded%", Messages.formatDate(worldData.lastLoaded().get())),
                new AbstractMap.SimpleEntry<>("%lastunloaded%", Messages.formatDate(worldData.lastUnloaded().get()))
        };
        List<String> messageList = plugin.getWorldManager().isPermitted(player, WorldsTabComplete.WorldsArgument.EDIT.getPermission(), buildWorld.getName())
                ? Messages.getStringList("world_item_lore_edit", player, placeholders)
                : Messages.getStringList("world_item_lore_normal", player, placeholders);

        // Replace %builders% placeholder
        List<String> lore = new ArrayList<>();
        for (String line : messageList) {
            if (!line.contains("%builders%")) {
                lore.add(line);
                continue;
            }

            List<String> builders = formatBuilders(player, buildWorld);
            for (int i = 0; i < builders.size(); i++) {
                String builderString = builders.get(i).trim();
                if (builderString.isEmpty()) {
                    continue;
                }

                if (i == 0) {
                    builderString = line.replace("%builders%", builderString);
                }

                if (i == builders.size() - 1) {
                    builderString = builderString.substring(0, builderString.length() - 1);
                }

                lore.add(builderString);
            }
        }

        return lore;
    }

    /**
     * Format the {@code %builder%} placeholder which can be used by a lore.
     *
     * @param player     The player used to format the placeholders
     * @param buildWorld The world which provides the builders
     * @return The formatted list of builders which have been added to the given world
     * @see CraftBuildWorld#getBuildersInfo(Player)
     */
    private List<String> formatBuilders(Player player, BuildWorld buildWorld) {
        String template = Messages.getString("world_item_builders_builder_template", player);
        List<Builder> builders = buildWorld.getBuilders();

        List<String> builderNames = new ArrayList<>();
        if (builders.isEmpty()) {
            String string = template.replace("%builder%", "-").trim();
            builderNames.add(string);
        } else {
            String string = "";
            int buildersInLine = 0;
            for (Builder builder : builders) {
                buildersInLine++;
                string = string.concat(template.replace("%builder%", builder.getName()));
                if (buildersInLine == 3) {
                    builderNames.add(string.trim());
                    buildersInLine = 0;
                    string = "";
                }
            }
            builderNames.add(string.trim());
        }

        builderNames.removeIf(String::isEmpty);
        return builderNames;
    }

    public void fillMultiInvWithGlass(BuildSystemPlugin plugin, Inventory inventory, Player player, int currentPage, int numOfPages) {
        for (int i = 0; i <= 8; i++) {
            addGlassPane(plugin, player, inventory, i);
        }

        for (int i = 45; i <= 53; i++) {
            addGlassPane(plugin, player, inventory, i);
        }
    }

    public XMaterial getColouredGlass(BuildSystemPlugin plugin, Player player) {
        SettingsManager settingsManager = plugin.getSettingsManager();
        CraftSettings settings = settingsManager.getSettings(player);

        Optional<XMaterial> glass = XMaterial.matchXMaterial(settings.getDesignColor().name() + "_STAINED_GLASS");
        return glass.orElse(XMaterial.BLACK_STAINED_GLASS);
    }

    public XMaterial getColouredGlassPane(BuildSystemPlugin plugin, Player player) {
        SettingsManager settingsManager = plugin.getSettingsManager();
        CraftSettings settings = settingsManager.getSettings(player);

        Optional<XMaterial> glass = XMaterial.matchXMaterial(settings.getDesignColor().name() + "_STAINED_GLASS_PANE");
        return glass.orElse(XMaterial.BLACK_STAINED_GLASS_PANE);
    }

    public XMaterial getCreateItem(WorldType worldType) {
        XMaterial material;
        switch (worldType) {
            case NORMAL:
                material = this.normalCreateItem;
                if (material == null) {
                    material = XMaterial.OAK_LOG;
                }
                break;
            case FLAT:
                material = this.flatCreateItem;
                if (material == null) {
                    material = XMaterial.GRASS_BLOCK;
                }
                break;
            case NETHER:
                material = this.netherCreateItem;
                if (material == null) {
                    material = XMaterial.NETHERRACK;
                }
                break;
            case END:
                material = this.endCreateItem;
                if (material == null) {
                    material = XMaterial.END_STONE;
                }
                break;
            case VOID:
                material = this.voidCreateItem;
                if (material == null) {
                    material = XMaterial.GLASS;
                }
                break;
            case CUSTOM:
                material = this.customCreateItem;
                if (material == null) {
                    material = XMaterial.FILLED_MAP;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported world type: " + worldType.name());
        }
        return material;
    }

    public void setCreateItem(WorldType worldType, XMaterial material) {
        switch (worldType) {
            case NORMAL:
                this.normalCreateItem = material;
                break;
            case FLAT:
                this.flatCreateItem = material;
                break;
            case NETHER:
                this.netherCreateItem = material;
                break;
            case END:
                this.endCreateItem = material;
                break;
            case VOID:
                this.voidCreateItem = material;
                break;
            case CUSTOM:
                this.customCreateItem = material;
                break;
            default:
                throw new IllegalArgumentException("Unsupported world type: " + worldType.name());
        }
    }

    public XMaterial getDefaultItem(WorldType worldType) {
        XMaterial material;
        switch (worldType) {
            case NORMAL:
                material = this.normalDefaultItem;
                if (material == null) {
                    material = XMaterial.OAK_LOG;
                }
                break;
            case FLAT:
                material = this.flatDefaultItem;
                if (material == null) {
                    material = XMaterial.GRASS_BLOCK;
                }
                break;
            case NETHER:
                material = this.netherDefaultItem;
                if (material == null) {
                    material = XMaterial.NETHERRACK;
                }
                break;
            case END:
                material = this.endDefaultItem;
                if (material == null) {
                    material = XMaterial.END_STONE;
                }
                break;
            case VOID:
                material = this.voidDefaultItem;
                if (material == null) {
                    material = XMaterial.GLASS;
                }
                break;
            case IMPORTED:
                material = this.importedDefaultItem;
                if (material == null) {
                    material = XMaterial.FURNACE;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported world type: " + worldType.name());
        }
        return material;
    }

    public void setDefaultItem(WorldType worldType, XMaterial material) {
        switch (worldType) {
            case NORMAL:
                this.normalDefaultItem = material;
                break;
            case FLAT:
                this.flatDefaultItem = material;
                break;
            case NETHER:
                this.netherDefaultItem = material;
                break;
            case END:
                this.endDefaultItem = material;
                break;
            case VOID:
                this.voidDefaultItem = material;
                break;
            case IMPORTED:
                this.importedDefaultItem = material;
                break;
            default:
                throw new IllegalArgumentException("Unsupported world type: " + worldType.name());
        }
    }

    public XMaterial getStatusItem(WorldStatus worldStatus) {
        XMaterial material;
        switch (worldStatus) {
            case NOT_STARTED:
                material = this.notStartedItem;
                if (material == null) {
                    material = XMaterial.RED_DYE;
                }
                break;
            case IN_PROGRESS:
                material = this.inProgressItem;
                if (material == null) {
                    material = XMaterial.ORANGE_DYE;
                }
                break;
            case ALMOST_FINISHED:
                material = this.almostFinishedItem;
                if (material == null) {
                    material = XMaterial.LIME_DYE;
                }
                break;
            case FINISHED:
                material = this.finishedItem;
                if (material == null) {
                    material = XMaterial.GREEN_DYE;
                }
                break;
            case ARCHIVE:
                material = this.archivedItem;
                if (material == null) {
                    material = XMaterial.CYAN_DYE;
                }
                break;
            case HIDDEN:
                material = this.hiddenItem;
                if (material == null) {
                    material = XMaterial.BONE_MEAL;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported world status: " + worldStatus.name());
        }
        return material;
    }

    public void setStatusItem(WorldStatus worldStatus, XMaterial material) {
        switch (worldStatus) {
            case NOT_STARTED:
                this.notStartedItem = material;
                break;
            case IN_PROGRESS:
                this.inProgressItem = material;
                break;
            case ALMOST_FINISHED:
                this.almostFinishedItem = material;
                break;
            case FINISHED:
                this.finishedItem = material;
                break;
            case ARCHIVE:
                this.archivedItem = material;
                break;
            case HIDDEN:
                this.hiddenItem = material;
                break;
            default:
                throw new IllegalArgumentException("Unsupported world status: " + worldStatus.name());
        }
    }

    public void save() {
        this.setupConfig.saveCreateItem(WorldType.NORMAL, getCreateItem(WorldType.NORMAL));
        this.setupConfig.saveCreateItem(WorldType.FLAT, getCreateItem(WorldType.FLAT));
        this.setupConfig.saveCreateItem(WorldType.NETHER, getCreateItem(WorldType.NETHER));
        this.setupConfig.saveCreateItem(WorldType.END, getCreateItem(WorldType.END));
        this.setupConfig.saveCreateItem(WorldType.VOID, getCreateItem(WorldType.VOID));

        this.setupConfig.saveDefaultItem(WorldType.NORMAL, getDefaultItem(WorldType.NORMAL));
        this.setupConfig.saveDefaultItem(WorldType.FLAT, getDefaultItem(WorldType.FLAT));
        this.setupConfig.saveDefaultItem(WorldType.NETHER, getDefaultItem(WorldType.NETHER));
        this.setupConfig.saveDefaultItem(WorldType.END, getDefaultItem(WorldType.END));
        this.setupConfig.saveDefaultItem(WorldType.VOID, getDefaultItem(WorldType.VOID));
        this.setupConfig.saveDefaultItem(WorldType.IMPORTED, getDefaultItem(WorldType.IMPORTED));

        this.setupConfig.saveStatusItem(WorldStatus.NOT_STARTED, getStatusItem(WorldStatus.NOT_STARTED));
        this.setupConfig.saveStatusItem(WorldStatus.IN_PROGRESS, getStatusItem(WorldStatus.IN_PROGRESS));
        this.setupConfig.saveStatusItem(WorldStatus.ALMOST_FINISHED, getStatusItem(WorldStatus.ALMOST_FINISHED));
        this.setupConfig.saveStatusItem(WorldStatus.FINISHED, getStatusItem(WorldStatus.FINISHED));
        this.setupConfig.saveStatusItem(WorldStatus.ARCHIVE, getStatusItem(WorldStatus.ARCHIVE));
        this.setupConfig.saveStatusItem(WorldStatus.HIDDEN, getStatusItem(WorldStatus.HIDDEN));
    }

    public void loadTypes() {
        FileConfiguration configuration = setupConfig.getFile();
        if (configuration == null) {
            return;
        }

        ConfigurationSection configurationSection = configuration.getConfigurationSection("setup.type");
        if (configurationSection == null) {
            return;
        }

        Set<String> worldTypes = configurationSection.getKeys(false);
        if (worldTypes.isEmpty()) {
            return;
        }

        for (String worldType : worldTypes) {
            String createMaterialString = configuration.getString("setup.type." + worldType + ".create");
            if (createMaterialString != null) {
                Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(createMaterialString);
                xMaterial.ifPresent(material -> setCreateItem(WorldType.valueOf(worldType.toUpperCase()), material));
            }

            String defaultMaterialString = configuration.getString("setup.type." + worldType + ".default");
            if (defaultMaterialString != null) {
                Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(defaultMaterialString);
                xMaterial.ifPresent(material -> setDefaultItem(WorldType.valueOf(worldType.toUpperCase()), material));
            }
        }
    }

    public void loadStatus() {
        FileConfiguration configuration = setupConfig.getFile();
        if (configuration == null) {
            return;
        }

        ConfigurationSection configurationSection = configuration.getConfigurationSection("setup.status");
        if (configurationSection == null) {
            return;
        }

        Set<String> worldStatus = configurationSection.getKeys(false);
        if (worldStatus.isEmpty()) {
            return;
        }

        for (String status : worldStatus) {
            String statusString = configuration.getString("setup.status." + status);
            if (statusString != null) {
                Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(statusString);
                xMaterial.ifPresent(material -> setStatusItem(WorldStatus.valueOf(status.toUpperCase()), material));
            }
        }
    }

    private static class WorldCreationComparator implements Comparator<BuildWorld> {

        @Override
        public int compare(BuildWorld buildWorld1, BuildWorld buildWorld2) {
            return Long.compare(buildWorld1.getCreationDate(), buildWorld2.getCreationDate());
        }
    }

    private static class WorldStatusComparator implements Comparator<BuildWorld> {

        @Override
        public int compare(BuildWorld buildWorld1, BuildWorld buildWorld2) {
            return Integer.compare(buildWorld1.getData().status().get().getStage(), buildWorld2.getData().status().get().getStage());
        }
    }
}