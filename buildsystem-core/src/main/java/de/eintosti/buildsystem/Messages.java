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
package de.eintosti.buildsystem;

import de.eintosti.buildsystem.util.color.ColorAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Messages {

    private static final Map<String, String> MESSAGES = new HashMap<>();
    private static final boolean PLACEHOLDER_API_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    private static YamlConfiguration config;

    public static void createMessageFile() {
        JavaPlugin plugin = JavaPlugin.getPlugin(BuildSystemPlugin.class);
        File file = new File(plugin.getDataFolder(), "messages.yml");
        try {
            if (file.createNewFile()) {
                plugin.getLogger().info("Created file: " + file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = YamlConfiguration.loadConfiguration(file);
        StringBuilder sb = new StringBuilder();

        addSpacer(sb, "# ██████╗ ██╗   ██╗██╗██╗     ██████╗ ███████╗██╗   ██╗███████╗████████╗███████╗███╗   ███╗");
        addSpacer(sb, "# ██╔══██╗██║   ██║██║██║     ██╔══██╗██╔════╝╚██╗ ██╔╝██╔════╝╚══██╔══╝██╔════╝████╗ ████║");
        addSpacer(sb, "# ██████╔╝██║   ██║██║██║     ██║  ██║███████╗ ╚████╔╝ ███████╗   ██║   █████╗  ██╔████╔██║");
        addSpacer(sb, "# ██╔══██╗██║   ██║██║██║     ██║  ██║╚════██║  ╚██╔╝  ╚════██║   ██║   ██╔══╝  ██║╚██╔╝██║");
        addSpacer(sb, "# ██████╔╝╚██████╔╝██║███████╗██████╔╝███████║   ██║   ███████║   ██║   ███████╗██║ ╚═╝ ██║");
        addSpacer(sb, "# ╚═════╝  ╚═════╝ ╚═╝╚══════╝╚═════╝ ╚══════╝   ╚═╝   ╚══════╝   ╚═╝   ╚══════╝╚═╝     ╚═╝");
        addSpacer(sb, "");
        addSpacer(sb, "");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# Messages");
        addSpacer(sb, "# ---------");
        setMessage(sb, "prefix", "&8▎ &bBuildSystem &8»");
        setMessage(sb, "player_join", "&7[&a+&7] &a%player%");
        setMessage(sb, "player_quit", "&7[&c-&7] &c%player%");
        setMessage(sb, "loading_world", "&7Loading &b%world%&7...");
        setMessage(sb, "world_not_loaded", "&cWorld is not loaded!");
        setMessage(sb, "enter_world_name", "&7Enter &bWorld Name");
        setMessage(sb, "enter_generator_name", "&7Enter &bGenerator Name");
        setMessage(sb, "enter_world_creator", "&7Enter &bWorld Creator");
        setMessage(sb, "enter_world_permission", "&7Enter &bPermission");
        setMessage(sb, "enter_world_project", "&7Enter &bProject");
        setMessage(sb, "enter_player_name", "&7Enter &bPlayer Name");
        setMessage(sb, "cancel_subtitle", "&7Type &ccancel &7to cancel");
        setMessage(sb, "input_cancelled", "%prefix% &cInput cancelled!");
        setMessage(sb, "update_available", Arrays.asList(
                "%prefix% &7Great! A new update is available &8[&bv%new_version%&8]",
                " &8➥ &7Your current version: &bv%current_version%"
        ));
        setMessage(sb, "command_archive_world", "%prefix% &cYou can't use that command here!");
        setMessage(sb, "command_not_builder", "%prefix% &cOnly builders can use that command!");
        addSpacer(sb, "");
        addSpacer(sb, "");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# Scoreboard");
        addSpacer(sb, "# ---------");
        setMessage(sb, "title", "&b&lBuildSystem");
        setMessage(sb, "body", Arrays.asList(
                "&7&m                     &8",
                "&7World:",
                " &b%world%",
                " ",
                "&7Status:",
                " %status%",
                "&7&m                     &7"
        ));
        addSpacer(sb, "");
        addSpacer(sb, "");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# Commands");
        addSpacer(sb, "# ---------");
        setMessage(sb, "sender_not_player", "You have to be a player to use this command!");
        setMessage(sb, "no_permissions", "%prefix% &cNot enough permissions.");
        addSpacer(sb, "");
        addSpacer(sb, "# /back");
        setMessage(sb, "back_usage", "%prefix% &7Usage: &b/back");
        setMessage(sb, "back_teleported", "%prefix% &7You were teleported to your &bprevious location&7.");
        setMessage(sb, "back_failed", "%prefix% &cNo previous location was found.");
        addSpacer(sb, "");
        addSpacer(sb, "# /build");
        setMessage(sb, "build_usage", "%prefix% &7Usage: &b/build [player]");
        setMessage(sb, "build_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "build_activated_self", "%prefix% &7Build mode was &aactivated&7.");
        setMessage(sb, "build_activated_other_sender", "%prefix% &7Build mode &8[&7for %target%&8] &7was &aactivated&7.");
        setMessage(sb, "build_activated_other_target", "%prefix% &7Build mode was &aactivated &8[&7by %sender%&8]&7.");
        setMessage(sb, "build_deactivated_self", "%prefix% &7Build mode was &cdeactivated&7.");
        setMessage(sb, "build_deactivated_other_sender", "%prefix% &7Build mode &8[&7for %target%&8] &7was &cdeactivated&7.");
        setMessage(sb, "build_deactivated_other_target", "%prefix% &7Build mode was &cdeactivated &8[&7by %sender%&8]&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /buildsystem");
        setMessage(sb, "buildsystem_usage", "%prefix% &7Usage: &b/buildsystem [page]");
        setMessage(sb, "buildsystem_invalid_page", "%prefix% &cInvalid page.");
        setMessage(sb, "buildsystem_title_with_page", "%prefix% &7&nBuildSystem Help:&8 (&7%page%/%max%&8)");
        setMessage(sb, "buildsystem_permission", "&7&nPermission&8: &b%permission%");
        setMessage(sb, "buildsystem_back", "&7Teleport to your previous location.");
        setMessage(sb, "buildsystem_blocks", "&7Opens a menu with secret blocks.");
        setMessage(sb, "buildsystem_build", "&7Puts you into 'build mode'.");
        setMessage(sb, "buildsystem_config", "&7Reload the config.");
        setMessage(sb, "buildsystem_day", "&7Set a world's time to daytime.");
        setMessage(sb, "buildsystem_explosions", "&7Toggle explosions.");
        setMessage(sb, "buildsystem_gamemode", "&7Change your gamemode.");
        setMessage(sb, "buildsystem_night", "&7Set a world's time to nighttime.");
        setMessage(sb, "buildsystem_noai", "&7Toggle entity AIs.");
        setMessage(sb, "buildsystem_physics", "&7Toggle block physics.");
        setMessage(sb, "buildsystem_settings", "&7Manage user settings.");
        setMessage(sb, "buildsystem_setup", "&7Change the default items when creating worlds.");
        setMessage(sb, "buildsystem_skull", "&7Receive a player or custom skull.");
        setMessage(sb, "buildsystem_speed", "&7Change your flying/walking speed.");
        setMessage(sb, "buildsystem_spawn", "&7Teleport to the spawn.");
        setMessage(sb, "buildsystem_top", "&7Teleport to the the highest location above you.");
        setMessage(sb, "buildsystem_worlds", "&7An overview of all &o/worlds &7commands.");
        addSpacer(sb, "");
        addSpacer(sb, "# /config");
        setMessage(sb, "config_usage", "%prefix% &7Usage: &b/config reload");
        setMessage(sb, "config_reloaded", "%prefix% &7The config was reloaded.");
        addSpacer(sb, "");
        addSpacer(sb, "# /explosions");
        setMessage(sb, "explosions_usage", "%prefix% &7Usage: &b/explosions <world>");
        setMessage(sb, "explosions_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "explosions_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "explosions_activated", "%prefix% &7Explosions in &b%world% &7were &aactivated&7.");
        setMessage(sb, "explosions_deactivated", "%prefix% &7Explosions in &b%world% &7were &cdeactivated&7.");
        setMessage(sb, "explosions_deactivated_in_world", "%prefix% &7Explosions in &b%world% &7are currently &cdeactivated&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /gamemode");
        setMessage(sb, "gamemode_usage", "%prefix% &7Usage: &b/gamemode <mode> [player]");
        setMessage(sb, "gamemode_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "gamemode_survival", "Survival");
        setMessage(sb, "gamemode_creative", "Creative");
        setMessage(sb, "gamemode_adventure", "Adventure");
        setMessage(sb, "gamemode_spectator", "Spectator");
        setMessage(sb, "gamemode_set_self", "%prefix% &7Your gamemode was set to &b%gamemode%&7.");
        setMessage(sb, "gamemode_set_other", "%prefix% &b%target%&7's gamemode was set to &b%gamemode%&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /physics");
        setMessage(sb, "physics_usage", "%prefix% &7Usage: &b/physics <world>");
        setMessage(sb, "physics_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "physics_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "physics_activated", "%prefix% &7Physics in &b%world% &7were &aactivated&7.");
        setMessage(sb, "physics_activated_all", "%prefix% &7Physics in &ball worlds &7were &aactivated&7.");
        setMessage(sb, "physics_deactivated", "%prefix% &7Physics in &b%world% &7were &cdeactivated&7.");
        setMessage(sb, "physics_deactivated_in_world", "%prefix% &7Physics in &b%world% &7are currently &cdeactivated&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /noai");
        setMessage(sb, "noai_usage", "%prefix% &7Usage: &b/noai <world>");
        setMessage(sb, "noai_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "noai_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "noai_deactivated", "%prefix% &7Entity AIs in &b%world% &7were &aactivated&7.");
        setMessage(sb, "noai_activated", "%prefix% &7Entity AIs in &b%world% &7were &cdeactivated&7.");
        setMessage(sb, "noai_activated_in_world", "%prefix% &7Entity AIs in &b%world% &7are currently &cdeactivated&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /skull");
        setMessage(sb, "skull_usage", "%prefix% &7Usage: &b/skull [name]");
        setMessage(sb, "skull_player_received", "%prefix% &7You received the skull of &b%player%&7.");
        setMessage(sb, "skull_custom_received", "%prefix% &7You received a &bcustom skull&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /spawn");
        setMessage(sb, "spawn_usage", "%prefix% &7Usage: &b/spawn");
        setMessage(sb, "spawn_admin", "%prefix% &7Usage: &b/spawn [set/remove]");
        setMessage(sb, "spawn_teleported", "%prefix% &7You were teleported to the &bspawn&7.");
        setMessage(sb, "spawn_unavailable", "%prefix% &cThere isn't a spawn to teleport to.");
        setMessage(sb, "spawn_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "spawn_set", "%prefix% &7Spawn set to &b%x% %y% %z% &7in &b%world%&7.");
        setMessage(sb, "spawn_remove", "%prefix% &7The spawn was removed.");
        addSpacer(sb, "");
        addSpacer(sb, "# /speed");
        setMessage(sb, "speed_usage", "%prefix% &7Usage: &b/speed [1-5]");
        setMessage(sb, "speed_set_flying", "%prefix% &7Your flying speed was set to &b%speed%&7.");
        setMessage(sb, "speed_set_walking", "%prefix% &7Your walking speed was set to &b%speed%&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /day");
        setMessage(sb, "day_usage", "%prefix% &7Usage: &b/day [world]");
        setMessage(sb, "day_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "day_set", "%prefix% &7It is now day in &b%world%&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /night");
        setMessage(sb, "night_usage", "%prefix% &7Usage: &b/night [world]");
        setMessage(sb, "night_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "night_set", "%prefix% &7It is now night in &b%world%&7.");
        addSpacer(sb, "");
        addSpacer(sb, "# /top");
        setMessage(sb, "top_usage", "%prefix% &7Usage: &b/top");
        setMessage(sb, "top_teleported", "%prefix% &7You were teleported to the &btop&7.");
        setMessage(sb, "top_failed", "%prefix% &cNo higher location was found.");
        addSpacer(sb, "");
        addSpacer(sb, "# /worlds");
        setMessage(sb, "worlds_addbuilder_usage", "%prefix% &7Usage: &b/worlds addBuilder <world>");
        setMessage(sb, "worlds_addbuilder_error", "%prefix% &cError: Please try again!");
        setMessage(sb, "worlds_addbuilder_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_addbuilder_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "worlds_addbuilder_already_creator", "%prefix% &cYou are already the creator.");
        setMessage(sb, "worlds_addbuilder_already_added", "%prefix% &cThis player is already a builder.");
        setMessage(sb, "worlds_addbuilder_added", "%prefix% &b%builder% &7was &aadded &7as a builder.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_builders_usage", "%prefix% &7Usage: &b/worlds builders <world>");
        setMessage(sb, "worlds_builders_unknown_world", "%prefix% &cUnknown world.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_world_name", "World name");
        setMessage(sb, "worlds_world_exists", "%prefix% &cThis world already exists.");
        setMessage(sb, "worlds_world_creation_invalid_characters", "%prefix% &7&oRemoved invalid characters from world name.");
        setMessage(sb, "worlds_world_creation_name_bank", "%prefix% &cThe world name cannot be blank.");
        setMessage(sb, "worlds_world_creation_started", "%prefix% &7The creation of &b%world% &8(&7Type: &f%type%&8) &7has started...");
        setMessage(sb, "worlds_template_creation_started", "%prefix% &7The creation of &b%world% &8(&7Template: &f%template%&8) &7has started...");
        setMessage(sb, "worlds_creation_finished", "%prefix% &7The world was &asuccessfully &7created.");
        setMessage(sb, "worlds_template_does_not_exist", "%prefix% &cThis template does not exist.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_unknown_command", "%prefix% &7Unknown command: &b/worlds help");
        setMessage(sb, "worlds_navigator_open", "%prefix% &cYou have already opened the navigator!");
        addSpacer(sb, "");
        setMessage(sb, "worlds_delete_usage", "%prefix% &7Usage: &b/worlds delete <world>");
        setMessage(sb, "worlds_delete_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_delete_unknown_directory", "%prefix% &cError while deleting world: Directory not found!");
        setMessage(sb, "worlds_delete_error", "%prefix% &cError while deleting world: Please try again!");
        setMessage(sb, "worlds_delete_canceled", "%prefix% &7The deletion of &b%world% &7was canceled.");
        setMessage(sb, "worlds_delete_started", "%prefix% &7The deletion of &b%world% &7has started...");
        setMessage(sb, "worlds_delete_finished", "%prefix% &7The world was &asuccessfully &7deleted.");
        setMessage(sb, "worlds_delete_players_world", "%prefix% &7&oThe world you were in was deleted.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_edit_usage", "%prefix% &7Usage: &b/worlds edit <world>");
        setMessage(sb, "worlds_edit_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_edit_error", "%prefix% &cError: Please try again.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_help_usage", "%prefix% &7Usage: &b/worlds help [page]");
        setMessage(sb, "worlds_help_invalid_page", "%prefix% &cInvalid page.");
        setMessage(sb, "worlds_help_title_with_page", "%prefix% &7&nWorlds Help:&8 (&7%page%/%max%&8)");
        setMessage(sb, "worlds_help_permission", "&7&nPermission&8: &b%permission%");
        setMessage(sb, "worlds_help_help", "&7Shows the list of all subcommands.");
        setMessage(sb, "worlds_help_info", "&7Shows information about a world.");
        setMessage(sb, "worlds_help_item", "&7Receive the 'World Navigator'.");
        setMessage(sb, "worlds_help_tp", "&7Teleport to another world.");
        setMessage(sb, "worlds_help_edit", "&7Opens the world editor.");
        setMessage(sb, "worlds_help_addbuilder", "&7Add a builder to a world.");
        setMessage(sb, "worlds_help_removebuilder", "&7Remove a builder from a &7world.");
        setMessage(sb, "worlds_help_builders", "&7Opens a world's list of builders.");
        setMessage(sb, "worlds_help_rename", "&7Rename an existing world.");
        setMessage(sb, "worlds_help_setitem", "&7Set a world's item.");
        setMessage(sb, "worlds_help_setcreator", "&7Set a world's creator.");
        setMessage(sb, "worlds_help_setproject", "&7Set a world's project.");
        setMessage(sb, "worlds_help_setpermission", "&7Set a world's permission.");
        setMessage(sb, "worlds_help_setstatus", "&7Set a world's status.");
        setMessage(sb, "worlds_help_setspawn", "&7Set a world's spawnpoint.");
        setMessage(sb, "worlds_help_removespawn", "&7Removes a world's spawnpoint.");
        setMessage(sb, "worlds_help_delete", "&7Delete a world.");
        setMessage(sb, "worlds_help_import", "&7Import a world.");
        setMessage(sb, "worlds_help_importall", "&7Import all worlds at once.");
        setMessage(sb, "worlds_help_unimport", "&7Unimport a world.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_import_usage", "%prefix% &7Usage: &b/worlds import <world> [-g <generator> | -c <creator>]");
        setMessage(sb, "worlds_import_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_import_world_is_imported", "%prefix% &cThis world is already imported.");
        setMessage(sb, "worlds_import_unknown_generator", "%prefix% &cUnknown generator.");
        setMessage(sb, "worlds_import_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "worlds_import_newer_version", "%prefix% &b%world% &7was created in a &cnewer version &7of Minecraft. Unable to import.");
        setMessage(sb, "worlds_import_started", "%prefix% &7The import of &b%world% &7has started...");
        setMessage(sb, "worlds_import_invalid_character", "%prefix% &7Unable to import &c%world%&7.\n" +
                "%prefix% &7&oName contains invalid character: &c%char%");
        setMessage(sb, "worlds_import_finished", "%prefix% &7The world was &asuccessfully &7imported.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_importall_usage", "%prefix% &7Usage: &b/worlds importall [-g <generator> | -c <creator>]");
        setMessage(sb, "worlds_importall_no_worlds", "%prefix% &cNo worlds were found.");
        setMessage(sb, "worlds_importall_started", "%prefix% &7Beginning import of &b%amount% &7worlds...");
        setMessage(sb, "worlds_importall_delay", "%prefix% &8➥ &7Delay between each world: &b%delay%s&7.");
        setMessage(sb, "worlds_importall_already_started", "%prefix% &cAll worlds are already being imported.");
        setMessage(sb, "worlds_importall_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "worlds_importall_invalid_character", "%prefix% &c✘ &7&o%world% &7contains invalid character &8(&c%char%&8)");
        setMessage(sb, "worlds_importall_world_already_imported", "%prefix% &c✘ &7World already imported: &b%world%");
        setMessage(sb, "worlds_importall_newer_version", "%prefix% &c✘ &b%world% &7was created in a &cnewer version &7of Minecraft");
        setMessage(sb, "worlds_importall_world_imported", "%prefix% &a✔ &7World imported: &b%world%");
        setMessage(sb, "worlds_importall_finished", "%prefix% &7All worlds have been &asuccessfully &7imported.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_info_usage", "%prefix% &7Usage: &b/worlds info [world]");
        setMessage(sb, "worlds_info_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "world_info", Arrays.asList(
                "&7&m-------------------------------------",
                "%prefix% &7&nWorld info:&b %world%",
                " ",
                " &8- &7Creator: &b%creator%",
                " &8- &7Type: &b%type%",
                " &8- &7Private: &b%private%",
                " &8- &7Builders enabled: &b%builders_enabled%",
                " &8- &7Builders: &b%builders%",
                " &8- &7Item: &b%item%",
                " &8- &7Status: &b%status%",
                " &8- &7Project: &b%project%",
                " &8- &7Permission: &b%permission%",
                " &8- &7Time: &b%time%",
                " &8- &7Creation date: &b%creation%",
                " &8- &7Physics: &b%physics%",
                " &8- &7Explosions: &b%explosions%",
                " &8- &7Block breaking: &b%block_breaking%",
                " &8- &7Block placement: &b%block_placement%",
                " &8- &7MobAI: &b%mobai%",
                " &8- &7Custom spawn: &b%custom_spawn%",
                "&7&m-------------------------------------"));
        addSpacer(sb, "");
        setMessage(sb, "worlds_item_receive", "%prefix% &7You received the &bNavigator&7.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_removebuilder_usage", "%prefix% &7Usage: &b/worlds removeBuilder <world>");
        setMessage(sb, "worlds_removebuilder_error", "%prefix% &cError: Please try again!");
        setMessage(sb, "worlds_removebuilder_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_removebuilder_player_not_found", "%prefix% &cThat player was not found.");
        setMessage(sb, "worlds_removebuilder_not_yourself", "%prefix% &cYou cannot remove yourself as creator.");
        setMessage(sb, "worlds_removebuilder_not_builder", "%prefix% &cThis player is not a builder.");
        setMessage(sb, "worlds_removebuilder_removed", "%prefix% &b%builder% &7was &cremoved &7as a builder.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_rename_usage", "%prefix% &7Usage: &b/worlds rename <world>");
        setMessage(sb, "worlds_rename_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_rename_error", "%prefix% &cPlease try again.");
        setMessage(sb, "worlds_rename_same_name", "%prefix% &cThis is the world's current name.");
        setMessage(sb, "worlds_rename_set", "%prefix% &b%oldName% &7was successfully renamed to &b%newName%&7.");
        setMessage(sb, "worlds_rename_players_world", "%prefix% &7&oThe world you are in is being renamed...");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setitem_usage", "%prefix% &7Usage: &b/worlds setItem <world>");
        setMessage(sb, "worlds_setitem_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_setitem_hand_empty", "%prefix% &cYou do not have an item in your hand.");
        setMessage(sb, "worlds_setitem_set", "%prefix% &b%world%&7's item was successfully changed.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setcreator_usage", "%prefix% &7Usage: &b/worlds setCreator <world>");
        setMessage(sb, "worlds_setcreator_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_setcreator_error", "%prefix% &cPlease try again.");
        setMessage(sb, "worlds_setcreator_set", "%prefix% &b%world%&7's creator was successfully changed.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setproject_usage", "%prefix% &7Usage: &b/worlds setProject <world>");
        setMessage(sb, "worlds_setproject_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_setproject_error", "%prefix% &cPlease try again.");
        setMessage(sb, "worlds_setproject_set", "%prefix% &b%world%&7's project was successfully changed.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setstatus_usage", "%prefix% &7Usage: &b/worlds setStatus <world>");
        setMessage(sb, "worlds_setstatus_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_setstatus_error", "%prefix% &cPlease try again.");
        setMessage(sb, "worlds_setstatus_set", "%prefix% &b%world%&7's status was was changed to: %status%&7.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setpermission_usage", "%prefix% &7Usage: &b/worlds setPermission <world>");
        setMessage(sb, "worlds_setpermission_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_setpermission_error", "%prefix% &cPlease try again.");
        setMessage(sb, "worlds_setpermission_set", "%prefix% &b%world%&7's permission was successfully changed.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_setspawn_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "worlds_setspawn_world_spawn_set", "%prefix% &b%world%&7's spawnpoint was set.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_removespawn_world_not_imported", "%prefix% &cWorld must be imported » /worlds import <world>");
        setMessage(sb, "worlds_removespawn_world_spawn_removed", "%prefix% &b%world%&7's spawnpoint was removed.");
        addSpacer(sb, "");
        setMessage(sb, "worlds_tp_usage", "%prefix% &7Usage: &b/worlds tp <world>");
        setMessage(sb, "worlds_tp_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_tp_world_not_imported", "%prefix% &cWorld must be imported: /worlds import <world>");
        setMessage(sb, "worlds_tp_entry_forbidden", "%prefix% &cYou are not allowed to enter this world!");
        addSpacer(sb, "");
        setMessage(sb, "worlds_unimport_usage", "%prefix% &7Usage: &b/worlds unimport <world>");
        setMessage(sb, "worlds_unimport_unknown_world", "%prefix% &cUnknown world.");
        setMessage(sb, "worlds_unimport_players_world", "%prefix% &7&oThe world you were in was unimported.");
        setMessage(sb, "worlds_unimport_finished", "%prefix% &b%world% &7has been &aunimported&7.");
        addSpacer(sb, "");
        addSpacer(sb, "");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# Items");
        addSpacer(sb, "# ---------");
        setMessage(sb, "navigator_item", "&b&lNavigator");
        setMessage(sb, "barrier_item", "&c&lClose Inventory");
        setMessage(sb, "custom_skull_item", "&bCustom Skull");
        addSpacer(sb, "");
        addSpacer(sb, "");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# GUIs");
        addSpacer(sb, "# ---------");
        addSpacer(sb, "# Multi-page inventory");
        setMessage(sb, "gui_previous_page", "&b« &7Previous Page");
        setMessage(sb, "gui_next_page", "&7Next Page &b»");
        addSpacer(sb, "");
        addSpacer(sb, "# Old Navigator");
        setMessage(sb, "old_navigator_title", "&3» &8Navigator");
        setMessage(sb, "old_navigator_world_navigator", "&aWorld Navigator");
        setMessage(sb, "old_navigator_world_archive", "&6World Archive");
        setMessage(sb, "old_navigator_private_worlds", "&bPrivate Worlds");
        setMessage(sb, "old_navigator_settings", "&cSettings");
        addSpacer(sb, "");
        addSpacer(sb, "# New Navigator");
        setMessage(sb, "new_navigator_world_navigator", "&a&lWORLD NAVIGATOR");
        setMessage(sb, "new_navigator_world_archive", "&6&lWORLD ARCHIVE");
        setMessage(sb, "new_navigator_private_worlds", "&b&lPRIVATE WORLDS");
        addSpacer(sb, "");
        addSpacer(sb, "# World Navigator");
        setMessage(sb, "world_navigator_title", "&3» &8World Navigator");
        setMessage(sb, "world_navigator_no_worlds", "&c&nNo worlds available");
        setMessage(sb, "world_navigator_create_world", "&bCreate World");
        setMessage(sb, "world_item_title", "&3&l%world%");
        setMessage(sb, "world_item_lore_normal", Arrays.asList(
                "&7Status&8: %status%",
                "",
                "&7Creator&8: &b%creator%",
                "&7Project&8: &b%project%",
                "&7Permission&8: &b%permission%",
                "",
                "&7Builders&8:",
                "%builders%"
        ));
        setMessage(sb, "world_item_lore_edit", Arrays.asList(
                "&7Status&8: %status%",
                "",
                "&7Creator&8: &b%creator%",
                "&7Project&8: &b%project%",
                "&7Permission&8: &b%permission%",
                "",
                "&7Builders&8:",
                "%builders%",
                "",
                "&8- &7&oLeft click&8: &7Teleport",
                "&8- &7&oRight click&8: &7Edit"
        ));
        setMessage(sb, "world_item_builders_builder_template", "&b%builder%&7, ");
        setMessage(sb, "world_sort_title", "&bSort");
        setMessage(sb, "world_sort_name_az", "&8» &7&oName (A-Z)");
        setMessage(sb, "world_sort_name_za", "&8» &7&oName (Z-A)");
        setMessage(sb, "world_sort_project_az", "&8» &7&oProject (A-Z)");
        setMessage(sb, "world_sort_project_za", "&8» &7&oProject (Z-A)");
        setMessage(sb, "world_sort_status_not_started", "&8» &7&oNot Started &8&o➡ &7&oFinished");
        setMessage(sb, "world_sort_status_finished", "&8» &7&oFinished &8&o➡ &7&oNot Started");
        setMessage(sb, "world_sort_date_newest", "&8» &7&oCreation date (Newest)");
        setMessage(sb, "world_sort_date_oldest", "&8» &7&oCreation date (Oldest)");
        setMessage(sb, "world_filter_title", "&bFilter");
        setMessage(sb, "world_filter_mode_none", "&8» &7&oNone");
        setMessage(sb, "world_filter_mode_starts_with", "&8» &7&oStarts with: &b&o%text%");
        setMessage(sb, "world_filter_mode_contains", "&8» &7&oContains: &b&o%text%");
        setMessage(sb, "world_filter_mode_matches", "&8» &7&oMatches: &b&o%text%");
        setMessage(sb, "world_filter_lore", Arrays.asList(
                "",
                "&8- &7&oLeft click&8: &7Change text",
                "&8- &7&oRight click&8: &7Change mode",
                "&8- &7&oShift click&8: &7Reset to default"
        ));
        addSpacer(sb, "");
        addSpacer(sb, "# World Archive");
        setMessage(sb, "archive_title", "&3» &8World Archive");
        setMessage(sb, "archive_no_worlds", "&c&nNo worlds available");
        addSpacer(sb, "");
        addSpacer(sb, "# Private Worlds");
        setMessage(sb, "private_title", "&3» &8Private Worlds");
        setMessage(sb, "private_no_worlds", "&c&nNo worlds available");
        setMessage(sb, "private_create_world", "&bCreate a private world");
        addSpacer(sb, "");
        addSpacer(sb, "# Setup");
        setMessage(sb, "setup_title", "&3» &8Setup");
        setMessage(sb, "setup_create_item_name", "&bCreate World Item");
        setMessage(sb, "setup_create_item_lore", Arrays.asList(
                "&7The item which is shown",
                "&7when you create a world.",
                "", "&7&nTo change&7:",
                "&8» &7&oDrag new item onto old one"
        ));
        setMessage(sb, "setup_default_item_name", "&bDefault Item");
        setMessage(sb, "setup_default_item_lore", Arrays.asList(
                "&7The item which a world",
                "&7has by default when created.",
                "",
                "&7&nTo change&7:",
                "&8» &7&oDrag new item onto old one"
        ));
        setMessage(sb, "setup_status_item_name", "&bStatus Item");
        setMessage(sb, "setup_status_item_name_lore", Arrays.asList(
                "&7The item which is shown when",
                "&7you change a world's status.",
                "",
                "&7&nTo change&7:",
                "&8» &7&oDrag new item onto old one"
        ));
        setMessage(sb, "setup_normal_world", "&bNormal World");
        setMessage(sb, "setup_flat_world", "&aFlat World");
        setMessage(sb, "setup_nether_world", "&cNether World");
        setMessage(sb, "setup_end_world", "&eEnd World");
        setMessage(sb, "setup_void_world", "&fEmpty World");
        setMessage(sb, "setup_imported_world", "&7Imported World");
        addSpacer(sb, "");
        addSpacer(sb, "# Create World");
        setMessage(sb, "create_title", "&3» &8Create World");
        setMessage(sb, "create_predefined_worlds", "&6Predefined Worlds");
        setMessage(sb, "create_templates", "&6Templates");
        setMessage(sb, "create_generators", "&6Generators");
        setMessage(sb, "create_no_templates", "&c&nNo templates available");
        setMessage(sb, "create_normal_world", "&bNormal World");
        setMessage(sb, "create_flat_world", "&aFlat World");
        setMessage(sb, "create_nether_world", "&cNether World");
        setMessage(sb, "create_end_world", "&eEnd World");
        setMessage(sb, "create_void_world", "&fEmpty World");
        setMessage(sb, "create_template", "&e%template%");
        setMessage(sb, "create_generators_create_world", "&eCreate world with generator");
        addSpacer(sb, "");
        addSpacer(sb, "# World Type");
        setMessage(sb, "type_normal", "Normal");
        setMessage(sb, "type_flat", "Flat");
        setMessage(sb, "type_nether", "Nether");
        setMessage(sb, "type_end", "End");
        setMessage(sb, "type_void", "Void");
        setMessage(sb, "type_custom", "Custom");
        setMessage(sb, "type_template", "Template");
        setMessage(sb, "type_private", "Private");
        addSpacer(sb, "");
        addSpacer(sb, "# World Status");
        setMessage(sb, "status_title", "&8Status &7» &3%world%");
        setMessage(sb, "status_not_started", "&cNot Started");
        setMessage(sb, "status_in_progress", "&6In Progress");
        setMessage(sb, "status_almost_finished", "&aAlmost Finished");
        setMessage(sb, "status_finished", "&2Finished");
        setMessage(sb, "status_archive", "&9Archive");
        setMessage(sb, "status_hidden", "&fHidden");
        addSpacer(sb, "");
        addSpacer(sb, "# World Difficulty");
        setMessage(sb, "difficulty_peaceful", "&fPeaceful");
        setMessage(sb, "difficulty_easy", "&aEasy");
        setMessage(sb, "difficulty_normal", "&6Normal");
        setMessage(sb, "difficulty_hard", "&cHard");
        addSpacer(sb, "");
        addSpacer(sb, "# Delete World");
        setMessage(sb, "delete_title", "&3» &8Delete World");
        setMessage(sb, "delete_world_name", "&e%world%");
        setMessage(sb, "delete_world_name_lore", Arrays.asList(
                "",
                "&c&nWarning&c: &7&oOnce a world is",
                "&7&odeleted it is lost forever",
                "&7&oand cannot be recovered!"
        ));
        setMessage(sb, "delete_world_cancel", "&cCancel");
        setMessage(sb, "delete_world_confirm", "&aConfirm");
        addSpacer(sb, "");
        addSpacer(sb, "# World Editor");
        setMessage(sb, "worldeditor_title", "&3» &8World Editor");
        setMessage(sb, "worldeditor_world_item", "&3&l%world%");
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_blockbreaking_item", "&bBlock Breaking");
        setMessage(sb, "worldeditor_blockbreaking_lore", Arrays.asList(
                "&7&oToggle whether or not blocks",
                "&7&oare able to be broken."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_blockplacement_item", "&bBlock Placement");
        setMessage(sb, "worldeditor_blockplacement_lore", Arrays.asList(
                "&7&oToggle whether or not blocks",
                "&7&oare able to be placed."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_physics_item", "&bBlock Physics");
        setMessage(sb, "worldeditor_physics_lore", Arrays.asList(
                "&7&oToggle whether or not block",
                "&7&ophysics are activated."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_time_item", "&bTime");
        setMessage(sb, "worldeditor_time_lore", Arrays.asList(
                "&7&oAlter the time of day",
                "&7&oin the world.",
                "",
                "&7&nCurrently&7: %time%"
        ));
        setMessage(sb, "worldeditor_time_lore_sunrise", "&6Sunrise");
        setMessage(sb, "worldeditor_time_lore_noon", "&eNoon");
        setMessage(sb, "worldeditor_time_lore_night", "&9Night");
        setMessage(sb, "worldeditor_time_lore_unknown", "&fUnknown");
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_explosions_item", "&bExplosions");
        setMessage(sb, "worldeditor_explosions_lore", Arrays.asList(
                "&7&oToggle whether or not",
                "&7&oexplosions are activated."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_butcher_item", "&bButcher");
        setMessage(sb, "worldeditor_butcher_lore", Collections.singletonList("&7&oKill all the mobs in the world."));
        setMessage(sb, "worldeditor_butcher_removed", "%prefix% &b%amount% &7mobs were removed.");
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_builders_item", "&bBuilders");
        setMessage(sb, "worldeditor_builders_lore", Arrays.asList(
                "&7&oManage which players can",
                "&7&obuild in the world.",
                "",
                "&8- &7&oLeft click&8: &7Toggle feature",
                "&8- &7&oRight click&8: &7Manage builders"));
        setMessage(sb, "worldeditor_builders_not_creator_item", "&c&mBuilders");
        setMessage(sb, "worldeditor_builders_not_creator_lore", Arrays.asList(
                "&7&oYou are not the creator",
                "&7&oof this world."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_builders_title", "&3» &8Builders");
        setMessage(sb, "worldeditor_builders_creator_item", "&7&nCreator&7:");
        setMessage(sb, "worldeditor_builders_creator_lore", "&8» &b%creator%");
        setMessage(sb, "worldeditor_builders_no_creator_item", "&cWorld has no creator!");
        setMessage(sb, "worldeditor_builders_builder_item", "&b%builder%");
        setMessage(sb, "worldeditor_builders_builder_lore", Collections.singletonList("&8- &7&oShift click&8: &7Remove"));
        setMessage(sb, "worldeditor_builders_add_builder_item", "&bAdd builder");
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_gamerules_item", "&bGamerules");
        setMessage(sb, "worldeditor_gamerules_lore", Collections.singletonList("&7&oManage the world's gamerules"));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_gamerules_title", "&3» &8Gamerules");
        setMessage(sb, "worldeditor_gamerules_boolean_enabled", Collections.singletonList("&7&nCurrently&7: &atrue"));
        setMessage(sb, "worldeditor_gamerules_boolean_disabled", Collections.singletonList("&7&nCurrently&7: &cfalse"));
        setMessage(sb, "worldeditor_gamerules_integer", Arrays.asList(
                "&7&nCurrently&7: &e%value%",
                "",
                "&8- &7&oLeft Click&8: &7decrease by 1",
                "&8- &7&oShift &7+ &7&oLeft Click&8: &7decrease by 10",
                "&8- &7&oRight Click&8: &7increase by 1",
                "&8- &7&oShift &7+ &7&oRight Click&8: &7increase by 10"));
        setMessage(sb, "worldsettings_gamerule_", Arrays.asList(
                "",
                "&c&nWarning&c: &7&oOnce a world is",
                "&7&odeleted it is lost forever",
                "&7&oand cannot be recovered!"
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_visibility_item", "&bVisibility");
        setMessage(sb, "worldeditor_visibility_lore_public", Arrays.asList(
                "&7&oChange the world's visibility",
                "",
                "&7&nCurrently&7: &bPublic"));
        setMessage(sb, "worldeditor_visibility_lore_private", Arrays.asList(
                "&7&oChange the world's visibility",
                "",
                "&7&nCurrently&7: &bPrivate"
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_mobai_item", "&bMob AI");
        setMessage(sb, "worldeditor_mobai_lore", Collections.singletonList("&7&oToggle whether mobs have an AI."));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_blockinteractions_item", "&bBlock Interactions");
        setMessage(sb, "worldeditor_blockinteractions_lore", Arrays.asList(
                "&7&oToggle whether interactions",
                "&7&owith blocks are cancelled."
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_difficulty_item", "&bDifficulty");
        setMessage(sb, "worldeditor_difficulty_lore", Arrays.asList(
                "&7&oChange the world's difficulty.",
                "",
                "&7&nCurrently&7: %difficulty%"
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_status_item", "&bStatus");
        setMessage(sb, "worldeditor_status_lore", Arrays.asList(
                "&7&oChange the world's status.",
                "",
                "&7&nCurrently&7: %status%"
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_project_item", "&bProject");
        setMessage(sb, "worldeditor_project_lore", Arrays.asList(
                "&7&oChange the world's project.",
                "",
                "&7&nCurrently&7: &b%project%"
        ));
        addSpacer(sb, "");
        setMessage(sb, "worldeditor_permission_item", "&bPermission");
        setMessage(sb, "worldeditor_permission_lore", Arrays.asList(
                "&7&oChange the world's permission.",
                "",
                "&7&nCurrently&7: &b%permission%"
        ));
        addSpacer(sb, "");
        addSpacer(sb, "# Settings");
        setMessage(sb, "settings_title", "&3» &8Settings");
        addSpacer(sb, "");
        setMessage(sb, "settings_change_design_item", "&bChange Design");
        setMessage(sb, "settings_change_design_lore", Arrays.asList(
                "&7&oSelect which colour the",
                "&7&oglass panes should have."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_clear_inventory_item", "&bClear Inventory");
        setMessage(sb, "settings_clear_inventory_lore", Arrays.asList(
                "&7&oWhen enabled, a player's",
                "&7&oinventory is cleared on join."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_disableinteract_item", "&bDisable Block Interactions");
        setMessage(sb, "settings_disableinteract_lore", Arrays.asList(
                "&7&oWhen enabled, interactions with",
                "&7&ocertain blocks are disabled."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_hideplayers_item", "&bHide Players");
        setMessage(sb, "settings_hideplayers_lore", Arrays.asList(
                "&7&oWhen enabled, all online",
                "&7&oplayers will be hidden."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_instantplacesigns_item", "&bInstant Place Signs");
        setMessage(sb, "settings_instantplacesigns_lore", Arrays.asList(
                "&7&oWhen enabled, signs are placed",
                "&7&owithout opening the text input."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_keep_navigator_item", "&bKeep Navigator");
        setMessage(sb, "settings_keep_navigator_lore", Arrays.asList(
                "&7&oWhen enabled, the navigator",
                "&7&owill remain in your inventory",
                "&7&oeven when you clear it."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_new_navigator_item", "&bNew Navigator");
        setMessage(sb, "settings_new_navigator_lore", Arrays.asList(
                "&7&oA new and improved navigator",
                "&7&owhich is no longer a GUI."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_nightvision_item", "&bNightvision");
        setMessage(sb, "settings_nightvision_lore", Arrays.asList(
                "&7&oWhen enabled, you will",
                "&7&oreceive permanent night vision."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_no_clip_item", "&bNoClip");
        setMessage(sb, "settings_no_clip_lore", Arrays.asList(
                "&7&oWhen flying against a wall, you",
                "&7&owill be put into spectator mode."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_open_trapdoors_item", "&bOpen Trapdoors");
        setMessage(sb, "settings_open_trapdoors_lore", Arrays.asList(
                "&7&oWhen right clicking iron (trap-)doors,",
                "&7&othey will be opened/closed."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_placeplants_item", "&bPlace Plants");
        setMessage(sb, "settings_placeplants_lore", Arrays.asList(
                "&7&oWhen enabled, you can place",
                "&7&oplants on every kind of block."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_scoreboard_item", "&bScoreboard");
        setMessage(sb, "settings_scoreboard_lore", Arrays.asList(
                "&7&oA scoreboard which provides",
                "&7&oyou with useful information."
        ));
        setMessage(sb, "settings_scoreboard_disabled_item", "&c&mScoreboard");
        setMessage(sb, "settings_scoreboard_disabled_lore", Arrays.asList(
                "&7&oThe scoreboard has been",
                "&7&odisabled in the config."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_slab_breaking_item", "&bSlab breaking");
        setMessage(sb, "settings_slab_breaking_lore", Arrays.asList(
                "&7&oWhen breaking double slabs,",
                "&7&oonly one half will be broken."
        ));
        addSpacer(sb, "");
        setMessage(sb, "settings_spawnteleport_item", "&bSpawn Teleport");
        setMessage(sb, "settings_spawnteleport_lore", Arrays.asList(
                "&7&oWhen enabled, you will be",
                "&7&oteleported to the spawn",
                "&7&oif it has been set."
        ));
        addSpacer(sb, "");
        addSpacer(sb, "# Change Design");
        setMessage(sb, "design_title", "&3» &8Change Design");
        setMessage(sb, "design_red", "Red");
        setMessage(sb, "design_orange", "Orange");
        setMessage(sb, "design_yellow", "Yellow");
        setMessage(sb, "design_pink", "Pink");
        setMessage(sb, "design_magenta", "Magenta");
        setMessage(sb, "design_purple", "Purple");
        setMessage(sb, "design_brown", "Brown");
        setMessage(sb, "design_lime", "Lime");
        setMessage(sb, "design_green", "Green");
        setMessage(sb, "design_blue", "Blue");
        setMessage(sb, "design_aqua", "Aqua");
        setMessage(sb, "design_light_blue", "Light Blue");
        setMessage(sb, "design_white", "White");
        setMessage(sb, "design_grey", "Grey");
        setMessage(sb, "design_dark_grey", "Dark Grey");
        setMessage(sb, "design_black", "Black");
        addSpacer(sb, "");
        addSpacer(sb, "# Speed");
        setMessage(sb, "speed_title", "&3» &8Speed");
        setMessage(sb, "speed_1", "&b1");
        setMessage(sb, "speed_2", "&b2");
        setMessage(sb, "speed_3", "&b3");
        setMessage(sb, "speed_4", "&b4");
        setMessage(sb, "speed_5", "&b5");
        addSpacer(sb, "");
        addSpacer(sb, "# Secret Blocks");
        setMessage(sb, "blocks_title", "&3» &8Secret Blocks");
        setMessage(sb, "blocks_full_oak_barch", "&bFull Oak Bark");
        setMessage(sb, "blocks_full_spruce_barch", "&bFull Spruce Bark");
        setMessage(sb, "blocks_full_birch_barch", "&bFull Birch Bark");
        setMessage(sb, "blocks_full_jungle_barch", "&bFull Jungle Bark");
        setMessage(sb, "blocks_full_acacia_barch", "&bFull Acacia Bark");
        setMessage(sb, "blocks_full_dark_oak_barch", "&bFull Dark Oak Bark");
        setMessage(sb, "blocks_red_mushroom", "&bRed Mushroom");
        setMessage(sb, "blocks_brown_mushroom", "&bBrown Mushroom");
        setMessage(sb, "blocks_full_mushroom_stem", "&bFull Mushroom Stem");
        setMessage(sb, "blocks_mushroom_stem", "&bMushroom Stem");
        setMessage(sb, "blocks_mushroom_block", "&bMushroom Block");
        setMessage(sb, "blocks_smooth_stone", "&bSmooth Stone");
        setMessage(sb, "blocks_double_stone_slab", "&bDouble Stone Slab");
        setMessage(sb, "blocks_smooth_sandstone", "&bSmooth Sandstone");
        setMessage(sb, "blocks_smooth_red_sandstone", "&bSmooth Red Sandstone");
        setMessage(sb, "blocks_powered_redstone_lamp", "&bPowered Redstone Lamp");
        setMessage(sb, "blocks_burning_furnace", "&bBurning Furnace");
        setMessage(sb, "blocks_piston_head", "&bPiston Head");
        setMessage(sb, "blocks_command_block", "&bCommand Block");
        setMessage(sb, "blocks_barrier", "&bBarrier");
        setMessage(sb, "blocks_invisible_item_frame", "&bInvisible Item Frame");
        setMessage(sb, "blocks_mob_spawner", "&bMob Spawner");
        setMessage(sb, "blocks_nether_portal", "&bNether Portal");
        setMessage(sb, "blocks_end_portal", "&bEnd Portal");
        setMessage(sb, "blocks_dragon_egg", "&bDragon Egg");
        setMessage(sb, "blocks_debug_stick", "&bDebug Stick");

        try (
                FileOutputStream fileStream = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)
        ) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadMessages();
    }

    private static void addSpacer(StringBuilder stringBuilder, String value) {
        stringBuilder.append(value).append("\n");
    }

    private static void setMessage(StringBuilder stringBuilder, String key, String defaultValue) {
        String value = config.getString(key, defaultValue);
        stringBuilder.append(key).append(": \"").append(value).append("\"").append("\n");
    }

    private static void setMessage(StringBuilder stringBuilder, String key, List<String> defaultValues) {
        List<String> values = config.getStringList(key);
        if (values.isEmpty()) {
            values = defaultValues;
        }

        stringBuilder.append(key).append(":\n");
        for (String value : values) {
            stringBuilder.append(" - \"").append(value).append("\"").append("\n");
        }
    }

    private static void loadMessages() {
        config.getConfigurationSection("")
                .getKeys(false)
                .forEach(message -> {
                    if (config.isList(message)) {
                        MESSAGES.put(message, String.join("\n", config.getStringList(message)));
                    } else {
                        MESSAGES.put(message, config.getString(message));
                    }
                });
    }

    private static void checkIfKeyPresent(String key) {
        if (!MESSAGES.containsKey(key)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[BuildSystem] Could not find message with key: " + key);
            createMessageFile();
        }
    }

    private static String getPrefix() {
        return MESSAGES.get("prefix");
    }

    @SafeVarargs
    public static void sendMessage(@Nullable CommandSender sender, String key, Map.Entry<String, Object>... placeholders) {
        if (sender == null) {
            return;
        }

        Player player = sender instanceof Player ? (Player) sender : null;
        String message = getString(key, player, placeholders);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    /**
     * Gets a message using the given key.
     *
     * @param key          The key of the message
     * @param player       The player to parse the placeholders against
     * @param placeholders The placeholders which are to be injected
     * @return The message uniquely identified by the given key
     */
    @SafeVarargs
    public static String getString(String key, @Nullable Player player, Map.Entry<String, Object>... placeholders) {
        checkIfKeyPresent(key);

        String message = MESSAGES.get(key).replace("%prefix%", getPrefix());
        String injectedPlaceholders = replacePlaceholders(message, placeholders);

        if (PLACEHOLDER_API_ENABLED && player != null) { // Player is null if message is sent to console
            injectedPlaceholders = PlaceholderAPI.setPlaceholders(player, injectedPlaceholders);
        }

        return ColorAPI.process(injectedPlaceholders);
    }

    /**
     * Gets a list of messages using the given key and injects the same placeholders into each line.
     *
     * @param key          The key of the message
     * @param player       The player to parse the placeholders against
     * @param placeholders The placeholders which are to be injected into all lines
     * @return A list of messages using the given key
     */
    @SafeVarargs
    public static List<String> getStringList(String key, @Nullable Player player, Map.Entry<String, Object>... placeholders) {
        return getStringList(key, player, (line) -> placeholders);
    }

    /**
     * Gets a list of messages using the given key and injects placeholders into each individual line.
     *
     * @param key          The key of the message
     * @param player       The player to parse the placeholders against
     * @param placeholders The function which gets the placeholders to be injected into a given line
     * @return A list of messages using the given key
     */
    public static List<String> getStringList(String key, @Nullable Player player, Function<String, Map.Entry<String, Object>[]> placeholders) {
        String message = MESSAGES.get(key).replace("%prefix%", getPrefix());
        return Arrays.stream(message.split("\n"))
                .map(line -> replacePlaceholders(line, placeholders.apply(line)))
                .map(line -> PLACEHOLDER_API_ENABLED && player != null ? PlaceholderAPI.setPlaceholders(player, line) : line)
                .map(ColorAPI::process)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    private static String replacePlaceholders(String query, Map.Entry<String, Object>... placeholders) {
        if (placeholders.length == 0) {
            return query;
        }

        return Arrays.stream(placeholders)
                .map(entry -> (Function<String, String>) data -> data.replaceAll(entry.getKey(), String.valueOf(entry.getValue())))
                .reduce(Function.identity(), Function::andThen)
                .apply(query);
    }

    public static String getDataString(@Nullable String key, Player player) {
        if (key == null) {
            return "-";
        }
        return getString(key, player);
    }

    public static String formatDate(long millis) {
        return millis > 0
                ? new SimpleDateFormat(JavaPlugin.getPlugin(BuildSystemPlugin.class).getConfigValues().getDateFormat()).format(millis)
                : "-";
    }
}