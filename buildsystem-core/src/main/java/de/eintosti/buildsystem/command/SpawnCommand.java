/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.command;

import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.world.BuildWorld;
import de.eintosti.buildsystem.world.SpawnManager;
import de.eintosti.buildsystem.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

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
            plugin.getLogger().warning(Messages.getString("sender_not_player"));
            return true;
        }

        Player player = (Player) sender;

        switch (args.length) {
            case 0:
                if (!spawnManager.teleport(player)) {
                    Messages.sendMessage(player, "spawn_unavailable");
                } else if (plugin.getConfigValues().isSpawnTeleportMessage()) {
                    Messages.sendMessage(player, "spawn_teleported");
                }
                break;

            case 1:
                if (!player.hasPermission("buildsystem.spawn")) {
                    Messages.sendMessage(player, "spawn_usage");
                    return true;
                }

                switch (args[0].toLowerCase()) {
                    case "set":
                        Location playerLocation = player.getLocation();
                        World bukkitWorld = playerLocation.getWorld();
                        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());

                        if (buildWorld == null) {
                            Messages.sendMessage(player, "spawn_world_not_imported");
                            return true;
                        }

                        spawnManager.set(playerLocation, buildWorld.getName());
                        Messages.sendMessage(player, "spawn_set",
                                new AbstractMap.SimpleEntry<>("%x%", round(playerLocation.getX())),
                                new AbstractMap.SimpleEntry<>("%y%", round(playerLocation.getY())),
                                new AbstractMap.SimpleEntry<>("%z%", round(playerLocation.getZ())),
                                new AbstractMap.SimpleEntry<>("%world%", playerLocation.getWorld().getName())
                        );
                        break;
                    case "remove":
                        spawnManager.remove();
                        Messages.sendMessage(player, "spawn_remove");
                        break;
                    default:
                        Messages.sendMessage(player, "spawn_admin");
                        break;
                }
                break;

            default:
                String key = player.hasPermission("buildsystem.spawn") ? "spawn_admin" : "spawn_usage";
                Messages.sendMessage(player, key);
                break;
        }
        return true;
    }

    private String round(double value) {
        int scale = (int) Math.pow(10, 2);
        return String.valueOf((double) Math.round(value * scale) / scale);
    }
}