package net.runelite.client.plugins.hRoguesDen;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import static net.runelite.api.ObjectID.*;

public enum RoguesDenObstacles {

    CONTORTION(new WorldPoint(3056,4992,1), 7251);


   // @Getter(AccessLevel.PACKAGE)
//    private final WorldArea location;

    @Getter(AccessLevel.PACKAGE)
    private final int obstacleId;


    RoguesDenObstacles(final WorldPoint min, final int obstacleId)
    {
       // this.location = new WorldArea(min, max);
        this.obstacleId = obstacleId;
    }
}
