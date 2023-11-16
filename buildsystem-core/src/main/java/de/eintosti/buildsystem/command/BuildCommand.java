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
import de.eintosti.buildsystem.player.CachedValues;
import de.eintosti.buildsystem.player.CraftBuildPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.UUID;

public class BuildCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final BuildPlayerManager playerManager;

    public BuildCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        plugin.getCommand("build").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildsystem.build")) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        switch (args.length) {
            case 0: {
                toggleBuildMode(player, null, true);
                break;
            }

            case 1: {
                if (!player.hasPermission("buildsystem.build.others")) {
                    plugin.sendPermissionMessage(player);
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    Messages.sendMessage(player, "build_player_not_found");
                    return true;
                }

                toggleBuildMode(target, player, false);
                break;
            }

            default: {
                Messages.sendMessage(player, "build_usage");
                break;
            }
        }

        return true;
    }

    private void toggleBuildMode(Player target, Player sender, boolean self) {
        UUID targetUuid = target.getUniqueId();
        CraftBuildPlayer buildPlayer = playerManager.getBuildPlayer(targetUuid);
        CachedValues cachedValues = buildPlayer.getCachedValues();

        if (playerManager.getBuildModePlayers().remove(targetUuid)) {
            cachedValues.resetGameModeIfPresent(target);
            cachedValues.resetInventoryIfPresent(target);

            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(target);
            if (self) {
                Messages.sendMessage(target, "build_deactivated_self");
            } else {
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(sender);
                Messages.sendMessage(sender, "build_deactivated_other_sender", new AbstractMap.SimpleEntry<>("%target%", target.getName()));
                Messages.sendMessage(target, "build_deactivated_other_target", new AbstractMap.SimpleEntry<>("%sender%", sender.getName()));
            }
        } else {
            playerManager.getBuildModePlayers().add(targetUuid);
            cachedValues.saveGameMode(target.getGameMode());
            cachedValues.saveInventory(target.getInventory().getContents());
            target.setGameMode(GameMode.CREATIVE);

            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(target);
            if (self) {
                Messages.sendMessage(target, "build_activated_self");
            } else {
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(sender);
                Messages.sendMessage(sender, "build_activated_other_sender", new AbstractMap.SimpleEntry<>("%target%", target.getName()));
                Messages.sendMessage(target, "build_activated_other_target", new AbstractMap.SimpleEntry<>("%sender%", sender.getName()));
            }
        }
    }
}