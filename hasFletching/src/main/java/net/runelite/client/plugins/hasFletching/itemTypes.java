package net.runelite.client.plugins.hasFletching;

import lombok.Getter;

@Getter
public enum itemTypes {
    Shortbow_R("Shortbow(Select for Regular)", 17694736),
    Shortbow("Shortbow(Select for Others)", 17694735),
    Longbow_R("Longbow(Select for Regular)", 17694737),
    Longbow("Longbow(Select for Others)", 17694736),
    Crossbow_Stock("Crossbow Stock", 17694738),
    Arrow_Shafts("Arrow Shafts", 17694734),
    Javelin_Shafts("Javelin Shafts", 17694735),
    Shield("Shield", 17694738);
    private final String name;
    private final int idValue;


    itemTypes(String name, int idValue)
    {
        this.name = name;
        this.idValue = idValue;

    }

}