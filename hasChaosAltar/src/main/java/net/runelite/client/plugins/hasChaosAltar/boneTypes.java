package net.runelite.client.plugins.hasChaosAltar;

import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;

@Getter
public enum boneTypes {
    BONE("Bone", ItemID.BONES,527),
    BIG_BONE("Big Bone ", ItemID.BIG_BONES,533),
    BABYDRAGON_BONES("Babydragon bones", ItemID.BABYDRAGON_BONES,535),
    DRAGON_BONE("Dragon Bone", ItemID.DRAGON_BONES,537 ),
    WYVERN_BONE("WYVERN Bone", ItemID.WYVERN_BONES,6816 );



    private final String name;
    private final int id;
    private final int notedID;

    boneTypes(String name, int id, int notedID)
    {
        this.name = name;
        this.id = id;
        this.notedID = notedID;
    }


}
