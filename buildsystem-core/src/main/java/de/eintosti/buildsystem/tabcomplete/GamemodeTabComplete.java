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
package de.eintosti.buildsystem.tabcomplete;

import de.eintosti.buildsystem.BuildSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GamemodeTabComplete extends ArgumentSorter implements TabCompleter {

    public GamemodeTabComplete(BuildSystemPlugin plugin) {
        plugin.getCommand("gamemode").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        ArrayList<String> arrayList = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return arrayList;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("buildsystem.gamemode")) {
            return arrayList;
        }

        if (args.length == 1) {
            for (GameMode gameMode : GameMode.values()) {
                addArgument(args[0], gameMode.name().toLowerCase(), arrayList);
            }
        } else if (args.length == 2) {
            if (!player.hasPermission("buildsystem.gamemode.others")) {
                return arrayList;
            }

            switch (args[0].toLowerCase()) {
                case "survival":
                case "s":
                case "0":
                case "creative":
                case "c":
                case "1":
                case "adventure":
                case "a":
                case "2":
                case "spectator":
                case "sp":
                case "3":
                    Bukkit.getOnlinePlayers().forEach(pl -> addArgument(args[1], pl.getName(), arrayList));
                    break;
            }
        }

        return arrayList;
    }
}