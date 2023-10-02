/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
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
import de.eintosti.buildsystem.util.UpdateChecker;
import de.eintosti.buildsystem.world.BuildWorld;
import de.eintosti.buildsystem.world.SpawnManager;
import de.eintosti.buildsystem.world.WorldManager;
import de.eintosti.buildsystem.world.data.WorldData;
import de.eintosti.buildsystem.world.data.WorldStatus;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.AbstractMap;

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
        boolean isJoinMessage = plugin.getConfigValues().isJoinQuitMessages();
        String message = isJoinMessage ? Messages.getString("player_join", new AbstractMap.SimpleEntry<>("%player%", event.getPlayer().getName())) : null;
        event.setJoinMessage(message);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> PaperLib.teleportAsync(player, logoutLocation.getLocation()), delay);
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
                        Messages.getStringList("update_available").forEach(line ->
                                stringBuilder.append(line
                                                .replace("%new_version%", result.getNewestVersion())
                                                .replace("%current_version%", plugin.getDescription().getVersion()))
                                        .append("\n"));
                        player.sendMessage(stringBuilder.toString());
                    }
                });
    }
}