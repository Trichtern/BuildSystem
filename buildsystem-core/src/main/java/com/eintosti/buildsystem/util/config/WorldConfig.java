/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.util.config;

import com.cryptomorin.xseries.XMaterial;
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.WorldManager;
import com.eintosti.buildsystem.object.world.BuildWorld;
import org.bukkit.generator.ChunkGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author einTosti
 */
public class WorldConfig extends ConfigurationFile {

    private final BuildSystem plugin;

    public WorldConfig(BuildSystem plugin) {
        super(plugin, "worlds.yml");
        this.plugin = plugin;
    }

    public void saveWorld(BuildWorld buildWorld) {
        getFile().set("worlds." + buildWorld.getName(), buildWorld.serialize());
        saveFile();
    }

    public void loadWorlds(WorldManager worldManager) {
        Logger logger = plugin.getLogger();
        if (plugin.getConfigValues().isUnloadWorlds()) {
            logger.log(Level.INFO, "*** Unload worlds is enabled ***");
            logger.log(Level.INFO, "*** Therefore worlds will not be loaded ***");
            return;
        }

        logger.log(Level.INFO, "*** All worlds will be loaded now ***");

        worldManager.getBuildWorlds().forEach(world -> {
            String worldName = world.getName();
            ChunkGenerator chunkGenerator = world.getChunkGenerator();
            worldManager.generateBukkitWorld(worldName, world.getType(), chunkGenerator);

            if (world.getMaterial() == XMaterial.PLAYER_HEAD) {
                plugin.getSkullCache().cacheSkull(worldName);
            }

            logger.log(Level.INFO, "✔ World loaded: " + worldName);
        });

        logger.log(Level.INFO, "*** All worlds have been loaded ***");
    }
}
