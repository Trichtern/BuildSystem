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
package de.eintosti.buildsystem.version.v1_13_R1;

import de.eintosti.buildsystem.version.gamerules.AbstractGameRulesInventory;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameRules_1_13_R1 extends AbstractGameRulesInventory {

    private final List<String> booleanEnabledLore, booleanDisabledLore;
    private final List<String> integerLore;

    public GameRules_1_13_R1(String inventoryTitle, List<String> booleanEnabledLore, List<String> booleanDisabledLore, List<String> integerLore) {
        super(inventoryTitle);

        this.booleanEnabledLore = booleanEnabledLore;
        this.booleanDisabledLore = booleanDisabledLore;
        this.integerLore = integerLore;
    }

    @Override
    protected void addGameRuleItem(Inventory inventory, int slot, World world, String gameRule) {
        ItemStack itemStack = new ItemStack(isEnabled(world, gameRule) ? Material.FILLED_MAP : Material.MAP);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(ChatColor.YELLOW + gameRule);
        itemMeta.setLore(getLore(world, gameRule));
        itemMeta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(itemMeta);

        inventory.setItem(slot, itemStack);
    }

    @Override
    protected boolean isEnabled(World world, String gameRuleName) {
        GameRule<?> gameRule = GameRule.getByName(gameRuleName);
        if (gameRule == null) {
            return false;
        }

        if (gameRule.getType().equals(Boolean.class)) {
            return (Boolean) world.getGameRuleValue(gameRule);
        }

        return true;
    }

    private List<String> getLore(World world, String gameRuleName) {
        GameRule<?> gameRule = GameRule.getByName(gameRuleName);
        if (gameRule == null) {
            return new ArrayList<>();
        }

        List<String> lore;
        if (isBoolean(gameRuleName)) {
            boolean enabled = (Boolean) world.getGameRuleValue(gameRule);
            lore = enabled ? this.booleanEnabledLore : this.booleanDisabledLore;
        } else {
            lore = this.integerLore.stream()
                    .map(line -> line.replace("%value%", world.getGameRuleValue(gameRule).toString()))
                    .collect(Collectors.toList());
        }
        return lore;
    }

    private boolean isBoolean(String gameRuleName) {
        GameRule<?> gameRule = GameRule.getByName(gameRuleName);
        if (gameRule == null) {
            return false;
        }
        return gameRule.getType().equals(Boolean.class);
    }

    @Override
    public void toggleGameRule(InventoryClickEvent event, World world) {
        int slot = event.getSlot();
        if (!isValidSlot(slot)) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            return;
        }

        String displayName = itemMeta.getDisplayName();
        String gameRuleName = ChatColor.stripColor(displayName);

        if (isBoolean(gameRuleName)) {
            GameRule<Boolean> gameRule = (GameRule<Boolean>) GameRule.getByName(gameRuleName);
            Boolean value = world.getGameRuleValue(gameRule);
            world.setGameRule(gameRule, !value);
        } else {
            GameRule<Integer> gameRule = (GameRule<Integer>) GameRule.getByName(gameRuleName);
            Integer value = world.getGameRuleValue(gameRule);

            if (event.isShiftClick()) {
                if (event.isRightClick()) {
                    value += 10;
                } else if (event.isLeftClick()) {
                    value -= 10;
                }
            } else {
                if (event.isRightClick()) {
                    value += 1;
                } else if (event.isLeftClick()) {
                    value -= 1;
                }
            }

            world.setGameRule(gameRule, value);
        }
    }
}