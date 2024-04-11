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
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.messages.MessagesOld;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

public class ExplosionsCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final BuildWorldManager worldManager;

    public ExplosionsCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("explosions").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(MessagesOld.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        String worldName = args.length == 0 ? player.getWorld().getName() : args[0];
        if (!worldManager.isPermitted(player, "buildsystem.explosions", worldName)) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        switch (args.length) {
            case 0:
                toggleExplosions(player, player.getWorld());
                break;
            case 1:
                toggleExplosions(player, Bukkit.getWorld(args[0]));
                break;
            default:
                MessagesOld.sendMessage(player, "explosions_usage");
                break;
        }

        return true;
    }

    private void toggleExplosions(Player player, World bukkitWorld) {
        if (bukkitWorld == null) {
            MessagesOld.sendMessage(player, "explosions_unknown_world");
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());
        if (buildWorld == null) {
            MessagesOld.sendMessage(player, "explosions_world_not_imported");
            return;
        }

        WorldData worldData = buildWorld.getData();
        if (!worldData.explosions().get()) {
            worldData.explosions().set(true);
            MessagesOld.sendMessage(player, "explosions_activated", new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()));
        } else {
            worldData.explosions().set(false);
            MessagesOld.sendMessage(player, "explosions_deactivated", new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()));
        }
    }
}