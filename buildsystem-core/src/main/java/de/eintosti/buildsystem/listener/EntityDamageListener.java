package de.eintosti.buildsystem.listener;

import com.cryptomorin.xseries.XSound;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.config.ConfigValues;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final BuildSystemPlugin plugin;

    public EntityDamageListener(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }

        // Teleport player up if void damage is taken
        if (cause != EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        ConfigValues configValues = plugin.getConfigValues();
        if (!configValues.isSaveFromDeath()) {
            return;
        }

        Player player = (Player) event.getEntity();
        Location teleportLoc = player.getLocation().clone().add(0, 200, 0);

        if (configValues.isTeleportToMapSpawn()) {
            BuildWorld buildWorld = plugin.getWorldManager().getBuildWorld(player.getWorld());
            if (buildWorld != null) {
                Location spawn = buildWorld.getData().getCustomSpawnLocation();
                if (spawn != null) {
                    teleportLoc = spawn;
                }
            }
        }

        event.setCancelled(true);
        player.setFallDistance(0);
        PaperLib.teleportAsync(player, teleportLoc)
                .whenComplete((completed, throwable) -> {
                    if (!completed) {
                        return;
                    }
                    XSound.ENTITY_ZOMBIE_INFECT.play(player);
                });
    }
}
