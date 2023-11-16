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
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.player.BuildPlayer;
import de.eintosti.buildsystem.api.player.PlayerManager;
import de.eintosti.buildsystem.api.settings.DesignColor;
import de.eintosti.buildsystem.api.settings.NavigatorType;
import de.eintosti.buildsystem.api.settings.WorldSort;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.Visibility;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.config.PlayersConfig;
import de.eintosti.buildsystem.navigator.settings.BuildWorldDisplay;
import de.eintosti.buildsystem.navigator.settings.BuildWorldFilter;
import de.eintosti.buildsystem.navigator.settings.NavigatorInventoryType;
import de.eintosti.buildsystem.settings.CraftSettings;
import de.eintosti.buildsystem.settings.SettingsManager;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BuildPlayerManager implements PlayerManager {

    private static final double MIN_HEIGHT = -0.16453003708696978;
    private static final double MAX_HEIGHT = 0.16481381407766063;

    private final BuildSystemPlugin plugin;
    private final PlayersConfig playersConfig;
    private final ConfigValues configValues;

    private final Map<UUID, CraftBuildPlayer> buildPlayers;

    private final Set<Player> openNavigator;
    private final Set<UUID> buildModePlayers;

    public BuildPlayerManager(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.playersConfig = new PlayersConfig(plugin);
        this.configValues = plugin.getConfigValues();

        this.buildPlayers = new HashMap<>();

        this.openNavigator = new HashSet<>();
        this.buildModePlayers = new HashSet<>();

        initEntityChecker();
    }

    public CraftBuildPlayer createBuildPlayer(UUID uuid, CraftSettings settings) {
        CraftBuildPlayer buildPlayer = this.buildPlayers.getOrDefault(uuid, new CraftBuildPlayer(uuid, settings));
        this.buildPlayers.put(uuid, buildPlayer);
        return buildPlayer;
    }

    public CraftBuildPlayer createBuildPlayer(Player player) {
        return createBuildPlayer(player.getUniqueId(), new CraftSettings());
    }

    @Override
    public Collection<BuildPlayer> getBuildPlayers() {
        return new ArrayList<>(buildPlayers.values());
    }

    @Override
    public CraftBuildPlayer getBuildPlayer(UUID uuid) {
        return this.buildPlayers.get(uuid);
    }

    @Override
    public CraftBuildPlayer getBuildPlayer(Player player) {
        return this.buildPlayers.get(player.getUniqueId());
    }

    @Nullable
    public String getSelectedWorldName(Player player) {
        BuildWorld selectedWorld = getBuildPlayer(player.getUniqueId()).getCachedWorld();
        if (selectedWorld == null) {
            return null;
        }

        String selectedWorldName = selectedWorld.getName();
        if (selectedWorldName.length() > 17) {
            selectedWorldName = selectedWorldName.substring(0, 14) + "...";
        }
        return selectedWorldName;
    }

    public Set<Player> getOpenNavigator() {
        return openNavigator;
    }

    @Override
    public Set<UUID> getBuildModePlayers() {
        return buildModePlayers;
    }

    @Override
    public boolean isInBuildMode(Player player) {
        return buildModePlayers.contains(player.getUniqueId());
    }

    @Override
    public boolean canCreateWorld(Player player, Visibility visibility) {
        BuildWorldManager worldManager = plugin.getWorldManager();

        int maxWorldAmountConfig = configValues.getMaxWorldAmount(visibility);
        if (maxWorldAmountConfig >= 0 && worldManager.getBuildWorlds().size() >= maxWorldAmountConfig) {
            return false;
        }

        int maxWorldAmountPlayer = getMaxWorlds(player, visibility);
        return maxWorldAmountPlayer < 0 || worldManager.getBuildWorldsCreatedByPlayer(player, visibility).size() < maxWorldAmountPlayer;
    }

    @Override
    public int getMaxWorlds(Player player, Visibility visibility) {
        int max = -1;
        if (player.hasPermission(BuildSystemPlugin.ADMIN_PERMISSION)) {
            return max;
        }

        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String permissionString = permission.getPermission();
            String[] splitPermission = permissionString.split("\\.");

            if (splitPermission.length != 4) {
                continue;
            }

            if (!splitPermission[1].equalsIgnoreCase("create")) {
                continue;
            }

            if (!splitPermission[2].equalsIgnoreCase(visibility.name().toLowerCase())) {
                continue;
            }

            String amountString = splitPermission[3];
            if (amountString.equals("*")) {
                return -1;
            }

            try {
                int amount = Integer.parseInt(amountString);
                if (amount > max) {
                    max = amount;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return max;
    }

    public void forceUpdateSidebar(BuildWorld buildWorld) {
        if (!configValues.isScoreboard()) {
            return;
        }

        World bukkitWorld = Bukkit.getWorld(buildWorld.getName());
        if (bukkitWorld == null) {
            return;
        }

        bukkitWorld.getPlayers().forEach(this::forceUpdateSidebar);
    }

    public void forceUpdateSidebar(Player player) {
        SettingsManager settingsManager = plugin.getSettingsManager();
        if (!configValues.isScoreboard() || !settingsManager.getSettings(player).isScoreboard()) {
            return;
        }
        settingsManager.updateScoreboard(player);
    }

    public void giveNavigator(Player player) {
        if (!configValues.isGiveNavigatorOnJoin()) {
            return;
        }

        if (!player.hasPermission("buildsystem.navigator.item")) {
            return;
        }

        if (plugin.getInventoryUtil().inventoryContainsNavigator(player)) {
            return;
        }

        ItemStack itemStack = plugin.getInventoryUtil().getItemStack(configValues.getNavigatorItem(), Messages.getString("navigator_item", player));
        PlayerInventory playerInventory = player.getInventory();
        ItemStack slot8 = playerInventory.getItem(8);
        if (slot8 == null || slot8.getType() == XMaterial.AIR.parseMaterial()) {
            playerInventory.setItem(8, itemStack);
        } else {
            playerInventory.addItem(itemStack);
        }
    }

    public void closeNavigator(Player player) {
        if (!openNavigator.contains(player)) {
            return;
        }

        CraftBuildPlayer buildPlayer = getBuildPlayer(player.getUniqueId());
        buildPlayer.setLastLookedAt(null);
        plugin.getArmorStandManager().removeArmorStands(player);

        XSound.ENTITY_ITEM_BREAK.play(player);
        ActionBar.clearActionBar(player);
        replaceBarrier(player);

        CachedValues cachedValues = buildPlayer.getCachedValues();
        cachedValues.resetWalkSpeedIfPresent(player);
        cachedValues.resetFlySpeedIfPresent(player);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        openNavigator.remove(player);
    }

    private void replaceBarrier(Player player) {
        if (!player.hasPermission("buildsystem.navigator.item")) {
            return;
        }

        InventoryUtils inventoryUtils = plugin.getInventoryUtil();
        String findItemName = Messages.getString("barrier_item", player);
        ItemStack replaceItem = inventoryUtils.getItemStack(plugin.getConfigValues().getNavigatorItem(), Messages.getString("navigator_item", player));

        inventoryUtils.replaceItem(player, findItemName, XMaterial.BARRIER, replaceItem);
    }

    private void initEntityChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkForEntity, 0L, 1L);
    }

    private void checkForEntity() {
        for (Player player : openNavigator) {
            if (getEntityName(player).isEmpty()) {
                continue;
            }

            CraftBuildPlayer buildPlayer = getBuildPlayer(player.getUniqueId());
            double lookedPosition = player.getEyeLocation().getDirection().getY();
            if (lookedPosition >= MIN_HEIGHT && lookedPosition <= MAX_HEIGHT) {
                NavigatorInventoryType inventoryType = NavigatorInventoryType.matchInventoryType(player, getEntityName(player));
                NavigatorInventoryType lastLookedAt = buildPlayer.getLastLookedAt();

                if (lastLookedAt == null || lastLookedAt != inventoryType) {
                    buildPlayer.setLastLookedAt(inventoryType);
                    sendTypeInfo(player, inventoryType);
                }
            } else {
                ActionBar.clearActionBar(player);
                buildPlayer.setLastLookedAt(null);
            }
        }
    }

    @Nullable
    private <T extends Entity> T getTarget(Entity entity, Iterable<T> entities) {
        if (entity == null) {
            return null;
        }

        T target = null;
        final Location entityLocation = entity.getLocation();
        final double threshold = 0.5;

        for (T other : entities) {
            final Location otherLocation = other.getLocation();
            final Vector vector = otherLocation.toVector().subtract(entityLocation.toVector());

            if (entityLocation.getDirection().normalize().crossProduct(vector).lengthSquared() < threshold && vector.normalize().dot(entityLocation.getDirection().normalize()) >= 0) {
                if (target == null || target.getLocation().distanceSquared(entityLocation) > otherLocation.distanceSquared(entityLocation)) {
                    target = other;
                }
            }
        }

        return target;
    }

    @Nullable
    private Entity getTargetEntity(Entity entity) {
        return getTarget(entity, entity.getNearbyEntities(3, 3, 3));
    }

    @NotNull
    private String getEntityName(Player player) {
        Entity targetEntity = getTargetEntity(player);
        if (targetEntity == null || targetEntity.getType() != EntityType.ARMOR_STAND) {
            return "";
        }

        Entity entity = getTargetEntity(player);
        if (entity == null || entity.getCustomName() == null) {
            return "";
        }

        return entity.getCustomName();
    }

    private void sendTypeInfo(Player player, NavigatorInventoryType inventoryType) {
        if (inventoryType == null) {
            ActionBar.clearActionBar(player);
            return;
        }

        String message;
        switch (inventoryType) {
            case ARCHIVE:
                message = "new_navigator_world_archive";
                break;
            case PRIVATE:
                message = "new_navigator_private_worlds";
                break;
            default:
                message = "new_navigator_world_navigator";
                break;
        }

        ActionBar.sendActionBar(player, Messages.getString(message, player));
        XSound.ENTITY_CHICKEN_EGG.play(player);
    }

    public void save() {
        playersConfig.savePlayers(this.buildPlayers.values());
    }

    public void load() {
        FileConfiguration configuration = playersConfig.getFile();
        ConfigurationSection configurationSection = configuration.getConfigurationSection("players");
        if (configurationSection == null) {
            return;
        }

        Set<String> uuids = configurationSection.getKeys(false);
        uuids.forEach(uuid -> {
            CraftBuildPlayer buildPlayer = createBuildPlayer(
                    UUID.fromString(uuid),
                    loadSettings(configuration, "players." + uuid + ".settings.")
            );
            buildPlayer.setLogoutLocation(loadLogoutLocation(configuration, "players." + uuid + ".logout-location"));
        });
    }

    @Nullable
    private LogoutLocation loadLogoutLocation(FileConfiguration configuration, String pathPrefix) {
        String location = configuration.getString(pathPrefix);
        if (location == null || location.trim().equals("")) {
            return null;
        }

        String[] parts = location.split(":");
        if (parts.length != 6) {
            return null;
        }

        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new LogoutLocation(worldName, x, y, z, yaw, pitch);
    }

    private CraftSettings loadSettings(FileConfiguration configuration, String pathPrefix) {
        NavigatorType navigatorType = NavigatorType.valueOf(configuration.getString(pathPrefix + "type"));
        DesignColor glassColor = DesignColor.matchColor(configuration.getString(pathPrefix + "glass"));
        BuildWorldDisplay worldDisplay = loadWorldDisplay(configuration, pathPrefix + "world-display.");
        boolean clearInventory = configuration.getBoolean(pathPrefix + "clear-inventory", false);
        boolean disableInteract = configuration.getBoolean(pathPrefix + "disable-interact", false);
        boolean hidePlayers = configuration.getBoolean(pathPrefix + "hide-players", false);
        boolean instantPlaceSigns = configuration.getBoolean(pathPrefix + "instant-place-signs", false);
        boolean keepNavigator = configuration.getBoolean(pathPrefix + "keep-navigator", false);
        boolean nightVision = configuration.getBoolean(pathPrefix + "nightvision", false);
        boolean noClip = configuration.getBoolean(pathPrefix + "no-clip", false);
        boolean placePlants = configuration.getBoolean(pathPrefix + "place-plants", false);
        boolean scoreboard = configuration.getBoolean(pathPrefix + "scoreboard", true);
        boolean slabBreaking = configuration.getBoolean(pathPrefix + "slab-breaking", false);
        boolean spawnTeleport = configuration.getBoolean(pathPrefix + "spawn-teleport", true);
        boolean trapDoor = configuration.getBoolean(pathPrefix + "trapdoor", false);

        return new CraftSettings(
                navigatorType, glassColor, worldDisplay, clearInventory, disableInteract, hidePlayers, instantPlaceSigns,
                keepNavigator, nightVision, noClip, placePlants, scoreboard, slabBreaking, spawnTeleport, trapDoor
        );
    }

    private BuildWorldDisplay loadWorldDisplay(FileConfiguration configuration, String pathPrefix) {
        WorldSort worldSort = WorldSort.matchWorldSort(configuration.getString(pathPrefix + "sort", WorldSort.NAME_A_TO_Z.name()));
        BuildWorldFilter.Mode filterMode = BuildWorldFilter.Mode.valueOf(configuration.getString(pathPrefix + "filter.mode", BuildWorldFilter.Mode.NONE.name()));
        String filterText = configuration.getString(pathPrefix + "filter.text", "");
        return new BuildWorldDisplay(worldSort, new BuildWorldFilter(filterMode, filterText));
    }
}