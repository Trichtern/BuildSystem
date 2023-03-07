/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.eintosti.buildsystem.command.subcommand.worlds;

import com.cryptomorin.xseries.XSound;
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.Messages;
import com.eintosti.buildsystem.command.subcommand.Argument;
import com.eintosti.buildsystem.command.subcommand.SubCommand;
import com.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import com.eintosti.buildsystem.util.UUIDFetcher;
import com.eintosti.buildsystem.util.external.PlayerChatInput;
import com.eintosti.buildsystem.world.BuildWorld;
import com.eintosti.buildsystem.world.Builder;
import com.eintosti.buildsystem.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.UUID;

/**
 * @author einTosti
 */
public class AddBuilderSubCommand implements SubCommand {

    private final BuildSystem plugin;
    private final String worldName;

    public AddBuilderSubCommand(BuildSystem plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;
    }

    @Override
    public void execute(Player player, String[] args) {
        WorldManager worldManager = plugin.getWorldManager();
        if (!worldManager.isPermitted(player, getArgument().getPermission(), worldName)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length > 2) {
            Messages.sendMessage(player, "worlds_addbuilder_usage");
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            Messages.sendMessage(player, "worlds_addbuilder_unknown_world");
            return;
        }

        getAddBuilderInput(player, buildWorld, true);
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.ADD_BUILDER;
    }

    public void getAddBuilderInput(Player player, BuildWorld buildWorld, boolean closeInventory) {
        new PlayerChatInput(plugin, player, "enter_player_name", input -> {
            String builderName = input.trim();
            Player builderPlayer = Bukkit.getPlayerExact(builderName);
            Builder builder;
            UUID builderId;

            if (builderPlayer == null) {
                builderId = UUIDFetcher.getUUID(builderName);
                if (builderId == null) {
                    Messages.sendMessage(player, "worlds_addbuilder_player_not_found");
                    player.closeInventory();
                    return;
                }
                builder = new Builder(builderId, builderName);
            } else {
                builder = new Builder(builderPlayer);
                builderId = builderPlayer.getUniqueId();
            }

            if (builderId.equals(player.getUniqueId()) && buildWorld.isCreator(player)) {
                Messages.sendMessage(player, "worlds_addbuilder_already_creator");
                player.closeInventory();
                return;
            }

            if (buildWorld.isBuilder(builderId)) {
                Messages.sendMessage(player, "worlds_addbuilder_already_added");
                player.closeInventory();
                return;
            }

            buildWorld.addBuilder(builder);
            XSound.ENTITY_PLAYER_LEVELUP.play(player);
            Messages.sendMessage(player, "worlds_addbuilder_added", new AbstractMap.SimpleEntry<>("%builder%", builderName));

            if (closeInventory) {
                player.closeInventory();
            } else {
                player.openInventory(plugin.getBuilderInventory().getInventory(buildWorld, player));
            }
        });
    }
}