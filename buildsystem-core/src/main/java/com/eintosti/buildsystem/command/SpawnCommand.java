/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.command;

import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.SpawnManager;
import com.eintosti.buildsystem.manager.WorldManager;
import com.eintosti.buildsystem.object.world.BuildWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * @author einTosti
 */
public class SpawnCommand implements CommandExecutor {

    private final BuildSystem plugin;
    private final SpawnManager spawnManager;
    private final WorldManager worldManager;

    public SpawnCommand(BuildSystem plugin) {
        this.plugin = plugin;
        this.spawnManager = plugin.getSpawnManager();
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("spawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().log(Level.WARNING, plugin.getString("sender_not_player"));
            return true;
        }

        Player player = (Player) sender;

        switch (args.length) {
            case 0:
                if (!spawnManager.teleport(player)) {
                    player.sendMessage(plugin.getString("spawn_unavailable"));
                } else if (plugin.getConfigValues().isSpawnTeleportMessage()) {
                    player.sendMessage(plugin.getString("spawn_teleported"));
                }
                break;

            case 1:
                if (!player.hasPermission("buildsystem.spawn")) {
                    player.sendMessage(plugin.getString("spawn_usage"));
                    return true;
                }

                switch (args[0].toLowerCase()) {
                    case "set":
                        Location playerLocation = player.getLocation();
                        World bukkitWorld = playerLocation.getWorld();
                        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());

                        if (buildWorld == null) {
                            player.sendMessage(plugin.getString("spawn_world_not_imported"));
                            return true;
                        }

                        spawnManager.set(playerLocation, buildWorld.getName());
                        player.sendMessage(plugin.getString("spawn_set")
                                .replace("%x%", round(playerLocation.getX()))
                                .replace("%y%", round(playerLocation.getY()))
                                .replace("%z%", round(playerLocation.getZ()))
                                .replace("%world%", playerLocation.getWorld().getName()));
                        break;
                    case "remove":
                        spawnManager.remove();
                        player.sendMessage(plugin.getString("spawn_remove"));
                        break;
                    default:
                        player.sendMessage(plugin.getString("spawn_admin"));
                        break;
                }
                break;

            default:
                if (player.hasPermission("buildsystem.spawn")) {
                    player.sendMessage(plugin.getString("spawn_admin"));
                } else {
                    player.sendMessage(plugin.getString("spawn_usage"));
                }
                break;
        }
        return true;
    }

    private String round(double value) {
        int scale = (int) Math.pow(10, 2);
        return String.valueOf((double) Math.round(value * scale) / scale);
    }
}
