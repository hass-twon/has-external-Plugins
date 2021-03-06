package net.runelite.client.plugins.hasAutoTele;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import java.awt.Rectangle;
///api
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import static net.runelite.api.MenuAction.ITEM_USE_ON_GAME_OBJECT;


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


import static net.runelite.api.MenuAction.ITEM_USE_ON_NPC;
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
	tags = {"has, chaos, chaosaltar, xp"}

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

	boolean checkForReset;
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

			String state = getState();

			switch(state){
				case "TIMEOUT":
					timeout--;
					break;
				case "TELE":
					clickTeleTab();
					teleported = true;
					checkForReset = true;
					botTimer = Instant.now();
					timeout = 3;
					break;

				case "RESET":
					teleported = false;
					checkForReset  = false;
					break;
				case "IDLE":
					break;
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

	private String getState(){
		int hp = client.getBoostedSkillLevel(Skill.HITPOINTS);
		if(timeout>0){
			return "TIMEOUT";
		}

		if(hp<=teleHP && !teleported && !anyFoodLeft()){
			return "TELE";
		}
		if(checkForReset){
			if(resetTime()){
				return "RESET";
			}else{
				timeout =3;
				return "TIMEOUT";
			}

		}
		return "IDLE";
	}


	private void clickTeleTab(){

			WidgetItem tablet = inventory.getWidgetItem(tabID);
			if(tablet != null){
				targetMenu = new MenuEntry("BREAK", "BREAK", tabID, 33, tablet.getIndex(), 9764864, false);
				menu.setEntry(targetMenu);
				mouse.delayMouseClick(tablet.getCanvasLocation(), sleepDelay());

			}else{
				utils.sendGameMessage("no tablet to tele");
			}

	}

	private boolean resetTime(){
		Duration duration = Duration.between(botTimer, Instant.now());
		timeRun = (int) duration.getSeconds();
		if(timeRun>config.timeToReset()){
			return true;
		} else {
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