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
package de.eintosti.buildsystem.command;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.world.BuildWorldManager;
import de.eintosti.buildsystem.world.SpawnManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

public class SpawnCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final SpawnManager spawnManager;
    private final BuildWorldManager worldManager;

    public SpawnCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.spawnManager = plugin.getSpawnManager();
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("spawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
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