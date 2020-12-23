package net.runelite.client.plugins.hasSmither;

import net.runelite.api.ItemID;
import lombok.Getter;

@Getter
public enum barTypes {
    BRONZE_BAR("BRONZE BAR", ItemID.BRONZE_BAR),
    IRON_BAR("IRON BAR", ItemID.IRON_BAR),
    SILVER_BAR("SILVER BAR", ItemID.SILVER_BAR),
    STEEL_BAR("Steel Bar", ItemID.STEEL_BAR),
    GOLD_BAR("Gold Bar", ItemID.GOLD_BAR),
    MITHRIL_BAR("Mithril Bar", ItemID.MITHRIL_BAR),
    ADAMANTITE_BAR("Adamant Bar", ItemID.ADAMANTITE_BAR),
    RUNITE_BAR("Rune Bar", ItemID.RUNITE_BAR);

    private final String name;
    private final int id;

    barTypes(String name, int id)
    {
        this.name = name;
        this.id = id;
    }
}
