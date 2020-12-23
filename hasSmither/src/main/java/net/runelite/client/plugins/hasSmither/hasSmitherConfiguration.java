package net.runelite.client.plugins.hasSmither;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("has-smither")
public interface hasSmitherConfiguration extends Config {


    @ConfigItem(
            keyName = "getBarType",
            name = "Smithing Bar",
            description = "Bar that will be used to craft items",
            position = 5
    )
    default barTypes getBarType()
    {
        return barTypes.BRONZE_BAR;
    }


    @ConfigItem(
            keyName = "craftItem",
            name = "Item",
            description = "Select item to smith",
            position = 10
    )
    default itemTypes craftItem()	{ return itemTypes.Dagger; }

    @ConfigItem(
            keyName = "bankLoc",
            name = "Smithing Location",
            description = "Select location to smith at",
            position = 15
    )
    default bankType bankLoc()	{ return bankType.VARROCK_WEST; }

    @ConfigItem(
            keyName = "startButton",
            name = "Start",
            description = "",
            position = 40
    )
    default Button startButton() { return new Button(); }

    @ConfigItem(
            keyName = "stopButton",
            name = "Stop",
            description = "",
            position = 50
    )
    default Button stopButton() { return new Button(); }
}
