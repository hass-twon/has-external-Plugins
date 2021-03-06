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
package net.runelite.client.plugins.hasSmither;

import com.google.inject.Provides;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.*;
import org.pf4j.Extension;
import net.runelite.api.ItemID;

///iUtils
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.BankUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.MouseUtils;
import net.runelite.client.plugins.iutils.ObjectUtils;
import net.runelite.client.plugins.iutils.PlayerUtils;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "has-smither",
        enabledByDefault = false,
        description = "Smiths item of choice at varrock",
        tags = {"has", "smith", "smither"}

)
@Slf4j
public class hasSmitherPlugin extends Plugin {


    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private hasSmitherConfiguration config;

    @Inject
    private iUtils utils;

    @Inject
    private ActionQueue action;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private ObjectUtils object;

    @Inject
    private BankUtils bank;

    @Inject
    private NPCUtils npc;

    @Inject
    private KeyboardUtils key;

    @Inject
    private WalkUtils walk;


    @Inject
    PluginManager pluginManager;

    boolean startSmithing;
    MenuEntry targetMenu;
    Player player;
    int timeOut = 0;
    boolean moving;
    boolean animating;
    boolean bankOpen;
    GameObject targetObject;
    String status;
    int barID;
    GameObject bankStand;
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    long sleepLength;
    int tickDelay = 0;
    boolean firstTime;
    boolean withDrawNow;
    int smithingWidg;
    int bankBoothID;
    int anvilID;
    //String item  = "SMITHING_ANVIL_DAGGER";
    List<Integer> REQUIRED_ITEMS = new ArrayList<>();

