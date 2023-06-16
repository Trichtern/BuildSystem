/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.settings;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.version.util.MinecraftVersion;
import de.eintosti.buildsystem.world.BuildWorldManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SettingsManager {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;
    private final BuildWorldManager worldManager;

    private final Map<UUID, FastBoard> boards;

    private final String scoreboardTitle;
    private final List<String> scoreboardBody;

    public SettingsManager(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.worldManager = plugin.getWorldManager();

        this.boards = new HashMap<>();

        this.scoreboardTitle = Messages.getString("title");
        this.scoreboardBody = Messages.getStringList("body");
    }

    public CraftSettings getSettings(UUID uuid) {
        return plugin.getPlayerManager().getBuildPlayer(uuid).getSettings();
    }

    public CraftSettings getSettings(Player player) {
        return getSettings(player.getUniqueId());
    }

    /**
     * Only set a player's scoreboard if {@link CraftSettings#isScoreboard} is equal to {@code true}.
     *
     * @param player   The player object
     * @param settings The player's settings
     */
    public void startScoreboard(Player player, CraftSettings settings) {
        if (!settings.isScoreboard()) {
            stopScoreboard(player, settings);
            return;
        }

        startScoreboard(player);
    }

    /**
     * Only set a player's scoreboard if {@link CraftSettings#isScoreboard} is equal to {@code true}.
     *
     * @param player The player object
     */
    public void startScoreboard(Player player) {
        if (!configValues.isScoreboard()) {
            return;
        }

        CraftSettings settings = getSettings(player);
        FastBoard board = new FastBoard(player);
        this.boards.put(player.getUniqueId(), board);

        if (!settings.isScoreboard()) {
            stopScoreboard(player, settings);
            return;
        }

        board.updateTitle(this.scoreboardTitle);
        BukkitTask scoreboardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> updateScoreboard(player, board), 0L, 20L);
        settings.setScoreboardTask(scoreboardTask);
    }

    /**
     * Set each player's scoreboard if they have {@link CraftSettings#isScoreboard} enabled.
     */
    public void startScoreboard() {
        if (!configValues.isScoreboard()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(this::startScoreboard);
    }

    public void updateScoreboard(Player player) {
        FastBoard board = this.boards.get(player.getUniqueId());
        if (board != null) {
            updateScoreboard(player, board);
        }
    }

    private void updateScoreboard(Player player, FastBoard board) {
        List<String> body = new ArrayList<>();

        for (String line : this.scoreboardBody) {
            body.add(injectPlaceholders(line, player));
        }

        // Scoreboard line cannot be longer than 30 chars in versions <1.13
        if (MinecraftVersion.getCurrent().isLowerThan(MinecraftVersion.AQUATIC_13)) {
            body = body.stream().map(line -> line.substring(0, Math.min(line.length(), 30))).collect(Collectors.toList());
        }

        board.updateLines(body);
    }

    private String injectPlaceholders(String originalString, Player player) {
        if (!originalString.matches(".*%*%.*")) {
            return originalString;
        }

        String worldName = player.getWorld().getName();
        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);

        return originalString
                .replace("%world%", worldName)
                .replace("%status%", parseWorldInformation(buildWorld, "%status%"))
                .replace("%permission%", parseWorldInformation(buildWorld, "%permission%"))
                .replace("%project%", parseWorldInformation(buildWorld, "%project%"))
                .replace("%creator%", parseWorldInformation(buildWorld, "%creator%"))
                .replace("%creation%", parseWorldInformation(buildWorld, "%creation%"))
                .replace("%lastedited%", parseWorldInformation(buildWorld, "%lastedited%"))
                .replace("%lastloaded%", parseWorldInformation(buildWorld, "%lastloaded%"))
                .replace("%lastunloaded%", parseWorldInformation(buildWorld, "%lastunloaded%"));
    }

    // Is there an easier way of doing this?
    private String parseWorldInformation(BuildWorld buildWorld, String input) {
        if (buildWorld == null) {
            return ChatColor.WHITE + "-";
        }

        WorldData worldData = buildWorld.getData();
        switch (input) {
            case "%status%":
                return Messages.getDataString(worldData.status().get().getKey());
            case "%permission%":
                return worldData.permission().get();
            case "%project%":
                return worldData.project().get();
            case "%creator%":
                return buildWorld.getCreator();
            case "%creation%":
                return Messages.formatDate(buildWorld.getCreationDate());
            case "%lastedited%":
                return Messages.formatDate(worldData.lastEdited().get());
            case "%lastloaded%":
                return Messages.formatDate(worldData.lastLoaded().get());
            case "%lastunloaded%":
                return Messages.formatDate(worldData.lastUnloaded().get());
            default:
                return ChatColor.WHITE + "-";
        }
    }

    private void stopScoreboard(Player player, CraftSettings settings) {
        BukkitTask scoreboardTask = settings.getScoreboardTask();
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
            settings.setScoreboardTask(null);
        }

        FastBoard board = this.boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void stopScoreboard(Player player) {
        stopScoreboard(player, getSettings(player));
    }

    public void stopScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::stopScoreboard);
    }
}