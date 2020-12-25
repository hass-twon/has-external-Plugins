package net.runelite.client.plugins.hasAutoTele;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import java.awt.Rectangle;
///api
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import static net.runelite.api.MenuOpcode.ITEM_USE_ON_GAME_OBJECT;


///client
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.*;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;


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


import static net.runelite.api.MenuOpcode.ITEM_USE_ON_NPC;
import static net.runelite.client.plugins.hasAutoTele.hasAutoTeleState.*;




import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-AutoTele",
	enabledByDefault = false,
	description = "Makes prayer xp",
	tags = {"has, chaos, chaosaltar, xp"},
	type = PluginType.PVM
)
@Slf4j
public class hasAutoTelePlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasAutoTeleConfiguration config;

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
	private ConfigManager configManager;

	@Inject
	PluginManager pluginManager;


	Instant botTimer;
	WallObject targetWallObject;
	hasAutoTeleState state;
	GameObject targettObject;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	NPC targetNPC;
	int timeToReset;
	LocalPoint beforeLoc;
	Player player;
	boolean teleported;
	WorldPoint altarLocation = new WorldPoint(2948, 3820, 0);
	WorldPoint doorLocation = new WorldPoint(2958, 3820, 0);
	GameObject chaosAltar;


	int timeout;
	int teleHP;
	int tabID;

	int timedOut = 0;
	long sleepLength;
	boolean startChaosAltar;
	boolean usedBones;
	boolean selectOption;
	boolean testFunc;
	int timeRun;
	int doorOption;
	private final Set<Integer> itemIds = new HashSet<>();
	Rectangle clickBounds;
	//boolean clickOnPersonNow;

	@Provides
	hasAutoTeleConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasAutoTeleConfiguration.class);
	}

	private void resetVals() {

		state = null;

		timedOut = 0;
		timedOut = 0;
		botTimer = null;
		skillLocation = null;
		startChaosAltar = false;
	}


	@Override
	protected void startUp()
	{
		teleHP = config.teleHP();
		tabID = config.tabID();
		timeToReset = config.timeToReset();

	log.info("started");
	teleported = false;
	}


	@Override
	protected void shutDown() {
		// runs on plugin shutdown

		log.info("Plugin stopped");
		startChaosAltar = false;
	}





	private long sleepDelay() {
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}




	@Subscribe
	private void onGameTick(GameTick tick) {

		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN){
			int hp = client.getBoostedSkillLevel(Skill.HITPOINTS);

			utils.sendGameMessage(String.valueOf(timeRun));
		//	utils.sendGameMessage(String.valueOf(hp) + " this is your current hp, and the hp to tele at is " + String.valueOf(teleHP));
			if(hp<=teleHP && !teleported && !anyFoodLeft()){
				clickTeleTab();
				teleported = true;
				botTimer = Instant.now();
			}else if(resetTime()){
				teleported = false;
			}
		}


	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN && startChaosAltar) {
			state = TIMEOUT;
			timeout = 2;
		}
	}

	private boolean chatOpen(){



		if(client.getWidget(219,1) == null){
			return false;
		}else{
			return true;
		}
	}


	private void clickTeleTab(){

			WidgetItem tablet = inventory.getWidgetItem(tabID);
			targetMenu = new MenuEntry("BREAK", "BREAK", tabID, 33, 8, 9764864, false);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(tablet.getCanvasLocation(), sleepDelay());

	}

	private boolean resetTime(){
		Duration duration = Duration.between(botTimer, Instant.now());
		timeRun = (int) duration.getSeconds();
		utils.sendGameMessage(String.valueOf(timeRun));
		if(timeRun>config.timeToReset()){
			return true;
		}else{
			return false;
		}
	}

	private boolean anyFoodLeft(){
		if(config.checkIfHaveFood()){
			if(inventory.containsItem(config.foodID())){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}




}