    @Provides
    hasSmitherConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(hasSmitherConfiguration.class);
    }

    @Override
    protected void startUp() {

        withDrawNow = false;
        firstTime = true;
        startSmithing = false;
        //barID = 2349;

        REQUIRED_ITEMS.add(ItemID.HAMMER);

        utils.sendGameMessage("plogin Started");

    }

    @Override
    protected void shutDown() {
    }

    @Subscribe
    public void onConfigButtonClicked(ConfigButtonClicked event) {

        if (!event.getGroup().equalsIgnoreCase("has-smither")) {
            return;
        }

        if (event.getKey().equals("startButton")) {
            utils.sendGameMessage("starting");

            barID = config.getBarType().getId();
            bankBoothID = config.bankLoc().getBankID();
            anvilID = config.bankLoc().getAnvilID();
            itemToSmith();
            startSmithing = true;
        } else if (event.getKey().equals("stopButton")) {
            utils.sendGameMessage("stopping");
            startSmithing = false;
        }
    }

    private String getStatus() {
        Widget smithingInterface = client.getWidget(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER);
        Widget levelUp = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
        if (timeOut > 0) {
            return "TIMEOUT";
        }
        if (playerUtils.isMoving(beforeLoc)) {
            timeOut = tickDelay();
            return "MOVING";
        }
        if (client.getLocalPlayer().getAnimation() == 898) {
            return "IN_ANIMATION";
        }
        if (levelUp != null && !levelUp.isHidden()) {
            return "ANVILING";
        }
        if (bank.isOpen()) {
            return bankState();
        }


        if (smithingInterface != null && !smithingInterface.isHidden()) {
            return "CRAFT_ITEM";
        }
        if (inventory.containsItemAmount(barID, 3, false, false)) {
            return "ANVILING";
        }
        if (inventory.containsItemAmount(barID, 2, false, false) || inventory.containsItemAmount(barID, 1, false, false) || inventory.containsItemAmount(barID, 0, false, false)) {
            return "BANKING";
        }

        if (!inventory.isFull()) {
            return "BANKING";
        }
        if (inventory.containsItemAmount(barID, 27, false, false)) {
            return "ANVILING";
        }

        if (playerUtils.isAnimating() || playerUtils.isMoving(beforeLoc)) {

            //utils.sendGameMessage("you are moving");
            return "WAITING";
        } else {
            return "UNKNOWN";
        }
    }

    private String bankState() {

        if (inventory.containsItem(ItemID.HAMMER) && withDrawNow) {
            withDrawNow = false;
            return "WITHDRAW_BAR";
        }
        if (inventory.isEmpty()) {
            return "WITHDRAW_HAMMER";
        }



        if (inventory.containsItemAmount(barID, 2, false, true) || inventory.containsItemAmount(barID, 1, false, true) || inventory.containsItemAmount(barID, 0, false, true)) {
            firstTime = false;
            withDrawNow = true;
            return "DEPOSIT";
        }


        if (!inventory.isFull() && !inventory.isEmpty()) {
            withDrawNow = true;
            return "DEPOSIT";
        }


        if (inventory.isFull()) {
            firstTime = true;
            return "CLOSE_BANK";
        } else {
            return "UNKNOWN";
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        player = client.getLocalPlayer();

        if (!startSmithing) {
            return;
        }


        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            status = getStatus();
            //utils.sendGameMessage(status);
            switch (status) {
                case "TIMEOUT":
                    playerUtils.handleRun(30, 20);

                    timeOut--;
                    break;
                case "MOVING":
                    playerUtils.handleRun(30, 20);
                    timeOut = tickDelay();
                    break;
                case "ANVILING":
                    //	utils.sendGameMessage("Walking to Anvil");
                    firstTime = true;
                    goToAnvil();
                    playerUtils.handleRun(30, 20);
                    timeOut = tickDelay();
                    break;
                case "IN_ANIMATION":
                    playerUtils.handleRun(30, 20);
                    timeOut = 5;
                    break;
                case "CRAFT_ITEM":
                    //utils.sendGameMessage("ITS OPEN BRO");
                    itemToSmith();
                    chooseItem();
                    timeOut = 5;
                    break;
                case "BANKING":
                    //utils.sendGameMessage("GOING TO BANK");
                    goToBank();
                    playerUtils.handleRun(30, 20);
                    timeOut = tickDelay();
                    break;
                case "UNKNOWN":
                    //utils.sendGameMessage("UNKNOWN");
                    break;
                case "DEPOSIT":
                    bank.depositAllExcept(REQUIRED_ITEMS);

                    timeOut = tickDelay();
                    break;
                case "WITHDRAW_HAMMER":
                    bank.withdrawItemAmount(ItemID.HAMMER, 1);
                    timeOut = tickDelay();
                    break;
                case "WITHDRAW_BAR":
                    bank.withdrawAllItem(barID);
                    timeOut = tickDelay();
                    break;
                case "CLOSE_BANK":
                    bank.close();
                    //	utils.sendGameMessage("GOING TO WALK TO FURNACE");
                    timeOut = tickDelay();
                    break;


                //utils.sendGameMessage("WITHDRAWING ITEMS");


            }
            beforeLoc = player.getLocalLocation();

        }

    }

    private void itemToSmith() {
        switch (config.craftItem()) {
            case Dagger:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_DAGGER.getId();
                break;
            case Sword:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_SWORD.getId();
                break;
            //smithingWidg = 20447242;
            case Scimitar:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_SCIMITAR.getId();
                break;
            case Long_sword:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_LONG_SWORD.getId();
                break;
            case Axe:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_AXE.getId();
                break;
            case Mace:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_MACE.getId();
                break;
            case Warhammer:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_WARHAMMER.getId();
                break;
            case Battle_axe:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_BATTLE_AXE.getId();
                break;
            case Double_Handed_Sword:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_TWO_H_SWORD.getId();
                break;
            case Chain_Body:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_CHAIN_BODY.getId();
                break;
            case Plate_legs:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_PLATE_LEGS.getId();
                break;
            case Plate_skirt:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_PLATE_SKIRT.getId();
                break;
            case Plate_body:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_PLATE_BODY.getId();
                break;
            case Nails:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_NAILS.getId();
                break;
            case Medium_Helm:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_MED_HELM.getId();
                break;
            case Full_helm:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_FULL_HELM.getId();
                break;
            case Square_shield:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_SQ_SHIELD.getId();
                break;
            case Kite_shield:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_KITE_SHIELD.getId();
                break;
            case Dart_tips:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_DART_TIPS.getId();
                break;
            case Arrowtips:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_ARROW_HEADS.getId();
                break;
            case Knives:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_KNIVES.getId();
                break;
            case Darts:
                smithingWidg = WidgetInfo.SMITHING_ANVIL_DART_TIPS.getId();
                break;
        }
    }

    private void chooseItem() {
//utils.sendGameMessage(Integer.toString(smithingWidg));
        targetMenu = new MenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, smithingWidg, false);
        if (targetMenu != null) {
            //utils.sendGameMessage("not null");
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
        } else {
            utils.sendGameMessage("its null");
        }
        mouse.delayClickRandomPointCenter(-75, 75, sleepDelay());

    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(false, 60, 350, 10, 100);
        return sleepLength;
    }


    private Point getRandomNullPoint() {
        if (client.getWidget(161, 34) != null) {
            Rectangle nullArea = client.getWidget(161, 34).getBounds();
            return new Point((int) nullArea.getX() + calc.getRandomIntBetweenRange(0, nullArea.width), (int) nullArea.getY() + calc.getRandomIntBetweenRange(0, nullArea.height));
        }

        return new Point(client.getCanvasWidth() - calc.getRandomIntBetweenRange(0, 2), client.getCanvasHeight() - calc.getRandomIntBetweenRange(0, 2));
    }

    private int tickDelay() {
        int tickLength = (int) calc.randomDelay(false, 1, 3, 1, 2);
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    private void goToBank() {
        targetObject = object.findNearestGameObjectWithin(player.getWorldLocation(), 25, bankBoothID);
        if (targetObject != null) {
            targetMenu = new MenuEntry("", "", targetObject.getId(), 4,
                    targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
        } else {
            utils.sendGameMessage("Cannot find bank");
        }
    }

    private void goToAnvil() {

        targetObject = object.findNearestGameObjectWithin(player.getWorldLocation(), 25, anvilID);
        if (targetObject != null) {
            targetMenu = new MenuEntry("", "", targetObject.getId(), 3,
                    targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
        }
    }
}