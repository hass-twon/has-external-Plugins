/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.hasCrabs;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.botutils.BotUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

import net.runelite.client.ui.overlay.OverlayManager;

@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
        name = "has-crabs",
        enabledByDefault = false,
        description = "Does  Crabs for You",
        tags = {"sand", "crabs", "sand crabs","has-crabs","has"}

)
@Slf4j
public class hascrabsPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private hascrabsOverlay overlay;

    @Inject
    private hascrabsConfiguration config;

    @Inject
    private BotUtils utils;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    NPC currentNPC;
    Instant botTimer;
    Instant totalTimer;
    Player player;
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    WorldPoint customLocation;
    WorldPoint resetLocation;
    long sleepLength;
    int tickLength;
    int timeout;
    boolean startBot;
    boolean walkToCrab;
    String states;
    long timeRan;
    int timeRun;
    boolean waiting;
    boolean goReset;
    String status;
    int timeRuns;
    int randVar;
    int resetTime;

    @Provides
    hascrabsConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(hascrabsConfiguration.class);

    }

    @Override
    protected void startUp() {

        utils.sendGameMessage("plogin Started");
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        botTimer = null;
        walkToCrab = false;
        startBot = false;
        waiting = false;
        goReset = false;
        overlayManager.remove(overlay);

    }

    @Subscribe
    public void onConfigButtonClicked(ConfigButtonClicked event) {

        if (!event.getGroup().equalsIgnoreCase("hascrabs")) {
            return;
        }

        if (event.getKey().equals("startButton")) {
            utils.sendGameMessage("starting");
            setLocations();
            botTimer = Instant.now();
            totalTimer = Instant.now();
            randVar = utils.getRandomIntBetweenRange(-5, 6);
            walkToCrab = true;
            startBot = true;
            waiting = false;
            goReset = false;
            resetTime= config.customTime();
        } else if (event.getKey().equals("stopButton")) {
            utils.sendGameMessage("stopping");
            startBot = false;
            timeRuns = config.customTime();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!startBot) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            log.debug("goreset value: " + String.valueOf(goReset));
            log.debug("waiting value: " + String.valueOf(waiting));
            log.debug("walktocreab value: " + String.valueOf(walkToCrab));


            if (timeout > 0) {
                timeout--;
            } else {
                states = getState();
                switch (states) {
                    case "GOTOCRAB":

                        //utils.sendGameMessage("Going to crabs");
                        if (player.getWorldLocation().distanceTo(customLocation) > 0) {
                            utils.webWalk(customLocation, 0, utils.isMoving(beforeLoc), sleepDelay());
                            //	utils.walk(customLocation,0,sleepDelay());
                            status = "Walking to crab";
                            timeout = tickDelay();
                            break;
                        } else {
                            //utils.sendGameMessage("at position");
                            walkToCrab = false;
                            waiting = true;
                            botTimer = Instant.now();
                            //  Duration duration = Duration.between(botTimer, Instant.now());
                            //  timeRan = duration.toSeconds();
                            //   timeRun =600- (int) timeRan;
                            //   log.info(String.valueOf(duration));
                            //  utils.sendGameMessage(Integer.toString(timeRun));
                        }
                    case "CHECKTIME":
                        Duration duration = Duration.between(botTimer, Instant.now());
                        //timeRan = duration.toSeconds();
                        timeRan = duration.getSeconds();
                        timeRun = (int) timeRan;
                        status = "Waiting until reset";
                        timeRuns = (resetTime) - timeRun;
                        // utils.sendGameMessage(Integer.toString(timeRun));
                        if (timeRun > resetTime) {
                            if(inCombat()){
                                timeRun =- 30;
                                break;
                            }else{
                                waiting = false;
                                goReset = true;
                                break;
                            }



                        }
                        break;
                    case "RESETTING":
                        if (player.getWorldLocation().distanceTo(resetLocation) > 4) {
                            //   utils.webWalk(resetLocation, 3, utils.isMoving(beforeLoc), sleepDelay());
                            utils.walk(resetLocation, 3, sleepDelay());
                            timeout = tickDelay();
                            status = "Resetting";
                            break;
                        } else {
                            walkToCrab = true;
                            goReset = false;
                            //  utils.sendGameMessage("at reset spot");
                        }


                }
            }
            beforeLoc = player.getLocalLocation();
        }

    }

    private String getState() {
        if (walkToCrab) {
            return "GOTOCRAB";
        }
        if (waiting) {
            return "CHECKTIME";
        }
        if (goReset) {
            return "RESETTING";
        } else {
            return null;
        }
    }

    private void setLocations() {
        if (config.crabType().name().equalsIgnoreCase("SAND")) {
            customLocation = new WorldPoint(1843, 3462, 0);
            resetLocation = new WorldPoint(1846, 3504, 0);
        }else if (config.crabType().name().equalsIgnoreCase("AMMONITE")){
            customLocation = new WorldPoint(3656,3875,0);
            resetLocation = new WorldPoint(3668,3843,0);
        }else if (config.crabType().name().equalsIgnoreCase("CUSTOM")){
            customLocation = getCustomLoc("crab");
            resetLocation = getCustomLoc("reset");
        }

    }

    private boolean inCombat(){
        currentNPC = (NPC) player.getInteracting();
        if(currentNPC == null){
            return false;
        }else{
            return true;
        }
    }

    private WorldPoint getCustomLoc(String what) {
        if (what.equalsIgnoreCase("crab")) {
            int[] customTemp = utils.stringToIntArray(config.customCrabLocation());
            if (customTemp.length != 3) {
                return null;
            } else {
                return new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
            }
        } else if (what.equalsIgnoreCase("reset")) {
            int[] customTemp = utils.stringToIntArray(config.customResetLocation());
            if (customTemp.length != 3) {
                return null;
            } else {
                return new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
            }
        }
        return null;
    }

    private long sleepDelay() {
        sleepLength = utils.randomDelay(false, 60, 300, 20, 100);
        return sleepLength;
    }

    private int tickDelay() {
        tickLength = (int) utils.randomDelay(false, 1, 3, 1, 2);
        return tickLength;
    }


}