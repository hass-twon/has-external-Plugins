package net.runelite.client.plugins.hasCrafting;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum gemType {
    Sapphire("Sapphire", ItemID.UNCUT_SAPPHIRE,ItemID.SAPPHIRE),
    Emerald("Emerald", ItemID.UNCUT_EMERALD,ItemID.EMERALD),
    Ruby("Ruby", ItemID.UNCUT_RUBY,ItemID.RUBY),
    Diamond("Diamond", ItemID.UNCUT_DIAMOND,ItemID.DIAMOND),
    Dragon_Stone("Dragon Stone", ItemID.UNCUT_DRAGONSTONE,ItemID.DRAGONSTONE);

    private final String name;
    private final int uncutID;
    private final int cutID;

    gemType(String name, int uncutID, int cutID)
    {
        this.name = name;
        this.uncutID = uncutID;
        this.cutID = cutID;

    }

}