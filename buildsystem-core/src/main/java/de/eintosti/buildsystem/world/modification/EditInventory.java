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
import com.google.common.collect.Sets;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.Visibility;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.command.subcommand.worlds.SetPermissionSubCommand;
import de.eintosti.buildsystem.command.subcommand.worlds.SetProjectSubCommand;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.player.BuildPlayerManager;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class EditInventory implements Listener {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;
    private final InventoryUtils inventoryUtils;
    private final BuildPlayerManager playerManager;

    public EditInventory(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.inventoryUtils = plugin.getInventoryUtil();
        this.playerManager = plugin.getPlayerManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public Inventory getInventory(Player player, CraftBuildWorld buildWorld) {
        Inventory inventory = Bukkit.createInventory(null, 54, Messages.getString("worldeditor_title", player));
        WorldData worldData = buildWorld.getData();

        fillGuiWithGlass(player, inventory);
        addBuildWorldInfoItem(player, inventory, buildWorld);

        addSettingsItem(player, inventory, 20, XMaterial.OAK_PLANKS, worldData.blockBreaking().get(), "worldeditor_blockbreaking_item", "worldeditor_blockbreaking_lore");
        addSettingsItem(player, inventory, 21, XMaterial.POLISHED_ANDESITE, worldData.blockPlacement().get(), "worldeditor_blockplacement_item", "worldeditor_blockplacement_lore");
        addSettingsItem(player, inventory, 22, XMaterial.SAND, worldData.physics().get(), "worldeditor_physics_item", "worldeditor_physics_lore");
        addTimeItem(player, inventory, buildWorld);
        addSettingsItem(player, inventory, 24, XMaterial.TNT, worldData.explosions().get(), "worldeditor_explosions_item", "worldeditor_explosions_lore");
        inventoryUtils.addItemStack(inventory, 29, XMaterial.DIAMOND_SWORD, Messages.getString("worldeditor_butcher_item", player),
                Messages.getStringList("worldeditor_butcher_lore", player)
        );
        addBuildersItem(player, inventory, buildWorld);
        addSettingsItem(player, inventory, 31, XMaterial.ARMOR_STAND, worldData.mobAi().get(), "worldeditor_mobai_item", "worldeditor_mobai_lore");
        addVisibilityItem(player, inventory, buildWorld);
        addSettingsItem(player, inventory, 33, XMaterial.TRIPWIRE_HOOK, worldData.blockInteractions().get(), "worldeditor_blockinteractions_item", "worldeditor_blockinteractions_lore");
        inventoryUtils.addItemStack(inventory, 38, XMaterial.FILLED_MAP, Messages.getString("worldeditor_gamerules_item", player),
                Messages.getStringList("worldeditor_gamerules_lore", player)
        );
        addDifficultyItem(player, inventory, buildWorld);
        inventoryUtils.addItemStack(inventory, 40, inventoryUtils.getStatusItem(worldData.status().get()), "worldeditor_status_item",
                Messages.getStringList("worldeditor_status_lore", player,
                        new AbstractMap.SimpleEntry<>("%status%", Messages.getDataString(buildWorld.getData().status().get().getKey(), player))
                )
        );
        inventoryUtils.addItemStack(inventory, 41, XMaterial.ANVIL, Messages.getString("worldeditor_project_item", player),
                Messages.getStringList("worldeditor_project_lore", player,
                        new AbstractMap.SimpleEntry<>("%project%", buildWorld.getData().project().get())
                )
        );
        inventoryUtils.addItemStack(inventory, 42, XMaterial.PAPER, Messages.getString("worldeditor_permission_item", player),
                Messages.getStringList("worldeditor_permission_lore", player,
                        new AbstractMap.SimpleEntry<>("%permission%", buildWorld.getData().permission().get())
                )
        );

        return inventory;
    }

    public void openInventory(Player player, CraftBuildWorld buildWorld) {
        player.openInventory(getInventory(player, buildWorld));
    }

    private void fillGuiWithGlass(Player player, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventoryUtils.addGlassPane(plugin, player, inventory, i);
        }
    }

    private void addBuildWorldInfoItem(Player player, Inventory inventory, BuildWorld buildWorld) {
        String displayName = Messages.getString("worldeditor_world_item", player, new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()));
        XMaterial material = buildWorld.getData().material().get();

        if (material == XMaterial.PLAYER_HEAD) {
            inventoryUtils.addSkull(inventory, 4, displayName, buildWorld.getName());
        } else {
            inventoryUtils.addItemStack(inventory, 4, material, displayName);
        }
    }

    private void addSettingsItem(Player player, Inventory inventory, int position, XMaterial material, boolean isEnabled, String displayNameKey, String loreKey) {
        ItemStack itemStack = material.parseItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(Messages.getString(displayNameKey, player));
            itemMeta.setLore(Messages.getStringList(loreKey, player));
            itemMeta.addItemFlags(ItemFlag.values());
        }

        itemStack.setItemMeta(itemMeta);
        if (isEnabled) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        inventory.setItem(position, itemStack);
    }

    private void addTimeItem(Player player, Inventory inventory, BuildWorld buildWorld) {
        World bukkitWorld = Bukkit.getWorld(buildWorld.getName());

        XMaterial xMaterial = XMaterial.WHITE_STAINED_GLASS;
        String value = Messages.getString("worldeditor_time_lore_unknown", player);

        switch (getWorldTime(bukkitWorld)) {
            case SUNRISE:
                xMaterial = XMaterial.ORANGE_STAINED_GLASS;
                value = Messages.getString("worldeditor_time_lore_sunrise", player);
                break;
            case NOON:
                xMaterial = XMaterial.YELLOW_STAINED_GLASS;
                value = Messages.getString("worldeditor_time_lore_noon", player);
                break;
            case NIGHT:
                xMaterial = XMaterial.BLUE_STAINED_GLASS;
                value = Messages.getString("worldeditor_time_lore_night", player);
                break;
        }

        inventoryUtils.addItemStack(inventory, 23, xMaterial, Messages.getString("worldeditor_time_item", player),
                Messages.getStringList("worldeditor_time_lore", player, new AbstractMap.SimpleEntry<>("%time%", value))
        );
    }

    public CraftBuildWorld.Time getWorldTime(World bukkitWorld) {
        if (bukkitWorld == null) {
            return CraftBuildWorld.Time.UNKNOWN;
        }

        int worldTime = (int) bukkitWorld.getTime();
        int noonTime = plugin.getConfigValues().getNoonTime();

        if (worldTime >= 0 && worldTime < noonTime) {
            return CraftBuildWorld.Time.SUNRISE;
        } else if (worldTime >= noonTime && worldTime < 13000) {
            return CraftBuildWorld.Time.NOON;
        } else {
            return CraftBuildWorld.Time.NIGHT;
        }
    }

    private void addBuildersItem(Player player, Inventory inventory, BuildWorld buildWorld) {
        UUID creatorId = buildWorld.getCreatorId();
        if ((creatorId != null && creatorId.equals(player.getUniqueId())) || player.hasPermission(BuildSystemPlugin.ADMIN_PERMISSION)) {
            addSettingsItem(player, inventory, 30, XMaterial.IRON_PICKAXE, buildWorld.getData().buildersEnabled().get(),
                    "worldeditor_builders_item", "worldeditor_builders_lore"
            );
        } else {
            inventoryUtils.addItemStack(inventory, 30, XMaterial.BARRIER,
                    Messages.getString("worldeditor_builders_not_creator_item", player),
                    Messages.getStringList("worldeditor_builders_not_creator_lore", player)
            );
        }
    }

    private void addVisibilityItem(Player player, Inventory inventory, BuildWorld buildWorld) {
        int slot = 32;
        String displayName = Messages.getString("worldeditor_visibility_item", player);
        boolean isPrivate = buildWorld.getData().privateWorld().get();

        if (!playerManager.canCreateWorld(player, Visibility.matchVisibility(isPrivate))) {
            inventoryUtils.addItemStack(inventory, slot, XMaterial.BARRIER, "§c§m" + ChatColor.stripColor(displayName));
            return;
        }

        XMaterial xMaterial = XMaterial.ENDER_EYE;
        List<String> lore = Messages.getStringList("worldeditor_visibility_lore_public", player);

        if (isPrivate) {
            xMaterial = XMaterial.ENDER_PEARL;
            lore = Messages.getStringList("worldeditor_visibility_lore_private", player);
        }

        inventoryUtils.addItemStack(inventory, slot, xMaterial, displayName, lore);
    }

    private void addDifficultyItem(Player player, Inventory inventory, CraftBuildWorld buildWorld) {
        XMaterial xMaterial;
        switch (buildWorld.getData().difficulty().get()) {
            case EASY:
                xMaterial = XMaterial.GOLDEN_HELMET;
                break;
            case NORMAL:
                xMaterial = XMaterial.IRON_HELMET;
                break;
            case HARD:
                xMaterial = XMaterial.DIAMOND_HELMET;
                break;
            default:
                xMaterial = XMaterial.LEATHER_HELMET;
                break;
        }

        inventoryUtils.addItemStack(inventory, 39, xMaterial,
                Messages.getString("worldeditor_difficulty_item", player),
                Messages.getStringList("worldeditor_difficulty_lore", player,
                        new AbstractMap.SimpleEntry<>("%difficulty%", buildWorld.getDifficultyName(player))
                )
        );
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryUtils.checkIfValidClick(event, "worldeditor_title")) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftBuildWorld buildWorld = plugin.getPlayerManager().getBuildPlayer(player).getCachedWorld();
        if (buildWorld == null) {
            player.closeInventory();
            Messages.sendMessage(player, "worlds_edit_error");
            return;
        }

        WorldData worldData = buildWorld.getData();
        switch (event.getSlot()) {
            case 20:
                if (hasPermission(player, "buildsystem.edit.breaking")) {
                    worldData.blockBreaking().set(!worldData.blockBreaking().get());
                }
                break;
            case 21:
                if (hasPermission(player, "buildsystem.edit.placement")) {
                    worldData.blockPlacement().set(!worldData.blockPlacement().get());
                }
                break;
            case 22:
                if (hasPermission(player, "buildsystem.edit.physics")) {
                    worldData.physics().set(!worldData.physics().get());
                }
                break;
            case 23:
                if (hasPermission(player, "buildsystem.edit.time")) {
                    changeTime(player, buildWorld);
                }
                break;
            case 24:
                if (hasPermission(player, "buildsystem.edit.explosions")) {
                    worldData.explosions().set(!worldData.explosions().get());
                }
                break;

            case 29:
                if (hasPermission(player, "buildsystem.edit.entities")) {
                    removeEntities(player, buildWorld);
                }
                return;
            case 30:
                if (itemStack.getType() == XMaterial.BARRIER.parseMaterial()) {
                    XSound.ENTITY_ITEM_BREAK.play(player);
                    return;
                }
                if (!hasPermission(player, "buildsystem.edit.builders")) {
                    return;
                }
                if (event.isRightClick()) {
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getBuilderInventory().openInventory(buildWorld, player);
                    return;
                }
                worldData.buildersEnabled().set(!worldData.buildersEnabled().get());
                break;
            case 31:
                if (hasPermission(player, "buildsystem.edit.mobai")) {
                    worldData.mobAi().set(!worldData.mobAi().get());
                }
                break;
            case 32:
                if (itemStack.getType() == XMaterial.BARRIER.parseMaterial()) {
                    XSound.ENTITY_ITEM_BREAK.play(player);
                    return;
                }
                if (!hasPermission(player, "buildsystem.edit.visibility")) {
                    return;
                }
                worldData.privateWorld().set(!worldData.privateWorld().get());
                break;
            case 33:
                if (hasPermission(player, "buildsystem.edit.interactions")) {
                    worldData.blockInteractions().set(!worldData.blockInteractions().get());
                }
                break;

            case 38:
                if (hasPermission(player, "buildsystem.edit.gamerules")) {
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getGameRules().resetInvIndex(player.getUniqueId());
                    plugin.getGameRuleInventory().openInventory(player, buildWorld);
                }
                return;
            case 39:
                if (hasPermission(player, "buildsystem.edit.difficulty")) {
                    buildWorld.cycleDifficulty();
                    buildWorld.getWorld().setDifficulty(buildWorld.getData().difficulty().get());
                }
                break;
            case 40:
                if (hasPermission(player, "buildsystem.edit.status")) {
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    plugin.getStatusInventory().openInventory(player);
                }
                return;
            case 41:
                if (hasPermission(player, "buildsystem.edit.project")) {
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    new SetProjectSubCommand(plugin, buildWorld.getName()).getProjectInput(player, buildWorld, false);
                }
                return;
            case 42:
                if (hasPermission(player, "buildsystem.edit.permission")) {
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    new SetPermissionSubCommand(plugin, buildWorld.getName()).getPermissionInput(player, buildWorld, false);
                }
                return;

            default:
                return;
        }

        XSound.ENTITY_CHICKEN_EGG.play(player);
        openInventory(player, buildWorld);
    }

    private boolean hasPermission(Player player, String permission) {
        if (player.hasPermission(permission)) {
            return true;
        }
        player.closeInventory();
        plugin.sendPermissionMessage(player);
        XSound.ENTITY_ITEM_BREAK.play(player);
        return false;
    }

    private void changeTime(Player player, CraftBuildWorld buildWorld) {
        World bukkitWorld = Bukkit.getWorld(buildWorld.getName());
        if (bukkitWorld == null) {
            return;
        }

        CraftBuildWorld.Time time = getWorldTime(bukkitWorld);
        switch (time) {
            case SUNRISE:
                bukkitWorld.setTime(configValues.getNoonTime());
                break;
            case NOON:
                bukkitWorld.setTime(configValues.getNightTime());
                break;
            case NIGHT:
                bukkitWorld.setTime(configValues.getSunriseTime());
                break;
        }

        openInventory(player, buildWorld);
    }

    private void removeEntities(Player player, BuildWorld buildWorld) {
        World bukkitWorld = Bukkit.getWorld(buildWorld.getName());
        if (bukkitWorld == null) {
            return;
        }

        AtomicInteger entitiesRemoved = new AtomicInteger();
        bukkitWorld.getEntities().stream()
                .filter(entity -> !IGNORED_ENTITIES.contains(entity.getType()))
                .forEach(entity -> {
                    entity.remove();
                    entitiesRemoved.incrementAndGet();
                });

        player.closeInventory();
        Messages.sendMessage(player, "worldeditor_butcher_removed", new AbstractMap.SimpleEntry<>("%amount%", entitiesRemoved.get()));
    }

    private static final Set<EntityType> IGNORED_ENTITIES = Sets.newHashSet(
            EntityType.ARMOR_STAND,
            EntityType.ENDER_CRYSTAL,
            EntityType.ITEM_FRAME,
            EntityType.FALLING_BLOCK,
            EntityType.MINECART,
            EntityType.MINECART_CHEST,
            EntityType.MINECART_COMMAND,
            EntityType.MINECART_FURNACE,
            EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER,
            EntityType.MINECART_TNT,
            EntityType.PLAYER
    );
}