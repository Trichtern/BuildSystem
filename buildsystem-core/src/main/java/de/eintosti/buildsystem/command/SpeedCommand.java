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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

public class SpeedCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;

    public SpeedCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("speed").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildsystem.speed")) {
            plugin.sendPermissionMessage(player);
            return true;
        }

        switch (args.length) {
            case 0:
                plugin.getSpeedInventory().openInventory(player);
                XSound.BLOCK_CHEST_OPEN.play(player);
                break;
            case 1:
                String speedString = args[0];
                switch (speedString) {
                    case "1":
                        setSpeed(player, 0.2f, speedString);
                        break;
                    case "2":
                        setSpeed(player, 0.4f, speedString);
                        break;
                    case "3":
                        setSpeed(player, 0.6f, speedString);
                        break;
                    case "4":
                        setSpeed(player, 0.8f, speedString);
                        break;
                    case "5":
                        setSpeed(player, 1.0f, speedString);
                        break;
                    default:
                        Messages.sendMessage(player, "speed_usage");
                        break;
                }
                break;
            default:
                Messages.sendMessage(player, "speed_usage");
                break;
        }

        return true;
    }

    private void setSpeed(Player player, float speed, String speedString) {
        if (player.isFlying()) {
            player.setFlySpeed(speed - 0.1f);
            Messages.sendMessage(player, "speed_set_flying", new AbstractMap.SimpleEntry<>("%speed%", speedString));
        } else {
            player.setWalkSpeed(speed);
            Messages.sendMessage(player, "speed_set_walking", new AbstractMap.SimpleEntry<>("%speed%", speedString));
        }
    }
}