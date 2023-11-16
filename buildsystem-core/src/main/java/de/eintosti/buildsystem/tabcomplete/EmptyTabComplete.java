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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The tab completer which is used for commands which do not have any arguments.
 *
 * @author einTosti
 */
public class EmptyTabComplete implements TabCompleter {

    public EmptyTabComplete(BuildSystemPlugin plugin) {
        plugin.getCommand("back").setTabCompleter(this);
        plugin.getCommand("blocks").setTabCompleter(this);
        plugin.getCommand("buildsystem").setTabCompleter(this);
        plugin.getCommand("settings").setTabCompleter(this);
        plugin.getCommand("setup").setTabCompleter(this);
        plugin.getCommand("top").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return new ArrayList<>();
    }
}