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
package de.eintosti.buildsystem.version.customblocks;

import de.eintosti.buildsystem.version.util.MinecraftVersion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public enum CustomBlock {
    BARRIER("blocks_barrier", "3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025"),
    BROWN_MUSHROOM("blocks_brown_mushroom", "fa49eca0369d1e158e539d78149acb1572949b88ba921d9ee694fea4c726b3"),
    BURNING_FURNACE("blocks_burning_furnace", "d17b8b43f8c4b5cfeb919c9f8fe93f26ceb6d2b133c2ab1eb339bd6621fd309c"),
    COMMAND_BLOCK("blocks_command_block", "8514d225b262d847c7e557b474327dcef758c2c5882e41ee6d8c5e9cd3bc914"),
    DOUBLE_STONE_SLAB("blocks_double_stone_slab", "151e70169ea00f04a9439221cf33770844159dd775fc8830e311fd9b5ccd2969"),
    DRAGON_EGG("blocks_dragon_egg", "3c151fb54b21fe5769ffb4825b5bc92da73657f214380e5d0301e45b6c13f7d"),
    END_PORTAL("blocks_end_portal", "7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5"),
    FULL_ACACIA_BARCH("blocks_full_acacia_barch", "96a3bba2b7a2b4fa46945b1471777abe4599695545229e782259aed41d6"),
    FULL_BIRCH_BARCH("blocks_full_birch_barch", "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b"),
    FULL_DARK_OAK_BARCH("blocks_full_dark_oak_barch", "cde9d4e4c343afdb3ed68038450fc6a67cd208b2efc99fb622c718d24aac"),
    FULL_JUNGLE_BARCH("blocks_full_jungle_barch", "1cefc19380683015e47c666e5926b15ee57ab33192f6a7e429244cdffcc262"),
    FULL_MUSHROOM_STEM("blocks_full_mushroom_stem", "f55fa642d5ebcba2c5246fe6499b1c4f6803c10f14f5299c8e59819d5dc"),
    FULL_OAK_BARCH("blocks_full_oak_barch", "22e4bb979efefd2ddb3f8b1545e59cd360492e12671ec371efc1f88af21ab83"),
    FULL_SPRUCE_BARCH("blocks_full_spruce_barch", "966cbdef8efb914d43a213be66b5396f75e5c1b9124f76f67d7cd32525748"),
    INVISIBLE_ITEM_FRAME("blocks_invisible_item_frame", "8122a503d7a6f57802b03af7624194a4c4f5077a99ae21dd276ce7db88bc38ae", MinecraftVersion.CAVES_17),
    MOB_SPAWNER("blocks_mob_spawner", "db6bd9727abb55d5415265789d4f2984781a343c68dcaf57f554a5e9aa1cd"),
    MUSHROOM_BLOCK("blocks_mushroom_block", "3fa39ccf4788d9179a8795e6b72382d49297b39217146eda68ae78384355b13"),
    MUSHROOM_STEM("blocks_mushroom_stem", "84d541275c7f924bcb9eb2dbbf4b866b7649c330a6a013b53d584fd4ddf186ca"),
    NETHER_PORTAL("blocks_nether_portal", "b0bfc2577f6e26c6c6f7365c2c4076bccee653124989382ce93bca4fc9e39b"),
    PISTON_HEAD("blocks_piston_head", "aa868ce917c09af8e4c350a5807041f6509bf2b89aca45e591fbbd7d4b117d"),
    POWERED_REDSTONE_LAMP("blocks_powered_redstone_lamp", "7eb4b34519fe15847dbea7229179feeb6ea57712d165dcc8ff6b785bb58911b0"),
    RED_MUSHROOM("blocks_red_mushroom", "732dbd6612e9d3f42947b5ca8785bfb334258f3ceb83ad69a5cdeebea4cd65"),
    SMOOTH_RED_SANDSTONE("blocks_smooth_red_sandstone", "a2da7aa1ae6cc9d6c36c18a460d2398162edc2207fdfc9e28a7bf84d7441b8a2"),
    SMOOTH_SANDSTONE("blocks_smooth_sandstone", "38fffbb0b8fdec6f62b17c451ab214fb86e4e355b116be961a9ae93eb49a43"),
    SMOOTH_STONE("blocks_smooth_stone", "8dd0cd158c2bb6618650e3954b2d29237f5b4c0ddc7d258e17380ab6979f071"),
    DEBUG_STICK("blocks_debug_stick", "badc048a7ce78f7dad72a07da27d85c0916881e5522eeed1e3daf217a38c1a", MinecraftVersion.AQUATIC_13);

    private final String key;
    private final String skullUrl;
    private final MinecraftVersion version;

    CustomBlock(String key, String skullUrl) {
        this.key = key;
        this.skullUrl = skullUrl;
        this.version = MinecraftVersion.BOUNTIFUL_8;
    }

    CustomBlock(String key, String skullUrl, MinecraftVersion version) {
        this.key = key;
        this.skullUrl = skullUrl;
        this.version = version;
    }

    @Nullable
    @ApiStatus.Internal
    public static CustomBlock getCustomBlock(String key) {
        String customBlock = key.substring("blocks_".length()).toUpperCase();
        try {
            return CustomBlock.valueOf(customBlock);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[BuildSystem] Cannot find CustomBlock: " + customBlock);
            return null;
        }
    }

    public String getKey() {
        return key;
    }

    public String getSkullUrl() {
        return skullUrl;
    }

    public MinecraftVersion getVersion() {
        return version;
    }
}