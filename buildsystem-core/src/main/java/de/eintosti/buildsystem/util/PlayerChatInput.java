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
package de.eintosti.buildsystem.util;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerChatInput implements Listener {

    private final BuildSystemPlugin plugin;

    private final BukkitTask taskId;
    private final InputRunnable runWhenComplete;
    private final UUID playerUuid;
    private final boolean inputMode;

    private final Map<UUID, PlayerChatInput> inputs = new HashMap<>();

    public PlayerChatInput(BuildSystemPlugin plugin, Player player, String titleKey, InputRunnable runWhenComplete) {
        this.plugin = plugin;

        String title = Messages.getString(titleKey, player);
        String subtitle = Messages.getString("cancel_subtitle", player);

        this.taskId = new BukkitRunnable() {
            public void run() {
                Titles.sendTitle(player, 0, 30, 0, title, subtitle);
            }
        }.runTaskTimer(plugin, 0L, 20L);

        this.playerUuid = player.getUniqueId();
        this.runWhenComplete = runWhenComplete;
        this.inputMode = true;

        player.closeInventory();
        XSound.ENTITY_PLAYER_LEVELUP.play(player);

        this.register();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String input = event.getMessage();
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        if (!inputs.containsKey(playerUuid)) {
            return;
        }

        PlayerChatInput current = inputs.get(playerUuid);
        if (!current.inputMode) {
            return;
        }

        event.setCancelled(true);

        if (input.equalsIgnoreCase("cancel")) {
            current.taskId.cancel();
            current.unregister();

            XSound.ENTITY_ITEM_BREAK.play(player);
            Titles.clearTitle(player);
            Messages.sendMessage(player, "input_cancelled");
            return;
        }

        current.taskId.cancel();
        Bukkit.getScheduler().runTask(current.plugin, () -> current.runWhenComplete.run(input));
        Titles.clearTitle(player);
        current.unregister();
    }

    private void register() {
        inputs.put(this.playerUuid, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void unregister() {
        inputs.remove(this.playerUuid);
        HandlerList.unregisterAll(this);
    }

    @FunctionalInterface
    public interface InputRunnable {

        void run(String input);
    }
}