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
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

public class TimeCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;
    private final BuildWorldManager worldManager;

    public TimeCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("day").setExecutor(this);
        plugin.getCommand("night").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        String worldName = args.length == 0 ? player.getWorld().getName() : args[0];
        World world = Bukkit.getWorld(worldName);

        switch (label.toLowerCase()) {
            case "day": {
                if (!worldManager.isPermitted(player, "buildsystem.day", worldName)) {
                    plugin.sendPermissionMessage(player);
                    return true;
                }

                switch (args.length) {
                    case 0: {
                        world.setTime(configValues.getNoonTime());
                        Messages.sendMessage(player, "day_set", new AbstractMap.SimpleEntry<>("%world%", world.getName()));
                        break;
                    }
                    case 1: {
                        if (world == null) {
                            Messages.sendMessage(player, "day_unknown_world");
                            return true;
                        }
                        world.setTime(configValues.getNoonTime());
                        Messages.sendMessage(player, "day_set", new AbstractMap.SimpleEntry<>("%world%", world.getName()));
                        break;
                    }
                    default:
                        Messages.sendMessage(player, "day_usage");
                        break;
                }
                break;
            }

            case "night": {
                if (!worldManager.isPermitted(player, "buildsystem.night", worldName)) {
                    plugin.sendPermissionMessage(player);
                    return true;
                }

                switch (args.length) {
                    case 0: {
                        world.setTime(configValues.getNightTime());
                        Messages.sendMessage(player, "night_set", new AbstractMap.SimpleEntry<>("%world%", world.getName()));
                        break;
                    }
                    case 1: {
                        if (world == null) {
                            Messages.sendMessage(player, "night_unknown_world");
                            return true;
                        }
                        world.setTime(configValues.getNightTime());
                        Messages.sendMessage(player, "night_set", new AbstractMap.SimpleEntry<>("%world%", world.getName()));
                        break;
                    }
                    default:
                        Messages.sendMessage(player, "night_usage");
                        break;
                }
                break;
            }
        }
        return true;
    }
}