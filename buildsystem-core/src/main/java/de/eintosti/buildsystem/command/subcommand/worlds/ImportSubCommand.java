/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.command.subcommand.worlds;

import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.command.subcommand.Argument;
import de.eintosti.buildsystem.command.subcommand.SubCommand;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import de.eintosti.buildsystem.util.ArgumentParser;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.BuildWorld;
import de.eintosti.buildsystem.world.Builder;
import de.eintosti.buildsystem.world.WorldManager;
import de.eintosti.buildsystem.world.generator.Generator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.UUID;

public class ImportSubCommand implements SubCommand {

    private final BuildSystem plugin;
    private final String worldName;

    public ImportSubCommand(BuildSystem plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!hasPermission(player)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Messages.sendMessage(player, "worlds_import_usage");
            return;
        }

        WorldManager worldManager = plugin.getWorldManager();
        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld != null) {
            Messages.sendMessage(player, "worlds_import_world_is_imported");
            return;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), args[1]);
        File levelFile = new File(worldFolder.getAbsolutePath() + File.separator + "level.dat");
        if (!worldFolder.isDirectory() || !levelFile.exists()) {
            Messages.sendMessage(player, "worlds_import_unknown_world");
            return;
        }

        String invalidChar = Arrays.stream(worldName.split(""))
                .filter(c -> c.matches("[^A-Za-z\\d/_-]") || c.matches(plugin.getConfigValues().getInvalidNameCharacters()))
                .findFirst()
                .orElse(null);
        if (invalidChar != null) {
            Messages.sendMessage(player, "worlds_import_invalid_character",
                    new AbstractMap.SimpleEntry<>("%world%", worldName),
                    new AbstractMap.SimpleEntry<>("%char%", invalidChar)
            );
            return;
        }

        Builder creator = new Builder(null, "-");
        Generator generator = Generator.VOID;
        String generatorName = null;

        if (args.length != 2) {
            ArgumentParser parser = new ArgumentParser(args);

            if (parser.isArgument("g")) {
                String generatorArg = parser.getValue("g");
                if (generatorArg == null) {
                    Messages.sendMessage(player, "worlds_import_usage");
                    return;
                }
                try {
                    generator = Generator.valueOf(generatorArg.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    generator = Generator.CUSTOM;
                    generatorName = generatorArg;
                }
            }

            if (parser.isArgument("c")) {
                String creatorArg = parser.getValue("c");
                if (creatorArg == null) {
                    Messages.sendMessage(player, "worlds_import_usage");
                    return;
                }
                UUID creatorId = UUIDFetcher.getUUID(creatorArg);
                if (creatorId == null) {
                    Messages.sendMessage(player, "worlds_import_player_not_found");
                    return;
                }
                creator = new Builder(creatorId, creatorArg);
            }
        }

        Messages.sendMessage(player, "worlds_import_started", new AbstractMap.SimpleEntry<>("%world%", worldName));
        worldManager.importWorld(player, worldName, creator, generator, generatorName, true);
        Messages.sendMessage(player, "worlds_import_finished");
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.IMPORT;
    }
}