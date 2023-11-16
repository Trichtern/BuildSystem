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
import de.eintosti.buildsystem.command.subcommand.SubCommand;
import de.eintosti.buildsystem.command.subcommand.worlds.*;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldsCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;

    public WorldsCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("worlds").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            if (!player.hasPermission("buildsystem.navigator")) {
                plugin.sendPermissionMessage(player);
                return true;
            }

            plugin.getNavigatorInventory().openInventory(player);
            XSound.BLOCK_CHEST_OPEN.play(player);
            return true;
        }

        WorldsTabComplete.WorldsArgument argument = WorldsTabComplete.WorldsArgument.matchArgument(args[0]);
        if (argument == null) {
            Messages.sendMessage(player, "worlds_unknown_command");
            return true;
        }

        // Most command use the structure /worlds <argument> <world> <...> which is why we assume that args[1] is the world name
        // Make sure to change if this is not the case for any specific command
        String worldName = args.length >= 2 ? args[1] : player.getWorld().getName();

        SubCommand subCommand;
        switch (argument) {
            case ADD_BUILDER: {
                subCommand = new AddBuilderSubCommand(plugin, worldName);
                break;
            }
            case BUILDERS: {
                subCommand = new BuildersSubCommand(plugin, worldName);
                break;
            }
            case DELETE: {
                subCommand = new DeleteSubCommand(plugin, worldName);
                break;
            }
            case EDIT: {
                subCommand = new EditSubCommand(plugin, worldName);
                break;
            }
            case HELP: {
                subCommand = new HelpSubCommand();
                break;
            }
            case IMPORT_ALL: {
                subCommand = new ImportAllSubCommand(plugin);
                break;
            }
            case IMPORT: {
                subCommand = new ImportSubCommand(plugin, worldName);
                break;
            }
            case INFO: {
                subCommand = new InfoSubCommand(plugin, worldName);
                break;
            }
            case ITEM: {
                subCommand = new ItemSubCommand(plugin);
                break;
            }
            case REMOVE_BUILDER: {
                subCommand = new RemoveBuilderSubCommand(plugin, worldName);
                break;
            }
            case REMOVE_SPAWN: {
                subCommand = new RemoveSpawnSubCommand(plugin);
                break;
            }
            case RENAME: {
                subCommand = new RenameSubCommand(plugin, worldName);
                break;
            }
            case SET_CREATOR: {
                subCommand = new SetCreatorSubCommand(plugin, worldName);
                break;
            }
            case SET_ITEM: {
                subCommand = new SetItemSubCommand(plugin, worldName);
                break;
            }
            case SET_PERMISSION: {
                subCommand = new SetPermissionSubCommand(plugin, worldName);
                break;
            }
            case SET_PROJECT: {
                subCommand = new SetProjectSubCommand(plugin, worldName);
                break;
            }
            case SET_SPAWN: {
                subCommand = new SetSpawnSubCommand(plugin);
                break;
            }
            case SET_STATUS: {
                subCommand = new SetStatusSubCommand(plugin, worldName);
                break;
            }
            case TP: {
                subCommand = new TeleportSubCommand(plugin);
                break;
            }
            case UNIMPORT: {
                subCommand = new UnimportSubCommand(plugin, worldName);
                break;
            }
            default: {
                throw new IllegalArgumentException("Could not find subcommand: " + argument.getName());
            }
        }
        subCommand.execute(player, args);
        return true;
    }
}