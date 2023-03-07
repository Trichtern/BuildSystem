/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.eintosti.buildsystem.command.subcommand.worlds;

import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.Messages;
import com.eintosti.buildsystem.command.subcommand.Argument;
import com.eintosti.buildsystem.command.subcommand.SubCommand;
import com.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import com.eintosti.buildsystem.world.BuildWorld;
import com.eintosti.buildsystem.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * @author einTosti
 */
public class ImportAllSubCommand implements SubCommand {

    private final BuildSystem plugin;

    public ImportAllSubCommand(BuildSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!hasPermission(player)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length != 1) {
            Messages.sendMessage(player, "worlds_importall_usage");
            return;
        }

        WorldManager worldManager = plugin.getWorldManager();
        File worldContainer = Bukkit.getWorldContainer();
        String[] directories = worldContainer.list((dir, name) -> {
            File worldFolder = new File(dir, name);
            if (!worldFolder.isDirectory()) {
                return false;
            }

            File levelFile = new File(dir + File.separator + name + File.separator + "level.dat");
            if (!levelFile.exists()) {
                return false;
            }

            BuildWorld buildWorld = worldManager.getBuildWorld(name);
            return buildWorld == null;
        });

        if (directories == null || directories.length == 0) {
            Messages.sendMessage(player, "worlds_importall_no_worlds");
            return;
        }

        worldManager.importWorlds(player, directories);
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.IMPORT_ALL;
    }
}