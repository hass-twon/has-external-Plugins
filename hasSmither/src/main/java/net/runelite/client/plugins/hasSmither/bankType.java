package net.runelite.client.plugins.hasSmither;


import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum bankType {

    VARROCK_WEST("VARROCK WEST", 34810,2097),
    PRIFFDINAS("PRIFFDINAS", 36559,2097);

    private final String name;
    private final int bankID;
    private final int anvilID;

    bankType(String name, int bankID, int anvilID)
    {
        this.name = name;
        this.bankID = bankID;
        this.anvilID = anvilID;
    }

}
