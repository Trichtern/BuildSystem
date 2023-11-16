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
package de.eintosti.buildsystem.listener;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.Builder;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WorldManipulateListener implements Listener {

    private final BuildSystemPlugin plugin;
    private final BuildWorldManager worldManager;

    public WorldManipulateListener(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        WorldData worldData = buildWorld.getData();
        if (!manageWorldInteraction(player, event, worldData.blockBreaking().get())) {
            worldData.lastEdited().set(System.currentTimeMillis());
            setStatus(worldData, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        WorldData worldData = buildWorld.getData();
        if (!manageWorldInteraction(player, event, worldData.blockPlacement().get())) {
            worldData.lastEdited().set(System.currentTimeMillis());
            setStatus(worldData, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();

        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        if (event.getEntity() instanceof ArmorStand) {
            manageWorldInteraction(player, event, buildWorld.getData().blockInteractions().get());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        manageWorldInteraction(player, event, buildWorld.getData().blockInteractions().get());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack != null && itemStack.getType() == plugin.getConfigValues().getWorldEditWand().parseMaterial()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        WorldData worldData = buildWorld.getData();
        manageWorldInteraction(player, event, worldData.blockInteractions().get());

        if (!worldData.physics().get() && event.getClickedBlock() != null) {
            if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == XMaterial.FARMLAND.parseMaterial()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Not every player can always interact with the {@link BuildWorld} they are in.
     * <p>
     * Reasons an interaction could be cancelled:
     * <ul>
     *     <li>The world has its {@link WorldStatus} set to archived;</li>
     *     <li>The world has a setting enabled which disallows certain events;</li>
     *     <li>The world only allows {@link Builder}s to build and the player is not such a builder.</li>
     * </ul>
     * <p>
     * However, a player can override these reasons if:
     * <ul>
     *     <li>The player has the permission {@code buildsystem.admin};</li>
     *     <li>The player has the permission {@code buildsystem.bypass.archive};</li>
     *     <li>The player has used {@code /build} to enter build-mode.</li>
     * </ul>
     *
     * @param player the player who manipulated the world
     * @param event  the event which was called by the world manipulation
     * @return if the event called when the player performs an action was cancelled
     */
    private boolean manageWorldInteraction(Player player, Event event, boolean worldSetting) {
        String worldName = player.getWorld().getName();
        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            return false;
        }

        if (disableArchivedWorlds(buildWorld, player, event)) {
            return true;
        }
        if (checkWorldSettings(player, event, worldSetting)) {
            return true;
        }
        return checkBuilders(buildWorld, player, event);
    }

    private boolean disableArchivedWorlds(BuildWorld buildWorld, Player player, Event event) {
        if (worldManager.canBypassBuildRestriction(player)) {
            return false;
        }

        if (buildWorld.getData().status().get() == WorldStatus.ARCHIVE) {
            ((Cancellable) event).setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private boolean checkWorldSettings(Player player, Event event, boolean worldSetting) {
        if (worldManager.canBypassBuildRestriction(player)) {
            return false;
        }

        if (!worldSetting) {
            ((Cancellable) event).setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private boolean checkBuilders(BuildWorld buildWorld, Player player, Event event) {
        if (worldManager.canBypassBuildRestriction(player)) {
            return false;
        }

        if (buildWorld.isCreator(player)) {
            return false;
        }

        if (buildWorld.getData().buildersEnabled().get() && !buildWorld.isBuilder(player)) {
            ((Cancellable) event).setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private void denyPlayerInteraction(Event event) {
        if (event instanceof PlayerInteractEvent) {
            ((PlayerInteractEvent) event).setUseItemInHand(Event.Result.DENY);
            ((PlayerInteractEvent) event).setUseInteractedBlock(Event.Result.DENY);
        }
    }

    private void setStatus(WorldData worldData, Player player) {
        if (worldData.status().get() == WorldStatus.NOT_STARTED) {
            worldData.status().set(WorldStatus.IN_PROGRESS);
            plugin.getPlayerManager().forceUpdateSidebar(player);
        }
    }
}