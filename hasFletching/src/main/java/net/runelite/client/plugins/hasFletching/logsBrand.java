package net.runelite.client.plugins.hasFletching;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum logsBrand {



        Regular("Log", ItemID.LOGS),
        Oak("Oak Log", ItemID.OAK_LOGS),
        Willow("Willow Log", ItemID.WILLOW_LOGS),
        Maple("Maple Log", ItemID.MAPLE_LOGS),
        Yew("Yew Log", ItemID.YEW_LOGS),
        Magic("Magic Log", ItemID.MAGIC_LOGS);


    private final String name;
    private final int logID;


    logsBrand(String name, int logID)
    {
        this.name = name;
        this.logID = logID;

    }


}
