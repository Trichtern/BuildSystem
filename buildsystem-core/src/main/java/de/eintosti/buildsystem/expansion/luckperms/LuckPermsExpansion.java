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
package de.eintosti.buildsystem.expansion.luckperms;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.expansion.luckperms.calculators.BuildModeCalculator;
import de.eintosti.buildsystem.expansion.luckperms.calculators.RoleCalculator;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LuckPermsExpansion {

    private final BuildSystemPlugin plugin;
    private final ContextManager contextManager;
    private final List<ContextCalculator<Player>> registeredCalculators;

    public LuckPermsExpansion(BuildSystemPlugin plugin) {
        LuckPerms luckPerms = plugin.getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            throw new IllegalStateException("LuckPerms API not loaded.");
        }

        this.plugin = plugin;
        this.contextManager = luckPerms.getContextManager();
        this.registeredCalculators = new ArrayList<>();
    }

    public void registerAll() {
        register("build-mode", () -> new BuildModeCalculator(plugin));
        register("role", () -> new RoleCalculator(plugin));
    }

    private void register(String option, Supplier<ContextCalculator<Player>> calculatorSupplier) {
        plugin.getLogger().info("Registering '" + option + "' calculator");
        ContextCalculator<Player> calculator = calculatorSupplier.get();
        this.contextManager.registerCalculator(calculator);
        this.registeredCalculators.add(calculator);
    }

    public void unregisterAll() {
        this.registeredCalculators.forEach(this.contextManager::unregisterCalculator);
        this.registeredCalculators.clear();
    }
}