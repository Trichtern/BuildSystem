/*
 * Copyright (c) 2018-2025, Thomas Meaney
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

import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.player.BuildPlayer;
import de.eintosti.buildsystem.player.LogoutLocation;
import de.eintosti.buildsystem.player.PlayerManager;
import de.eintosti.buildsystem.settings.Settings;
import de.eintosti.buildsystem.settings.SettingsManager;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.util.UpdateChecker;
import de.eintosti.buildsystem.world.BuildWorld;
import de.eintosti.buildsystem.world.SpawnManager;
import de.eintosti.buildsystem.world.WorldManager;
import de.eintosti.buildsystem.world.data.WorldData;
import de.eintosti.buildsystem.world.data.WorldStatus;
import io.papermc.lib.PaperLib;
import java.util.AbstractMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerJoinListener implements Listener {

    private final BuildSystem plugin;
    private final ConfigValues configValues;

    private final PlayerManager playerManager;
    private final SettingsManager settingsManager;
    private final SpawnManager spawnManager;
    private final WorldManager worldManager;

    public PlayerJoinListener(BuildSystem plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();

        this.playerManager = plugin.getPlayerManager();
        this.settingsManager = plugin.getSettingsManager();
        this.spawnManager = plugin.getSpawnManager();
        this.worldManager = plugin.getWorldManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void sendPlayerJoinMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String message = plugin.getConfigValues().isJoinQuitMessages()
                ? Messages.getString("player_join", player, new AbstractMap.SimpleEntry<>("%player%", player.getName()))
                : null;
        event.setJoinMessage(message);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUIDFetcher.cacheUser(player.getUniqueId(), player.getName());

        BuildPlayer buildPlayer = playerManager.createBuildPlayer(player);
        manageHidePlayer(player, buildPlayer);
        manageSettings(player, buildPlayer.getSettings());
        teleportToCorrectLocation(player, buildPlayer);
        playerManager.giveNavigator(player);

        String worldName = player.getWorld().getName();
        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld != null) {
            WorldData worldData = buildWorld.getData();
            if (!worldData.physics().get() && player.hasPermission("buildsystem.physics.message")) {
                Messages.sendMessage(player, "physics_deactivated_in_world", new AbstractMap.SimpleEntry<>("%world%", worldName));
            }

            if (configValues.isArchiveVanish() && worldData.status().get() == WorldStatus.ARCHIVE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false), false);
                Bukkit.getOnlinePlayers().forEach(pl -> pl.hidePlayer(player));
            }
        }

        if (player.hasPermission("buildsystem.updates")) {
            performUpdateCheck(player);
        }
    }

    /**
     * Teleports the player to the correct location.
     * <ul>
     *   <li>If the spawn exists and {@link Settings#isSpawnTeleport()} is enabled for the player, the player will be teleported to the spawn</li>
     *   <li>If the player has a {@link LogoutLocation}, teleport to that location</li>
     *   <li>Otherwise, do nothing</li>
     * </ul>
     *
     * @param player      The player to teleport
     * @param buildPlayer The build-player for the given player
     */
    private void teleportToCorrectLocation(Player player, BuildPlayer buildPlayer) {
        if (buildPlayer.getSettings().isSpawnTeleport() && spawnManager.spawnExists()) {
            spawnManager.teleport(player);
            return;
        }

        LogoutLocation logoutLocation = buildPlayer.getLogoutLocation();
        if (logoutLocation == null) {
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(logoutLocation.getWorldName());
        if (buildWorld == null) {
            return;
        }

        int delay = buildWorld.isLoaded() ? 0 : 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location location = logoutLocation.getLocation();
            if (location != null) {
                PaperLib.teleportAsync(player, location);
            }
        }, delay);
    }

    @SuppressWarnings("deprecation")
    private void manageHidePlayer(Player player, BuildPlayer buildPlayer) {
        // Hide all players to player
        if (buildPlayer.getSettings().isHidePlayers()) {
            Bukkit.getOnlinePlayers().forEach(player::hidePlayer);
        }

        // Hide player from all players who have hidePlayers enabled
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (!settingsManager.getSettings(pl).isHidePlayers()) {
                continue;
            }
            pl.hidePlayer(player);
        }
    }

    /**
     * Activates features for the player according to their {@link Settings}.
     *
     * @param player   The player to activate the features for
     * @param settings The player's settings
     */
    private void manageSettings(Player player, Settings settings) {
        if (settings.isNoClip()) {
            plugin.getNoClipManager().startNoClip(player);
        }

        if (settings.isScoreboard()) {
            settingsManager.startScoreboard(player);
            plugin.getPlayerManager().forceUpdateSidebar(player);
        }

        if (settings.isClearInventory()) {
            player.getInventory().clear();
        }
    }

    private void performUpdateCheck(Player player) {
        if (!configValues.isUpdateChecker()) {
            return;
        }

        UpdateChecker.init(plugin, BuildSystem.SPIGOT_ID)
                .requestUpdateCheck()
                .whenComplete((result, e) -> {
                    if (result.requiresUpdate()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        Messages.getStringList("update_available", player).forEach(line ->
                                stringBuilder.append(line
                                                .replace("%new_version%", result.getNewestVersion())
                                                .replace("%current_version%", plugin.getDescription().getVersion()))
                                        .append("\n"));
                        player.sendMessage(stringBuilder.toString());
                    }
                });
    }
}