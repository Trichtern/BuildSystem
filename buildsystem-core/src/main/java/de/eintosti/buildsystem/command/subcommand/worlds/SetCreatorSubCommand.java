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

import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.command.subcommand.Argument;
import de.eintosti.buildsystem.command.subcommand.SubCommand;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import de.eintosti.buildsystem.util.PlayerChatInput;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.entity.Player;

import java.util.AbstractMap;

public class SetCreatorSubCommand implements SubCommand {

    private final BuildSystemPlugin plugin;
    private final String worldName;

    public SetCreatorSubCommand(BuildSystemPlugin plugin, String worldName) {
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
            Messages.sendMessage(player, "worlds_setcreator_usage");
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            Messages.sendMessage(player, "worlds_setcreator_unknown_world");
            return;
        }

        new PlayerChatInput(plugin, player, "enter_world_creator", input -> {
            String creator = input.trim();
            buildWorld.setCreator(creator);
            if (!creator.equalsIgnoreCase("-")) {
                buildWorld.setCreatorId(UUIDFetcher.getUUID(creator));
            } else {
                buildWorld.setCreatorId(null);
            }

            plugin.getPlayerManager().forceUpdateSidebar(buildWorld);
            XSound.ENTITY_PLAYER_LEVELUP.play(player);
            Messages.sendMessage(player, "worlds_setcreator_set", new AbstractMap.SimpleEntry<>("%world%", buildWorld.getName()));
            player.closeInventory();
        });
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.SET_CREATOR;
    }
}