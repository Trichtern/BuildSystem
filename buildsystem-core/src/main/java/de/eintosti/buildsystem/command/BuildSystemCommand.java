/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.command;

import com.google.common.collect.Lists;
import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BuildSystemCommand extends PagedCommand implements CommandExecutor {

    private final BuildSystem plugin;

    public BuildSystemCommand(BuildSystem plugin) {
        super("buildsystem_permission", "buildsystem_title_with_page");

        this.plugin = plugin;
        plugin.getCommand("buildsystem").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", null));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendMessage(player, 1);
        } else if (args.length == 1) {
            try {
                int page = Integer.parseInt(args[0]);
                sendMessage(player, page);
            } catch (NumberFormatException e) {
                Messages.sendMessage(player, "buildsystem_invalid_page");
            }
        } else {
            Messages.sendMessage(player, "buildsystem_usage");
        }
        return true;
    }

    @Override
    protected List<TextComponent> getCommands(Player player) {
        List<TextComponent> commands = Lists.newArrayList(
                createComponent(player, "buildsystem_back", "/back", "/back", "buildsystem.back"),
                createComponent(player, "buildsystem_blocks", "/blocks", "/blocks", "buildsystem.blocks"),
                createComponent(player, "buildsystem_build", "/build [player]", "/build", "buildsystem.build"),
                createComponent(player, "buildsystem_config", "/config reload", "/config reload", "buildsystem.config"),
                createComponent(player, "buildsystem_day", "/day [world]", "/day", "buildsystem.day"),
                createComponent(player, "buildsystem_explosions", "/explosions [world]", "/explosions", "buildsystem.explosions"),
                createComponent(player, "buildsystem_gamemode", "/gm <gamemode> [player]", "/gm ", "buildsystem.gamemode"),
                createComponent(player, "buildsystem_night", "/night [world]", "/night", "buildsystem.night"),
                createComponent(player, "buildsystem_noai", "/noai [world]", "/noai", "buildsystem.noai"),
                createComponent(player, "buildsystem_physics", "/physics [world]", "/physics", "buildsystem.physics"),
                createComponent(player, "buildsystem_settings", "/settings", "/settings", "buildsystem.settings"),
                createComponent(player, "buildsystem_setup", "/setup", "/setup", "buildsystem.setup"),
                createComponent(player, "buildsystem_skull", "/skull [player/id]", "/skull", "buildsystem.skull"),
                createComponent(player, "buildsystem_spawn", "/spawn", "/spawn", "-"),
                createComponent(player, "buildsystem_speed", "/speed <1-5>", "/speed ", "buildsystem.speed"),
                createComponent(player, "buildsystem_top", "/top", "/top", "buildsystem.top"),
                createComponent(player, "buildsystem_worlds", "/worlds help", "/worlds help", "-")
        );
        commands.removeIf(textComponent -> textComponent.getText().isEmpty());
        return commands;
    }
}