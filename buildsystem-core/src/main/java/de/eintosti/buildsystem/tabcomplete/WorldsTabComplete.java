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
package de.eintosti.buildsystem.tabcomplete;

import com.google.common.collect.Lists;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.generator.Generator;
import de.eintosti.buildsystem.command.subcommand.Argument;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldsTabComplete extends ArgumentSorter implements TabCompleter {

    private final BuildWorldManager worldManager;

    public WorldsTabComplete(BuildSystemPlugin plugin) {
        this.worldManager = plugin.getWorldManager();
        plugin.getCommand("worlds").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (!(sender instanceof Player)) {
            return arrayList;
        }

        Player player = (Player) sender;

        switch (args.length) {
            case 1: {
                for (WorldsArgument argument : WorldsArgument.values()) {
                    String command = argument.getName();
                    String permission = argument.getPermission();

                    if (permission == null || player.hasPermission(permission)) {
                        addArgument(args[0], command, arrayList);
                    }
                }
                return arrayList;
            }

            case 2: {
                switch (args[0].toLowerCase()) {
                    case "builders":
                    case "edit":
                    case "info":
                    case "rename":
                    case "setcreator":
                    case "setitem":
                    case "setpermission":
                    case "setproject":
                    case "setstatus":
                    case "tp":
                    case "unimport":
                        worldManager.getBuildWorlds().stream()
                                .filter(world -> player.hasPermission(world.getData().permission().get()) || world.getData().permission().get().equalsIgnoreCase("-"))
                                .filter(world -> worldManager.isPermitted(player, "buildsystem." + args[0].toLowerCase(), world.getName()))
                                .forEach(world -> addArgument(args[1], world.getName(), arrayList));
                        break;

                    case "addbuilder":
                    case "delete":
                    case "removebuilder":
                        worldManager.getBuildWorlds().stream()
                                .filter(world -> worldManager.isPermitted(player, "buildsystem." + args[0].toLowerCase(), world.getName()))
                                .forEach(world -> addArgument(args[1], world.getName(), arrayList));
                        break;

                    case "import":
                        String[] directories = Bukkit.getWorldContainer().list((dir, name) -> {
                            for (String charString : name.split("")) {
                                if (charString.matches("[^A-Za-z0-9/_-]")) {
                                    return false;
                                }
                            }

                            File worldFolder = new File(dir, name);
                            if (!worldFolder.isDirectory()) {
                                return false;
                            }

                            if (!new File(worldFolder, "level.dat").exists()) {
                                return false;
                            }

                            return worldManager.getBuildWorld(name) == null;
                        });

                        if (directories == null || directories.length == 0) {
                            return arrayList;
                        }

                        for (String projectName : directories) {
                            addArgument(args[1], projectName, arrayList);
                        }
                        break;
                }
                return arrayList;
            }

            default:
                // Add arguments to /worlds import
                if (!args[0].equalsIgnoreCase("import")) {
                    return arrayList;
                }

                Map<String, List<String>> arguments = new HashMap<String, List<String>>() {{
                    put("-g", Arrays.stream(Generator.values()).filter(generator -> generator != Generator.CUSTOM).map(Enum::name).collect(Collectors.toList()));
                    put("-c", Lists.newArrayList());
                }};

                if (args.length % 2 == 1) {
                    arguments.keySet().stream()
                            .filter(key -> !Lists.newArrayList(args).contains(key))
                            .forEach(argument -> addArgument(args[args.length - 1], argument, arrayList));
                } else {
                    List<String> values = arguments.get(args[args.length - 2]);
                    if (values != null) {
                        for (String argument : values) {
                            addArgument(args[args.length - 1], argument, arrayList);
                        }
                    }
                }
                return arrayList;
        }
    }

    public enum WorldsArgument implements Argument {
        ADD_BUILDER("addBuilder", "buildsystem.addbuilder"),
        BUILDERS("builders", "buildsystem.builders"),
        DELETE("delete", "buildsystem.delete"),
        EDIT("edit", "buildsystem.edit"),
        HELP("help", null),
        IMPORT("import", "buildsystem.import"),
        IMPORT_ALL("importAll", "buildsystem.import.all"),
        INFO("info", "buildsystem.info"),
        ITEM("item", null),
        REMOVE_BUILDER("removeBuilder", "buildsystem.removebuilder"),
        RENAME("rename", "buildsystem.rename"),
        SET_CREATOR("setCreator", "buildsystem.setcreator"),
        SET_ITEM("setItem", "buildsystem.setitem"),
        SET_PERMISSION("setPermission", "buildsystem.setpermission"),
        SET_PROJECT("setProject", "buildsystem.setproject"),
        SET_STATUS("setStatus", "buildsystem.setstatus"),
        SET_SPAWN("setSpawn", "buildsystem.setspawn"),
        REMOVE_SPAWN("removeSpawn", "buildsystem.removespawn"),
        TP("tp", "buildsystem.worldtp"),
        UNIMPORT("unimport", "buildsystem.unimport");

        private final String command;
        private final String permission;

        WorldsArgument(String command, String permission) {
            this.command = command;
            this.permission = permission;
        }

        @Nullable
        public static WorldsArgument matchArgument(String input) {
            return Arrays.stream(values())
                    .filter(argument -> argument.getName().equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String getName() {
            return command;
        }

        @Override
        public String getPermission() {
            return permission;
        }
    }
}
