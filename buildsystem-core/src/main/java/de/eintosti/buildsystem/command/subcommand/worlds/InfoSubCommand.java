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
package de.eintosti.buildsystem.command.subcommand.worlds;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.command.subcommand.Argument;
import de.eintosti.buildsystem.command.subcommand.SubCommand;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import de.eintosti.buildsystem.world.BuildWorldManager;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.AbstractMap;

public class InfoSubCommand implements SubCommand {

    private final BuildSystemPlugin plugin;
    private final String worldName;

    public InfoSubCommand(BuildSystemPlugin plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;
    }

    @Override
    public void execute(Player player, String[] args) {
        BuildWorldManager worldManager = plugin.getWorldManager();
        if (!worldManager.isPermitted(player, getArgument().getPermission(), worldName)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length > 2) {
            Messages.sendMessage(player, "worlds_info_usage");
            return;
        }

        CraftBuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            Messages.sendMessage(player, "worlds_info_unknown_world");
            return;
        }

        //TODO: Print information about the custom generator?
        WorldData worldData = buildWorld.getData();
        Messages.sendMessage(player, "world_info",
                new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()),
                new AbstractMap.SimpleEntry<>("%creator%", buildWorld.getCreator()),
                new AbstractMap.SimpleEntry<>("%item%", worldData.material().get().name()),
                new AbstractMap.SimpleEntry<>("%type%", Messages.getDataString(buildWorld.getType().getKey(), player)),
                new AbstractMap.SimpleEntry<>("%private%", worldData.privateWorld().get()),
                new AbstractMap.SimpleEntry<>("%builders_enabled%", worldData.buildersEnabled().get()),
                new AbstractMap.SimpleEntry<>("%builders%", buildWorld.getBuildersInfo(player)),
                new AbstractMap.SimpleEntry<>("%block_breaking%", worldData.blockBreaking().get()),
                new AbstractMap.SimpleEntry<>("%block_placement%", worldData.blockPlacement().get()),
                new AbstractMap.SimpleEntry<>("%status%", Messages.getDataString(worldData.status().get().getKey(), player)),
                new AbstractMap.SimpleEntry<>("%project%", worldData.project().get()),
                new AbstractMap.SimpleEntry<>("%permission%", worldData.permission().get()),
                new AbstractMap.SimpleEntry<>("%time%", buildWorld.getWorldTime()),
                new AbstractMap.SimpleEntry<>("%creation%", Messages.formatDate(buildWorld.getCreationDate())),
                new AbstractMap.SimpleEntry<>("%physics%", worldData.physics().get()),
                new AbstractMap.SimpleEntry<>("%explosions%", worldData.explosions().get()),
                new AbstractMap.SimpleEntry<>("%mobai%", worldData.mobAi().get()),
                new AbstractMap.SimpleEntry<>("%custom_spawn%", getCustomSpawn(buildWorld)),
                new AbstractMap.SimpleEntry<>("%lastedited%", Messages.formatDate(worldData.lastEdited().get())),
                new AbstractMap.SimpleEntry<>("%lastloaded%", Messages.formatDate(worldData.lastLoaded().get())),
                new AbstractMap.SimpleEntry<>("%lastunloaded%", Messages.formatDate(worldData.lastUnloaded().get()))
        );
    }

    private String getCustomSpawn(BuildWorld buildWorld) {
        WorldData worldData = buildWorld.getData();
        Location spawn = worldData.getCustomSpawnLocation();
        if (spawn == null) {
            return "-";
        }
        return "XYZ: " + round(spawn.getX()) + " / " + round(spawn.getY()) + " / " + round(spawn.getZ());
    }

    private double round(double value) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(value * scale) / scale;
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.INFO;
    }
}