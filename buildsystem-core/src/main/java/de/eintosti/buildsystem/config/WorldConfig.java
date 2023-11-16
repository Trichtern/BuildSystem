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
package de.eintosti.buildsystem.config;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.world.BuildWorldManager;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import de.eintosti.buildsystem.world.CraftBuildWorldCreator;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class WorldConfig extends ConfigurationFile {


    private final BuildSystemPlugin plugin;
    private final Logger logger;

    public WorldConfig(BuildSystemPlugin plugin) {
        super(plugin, "worlds.yml");
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void saveWorlds(Collection<CraftBuildWorld> buildWorlds) {
        buildWorlds.forEach(buildWorld -> getFile().set("worlds." + buildWorld.getName(), buildWorld.serialize()));
        saveFile();
    }

    public void loadWorlds(BuildWorldManager worldManager) {
        boolean loadAllWorlds = !plugin.getConfigValues().isUnloadWorlds();
        if (loadAllWorlds) {
            logger.info("*** All worlds will be loaded now ***");
        } else {
            logger.info("*** 'Unload worlds' has been enabled in the config ***");
            logger.info("*** Therefore, worlds will not be pre-loaded ***");
        }

        List<BuildWorld> notLoaded = new ArrayList<>();
        worldManager.getBuildWorlds().forEach(buildWorld -> {
            if (loadWorld(buildWorld, loadAllWorlds) == LoadResult.FAILED) {
                notLoaded.add(buildWorld);
            }
        });
        notLoaded.forEach(worldManager::removeBuildWorld);

        if (loadAllWorlds) {
            logger.info("*** All worlds have been loaded ***");
        }
    }

    /**
     * Loads the {@link BuildWorld} if {@link ConfigValues#isUnloadWorlds()} is not enabled.
     * Otherwise, the world will only be loaded if it's on the unload blacklist.
     *
     * @param buildWorld The world to load
     * @param alwaysLoad Should the world always be loaded
     * @return The result of the load attempt
     */
    private LoadResult loadWorld(BuildWorld buildWorld, boolean alwaysLoad) {
        String worldName = buildWorld.getName();
        if (!alwaysLoad && !plugin.getConfigValues().getBlackListedWorldsToUnload().contains(worldName)) {
            return LoadResult.NOT_BLACKLISTED;
        }

        World world = new CraftBuildWorldCreator(plugin, buildWorld).generateBukkitWorld();
        if (world == null) {
            return LoadResult.FAILED;
        }

        buildWorld.getData().lastLoaded().set(System.currentTimeMillis());
        logger.info("✔ World loaded: " + worldName);
        return LoadResult.LOADED;
    }

    private enum LoadResult {

        /**
         * The world was loaded
         */
        LOADED,

        /**
         * The world was unable to be loaded
         */
        FAILED,

        /**
         * {@link ConfigValues#isUnloadWorlds()} is enabled and the world is not blacklisted to unload
         */
        NOT_BLACKLISTED
    }
}