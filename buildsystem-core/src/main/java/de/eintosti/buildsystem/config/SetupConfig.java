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

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.api.world.data.WorldType;

public class SetupConfig extends ConfigurationFile {

    public SetupConfig(BuildSystemPlugin plugin) {
        super(plugin, "setup.yml");
    }

    public void saveCreateItem(WorldType worldType, XMaterial material) {
        getFile().set("setup.type." + worldType.name().toLowerCase() + ".create", material.name());
        saveFile();
    }

    public void saveDefaultItem(WorldType worldType, XMaterial material) {
        getFile().set("setup.type." + worldType.name().toLowerCase() + ".default", material.name());
        saveFile();
    }

    public void saveStatusItem(WorldStatus worldStatus, XMaterial material) {
        getFile().set("setup.status." + worldStatus.name().toLowerCase(), material.name());
        saveFile();
    }
}