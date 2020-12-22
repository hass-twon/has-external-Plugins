package net.runelite.client.plugins.hasChaosAltar;

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
import static net.runelite.client.plugins.hasChaosAltar.hasChaosAltarState.*;




import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-chaosAltar",
	enabledByDefault = false,
	description = "Makes prayer xp",
	tags = {"has, chaos, chaosaltar, xp"},
	type = PluginType.SKILLING
)
@Slf4j
public class hasChaosAltarPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasChaosAltarConfiguration config;

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



	WallObject targetWallObject;
	hasChaosAltarState state;
	GameObject targettObject;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	NPC targetNPC;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	WorldPoint altarLocation = new WorldPoint(2948, 3820, 0);
	WorldPoint doorLocation = new WorldPoint(2958, 3820, 0);
	GameObject chaosAltar;

	int boneID;
	int timeout;
	int notedBoneID;
	int timedOut = 0;
	long sleepLength;
	boolean startChaosAltar;
	boolean usedBones;
	boolean selectOption;
	boolean testFunc;
	private final Set<Integer> itemIds = new HashSet<>();
	Rectangle clickBounds;
	//boolean clickOnPersonNow;

	@Provides
	hasChaosAltarConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasChaosAltarConfiguration.class);
	}

	private void resetVals() {

		state = null;
		timedOut = 0;
		timedOut = 0;
		botTimer = null;
		skillLocation = null;
		startChaosAltar = false;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("hasChaosAltar")) {
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startChaosAltar) {
				timedOut = 0;
				selectOption = false;
				startChaosAltar = true;
				usedBones = false;
				state = null;
				targetMenu = null;
				//clickOnPersonNow = false;
				botTimer = Instant.now();
				setLocation();
				boneID = config.getBoneType().getId();
				notedBoneID = config.getBoneType().getNotedID();


			//	utils.sendGameMessage("bone id is " + String.valueOf(boneID) + "noted bone id is " + String.valueOf(notedBoneID));

			} else {
				resetVals();
			}
		} else if(configButtonClicked.getKey().equals("testButton")){
testFunc = true;
		}
	}

	@Override
	protected void shutDown() {
		// runs on plugin shutdown

		log.info("Plugin stopped");
		startChaosAltar = false;
	}



	public void setLocation() {
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN)) {
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		} else {
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
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




	private Point getRandomNullPoint() {
		if (client.getWidget(161, 34) != null) {
			Rectangle nullArea = client.getWidget(161, 34).getBounds();
			return new Point((int) nullArea.getX() + calc.getRandomIntBetweenRange(0, nullArea.width), (int) nullArea.getY() + calc.getRandomIntBetweenRange(0, nullArea.height));
		}

		return new Point(client.getCanvasWidth() - calc.getRandomIntBetweenRange(0, 2), client.getCanvasHeight() - calc.getRandomIntBetweenRange(0, 2));
	}

	public hasChaosAltarState getState() {
		if (timeout > 0) {
			return TIMEOUT;
		}
		if (playerUtils.isMoving(beforeLoc)) {
			timeout = 1 + tickDelay();
			return MOVING;
		}

		if(inventory.containsItem(boneID) && client.getLocalPlayer().getWorldLocation().equals(altarLocation) && inventory.containsItem(notedBoneID)){
			return ATALTAR;
		}
		if(inventory.containsItem(boneID) && !client.getLocalPlayer().getWorldLocation().equals(altarLocation) && inventory.containsItem(notedBoneID)){
			return WALKTOALTAR;
		}

		if(chatOpen()){
			return SELECTCHOICE;
		}
	//	if(clickOnPersonNow){
	//		return DUDE;
	//	}
		if(usedBones = true && isDoorOpen() && inventory.containsItem(notedBoneID)){
		return CLICKDUDE;
		}
		if(usedBones = true && !isDoorOpen() && inventory.containsItem(notedBoneID)){
			return CLICKDOOR;
		}
		if( usedBones && timedOut>=15){
			if(isDoorOpen()){
				timedOut = 0;
				return CLICKDUDE;
			}else{
				timedOut =0;
				return  CLICKDOOR;
			}

		}

		if(!inventory.containsItem(boneID) && inventory.containsItem(notedBoneID) && client.getLocalPlayer().getWorldLocation().equals(altarLocation)){
			usedBones = true;
			timeout = 1;
			timedOut = 0;
			return TIMEOUT;
		}

		if(!inventory.containsItem(boneID) && !inventory.containsItem(notedBoneID)){
			return FINISHED;
		}






		return IDLE;
	}

	@Subscribe
	private void onGameTick(GameTick tick) {
		if(testFunc == true){
			testFunc();
		}
		if (!startChaosAltar) {
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null ) {
			if (!client.isResized()) {
				utils.sendGameMessage("Client must be set to resizable");
				startChaosAltar = false;
				return;
			}
			timedOut++;
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state) {
				case TIMEOUT:
					playerUtils.handleRun(30, 20);
					timeout--;
					break;
				case WALKTOALTAR:
					walk.sceneWalk(altarLocation,0,sleepDelay());
					timeout = 3;
					break;
				case MOVING:
					playerUtils.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case ATALTAR:
					//utils.sendGameMessage("We are here");
					useBonesOnAltar();
					break;
				case FINISHED:
					utils.sendGameMessage("You have no bones brokeass");
					startChaosAltar = false;
					break;
				case CLICKDUDE:
					utils.sendGameMessage("clicking on dude");
				//	selectBones();

					unNoteBones();
					selectOption = true;
					//walk.sceneWalk(dudeLocaiton,0,sleepDelay());
					break;
				case CLICKDOOR:
					openDoor();
					timeout =2;
					break;


				case DUDE:
					unNoteBones();
				//	clickOnPersonNow = false;
					selectOption = true;
					timeout = 2;
					break;
				case SELECTCHOICE:
					if(chatOpen()){
						targetNPC = npc.findNearestNpc(7995);
					//utils.sendGameMessage("chat is opne so select an option");
					targetMenu = new MenuEntry("Continue","",0,30,3,14352385,false);
					menu.setEntry(targetMenu);
					mouse.delayMouseClick(targetNPC.getConvexHull().getBounds(),sleepDelay());
					selectOption = false;
					usedBones = false;
					break;
					}else{
						//utils.sendGameMessage("chat not open yet");
					}
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

	private boolean chatOpen(){



		if(client.getWidget(219,1) == null){
			return false;
		}else{
			return true;
		}
	}
	private void withdrawItems() {
		if (inventory.isEmpty() && bank.isOpen())
		{
			withdrawX(1783);
			timeout=1+tickDelay();
		}
		else
		{
			if(inventory.containsItem(ItemID.BUCKET_OF_SAND))
			withdrawX(1781);
		}
	}

	private void useBonesOnAltar(){
		WidgetItem bones = inventory.getWidgetItem(ItemID.BIG_BONES);
		chaosAltar = object.findNearestGameObject(411);

		if(bones != null){

			targetMenu = new MenuEntry("", "", chaosAltar.getId(), ITEM_USE_ON_GAME_OBJECT.getId(),
					chaosAltar.getSceneMinLocation().getX(), chaosAltar.getSceneMinLocation().getY(), false);
			utils.doModifiedActionMsTime(targetMenu, bones.getId(), bones.getIndex(), ITEM_USE_ON_GAME_OBJECT.getId(), chaosAltar.getConvexHull().getBounds(), sleepDelay());

		}

	}

	private void openDoor(){
		targettObject = object.findNearestGameObject(566);
		Point random = new Point(0,0);
		int randtest = calc.getRandomIntBetweenRange(0,1);
		//utils.sendGameMessage(String.valueOf(randtest));
		if(randtest==1){
			targetMenu = new MenuEntry("Open","<col=ffff>Large door",1521,3,62,52,false);
		}else{
			targetMenu = new MenuEntry("Open","<col=ffff>Large door",1521,3,54,52,false);
		}


		menu.setEntry(targetMenu);
		//mouse.click(random);
		mouse.delayMouseClick(random,sleepDelay());
		//mouse.delayMouseClick(targettObject.getConvexHull().getBounds(),sleepDelay());
	}

	private void unNoteBones() {
		NPC Phials = npc.findNearestNpc(7995);
		if (Phials != null) {
		//	utils.sendGameMessage("phials not null");
			targetMenu = new MenuEntry("", "", 14865, 7, 0, 0, false);
			menu.setModifiedEntry(targetMenu, 533, inventory.getWidgetItem(533).getIndex(), 7);
			mouse.delayMouseClick(Phials.getConvexHull().getBounds(), sleepDelay());
		}
		/*
		NPC druid = npc.findNearestNpc(7995);
		if (druid != null) {
			//targetMenu = new MenuEntry("", "<col=ff9040>Big bones<col=ffffff>-><col=ffff00>Elder Chaos druid", 1895, 7, 0, 0, false);
		//	menu.setEntry(targetMenu);
			//mouse.delayMouseClick(druid.getConvexHull().getBounds(),sleepDelay());
			//selectOption = true;
			//menu.setModifiedEntry(targetMenu, NOTE_ID, inventory.getWidgetItem(NOTE_ID).getIndex(), 7);
			//mouse.delayMouseClick(Phials.getConvexHull().getBounds(), sleepDelay());
			//targetMenu = new MenuEntry("","", 1895, 7, 0, 0, false);
			//menu.setModifiedEntry(targetMenu, notedBoneID, inventory.getWidgetItem(notedBoneID).getIndex(), 7);
			//mouse.delayMouseClick(druid.getConvexHull().getBounds(), sleepDelay());
			//targetMenu = new MenuEntry("Talk-to", "<col=ffff00>Elder Chaos Druid Templeton", druid.getIndex(), 9,0,0, false);
			//menu.setEntry(targetMenu);
			//mouse.delayMouseClick(druid.getConvexHull().getBounds(), sleepDelay());
			WidgetItem bonez = inventory.getWidgetItem(notedBoneID);
			if (bonez != null)
			{
				targetMenu = new MenuEntry("", "", druid.getId(), ITEM_USE_ON_NPC.getId(),
						0, 0, false);
			//	utils.doNpcActionMsTime(targetMenu, bonez.getId(), bonez.getIndex(), ITEM_USE_ON_NPC.getId(), druid.getConvexHull(), sleepDelay());
				utils.doActionMsTime(targetMenu,druid.getConvexHull().getBounds(),sleepDelay());
				timeout = tickDelay();
			}else{
				utils.sendGameMessage("bonez are null");
			}

			return;
		}else{
			utils.sendGameMessage("druid is null");
		}
*/

	}

	private boolean isDoorOpen(){
		targetWallObject = object.findWallObjectWithin(doorLocation,0,1521);
		if(targetWallObject == null){
			return true;
		}else{
			return false;
		}
	}

	private void testFunc(){

//utils.sendGameMessage(String.valueOf(randtest));
	}
	private void selectBones(){
		/*
		WidgetItem bonez = inventory.getWidgetItem(notedBoneID);
		targetMenu = new MenuEntry("Use", "Use", 533, MenuOpcode.ITEM_USE.getId(),
				bonez.getIndex(), 9764864, false);
		menu.setEntry(targetMenu);
		mouse.delayMouseClick(bonez.getCanvasBounds(),sleepDelay());
	*/
	}

	private void depositItems() {
		if (inventory.isFull() && bank.isOpen())
			bank.depositAll();
	}
	private void makeGlass() {
		utils.doActionMsTime(new MenuEntry("Make", "<col=ff9040>Molten glass</col>", 1, 57, -1, 17694734, false), client.getWidget(270,14).getBounds(), sleepDelay());
		{
			mouse.getClickPoint(client.getWidget(270,14).getBounds());
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event){
		log.info(event.toString());
	}
	private void withdrawX(int ID){
		if(client.getVarbitValue(3960)!=14){
			bank.withdrawItemAmount(ID,14);
			timeout+=3;
		} else {
			targetMenu = new MenuEntry("", "", (client.getVarbitValue(6590) == 3) ? 1 : 5, MenuOpcode.CC_OP.getId(), bank.getBankItemWidget(ID).getIndex(), 786444, false);
			menu.setEntry(targetMenu);
			clickBounds = bank.getBankItemWidget(ID).getBounds()!=null ? bank.getBankItemWidget(ID).getBounds() : new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
			mouse.delayMouseClick(clickBounds,sleepDelay());
		}
	}
}