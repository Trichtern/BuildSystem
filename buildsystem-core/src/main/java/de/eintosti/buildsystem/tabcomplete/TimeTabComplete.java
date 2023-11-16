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
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TimeTabComplete extends ArgumentSorter implements TabCompleter {

    private final BuildWorldManager worldManager;

    public TimeTabComplete(BuildSystemPlugin plugin) {
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("day").setTabCompleter(this);
        plugin.getCommand("night").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        ArrayList<String> arrayList = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return arrayList;
        }
        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "day":
                worldManager.getBuildWorlds().forEach(world -> {
                    String worldName = world.getName();
                    if (worldManager.isPermitted(player, "buildsystem.day", worldName)) {
                        addArgument(args[0], worldName, arrayList);
                    }
                });
                break;

            case "night":
                worldManager.getBuildWorlds().forEach(world -> {
                    String worldName = world.getName();
                    if (worldManager.isPermitted(player, "buildsystem.night", worldName)) {
                        addArgument(args[0], worldName, arrayList);
                    }
                });
                break;
        }

        return arrayList;
    }
}