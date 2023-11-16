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

import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.player.BuildPlayerManager;
import de.eintosti.buildsystem.player.CraftBuildPlayer;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BackCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final BuildPlayerManager playerManager;

    public BackCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        plugin.getCommand("back").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildsystem.back")) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        if (args.length == 0) {
            teleportBack(player);
        } else {
            Messages.sendMessage(player, "back_usage");
        }

        return true;
    }

    private void teleportBack(Player player) {
        UUID playerUuid = player.getUniqueId();
        CraftBuildPlayer buildPlayer = playerManager.getBuildPlayer(playerUuid);
        Location previousLocation = buildPlayer.getPreviousLocation();

        if (previousLocation == null) {
            Messages.sendMessage(player, "back_failed");
            return;
        }

        PaperLib.teleportAsync(player, previousLocation)
                .whenComplete((completed, throwable) -> {
                    if (!completed) {
                        return;
                    }
                    XSound.ENTITY_ZOMBIE_INFECT.play(player);
                    Messages.sendMessage(player, "back_teleported");
                    buildPlayer.setPreviousLocation(null);
                });
    }
}