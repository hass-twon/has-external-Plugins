package net.runelite.client.plugins.hasBirdHouseRun;

import com.google.inject.Provides;
import io.reactivex.rxjava3.annotations.Nullable;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.iutils.*;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static net.runelite.api.MenuAction.*;

// Many functions borrowed from https://github.com/Magnusrn/Plugins
@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-BirdHouseRun",
	enabledByDefault = false,
	description = "One click bird house runs",
	tags = {"has, birdhouse, has-birdhouse, run"}

)
@Slf4j
public class hasBirdHouseRunPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasBirdHouseRunConfiguration config;

	@Inject
	private iUtils utils;

	@Inject
	private ActionQueue action;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ExecutorService executorService;

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

	Player player;
	Boolean plugStarted;
	Boolean startClicked = false;
	int timeout;
	Long sleepLength;
	LegacyMenuEntry targetMenu;
	int hopSeedID;
	int logID;
	private String state = "WEAR_PENDANT";
	int birdhouse_gameobjID = 30568;
	int birdhouse_id;

	GameObject ourBank;


	@Provides
	hasBirdHouseRunConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasBirdHouseRunConfiguration.class);
	}

	private void resetVals() {
		state = "WEAR_PENDANT";
		timeout = 0;
	}


	@Override
	protected void startUp()
	{
		resetVals();
		determineBirdHouseID();
	}

	private void determineBirdHouseID() {
		//Regular
		if(config.logID() == 1511){
			birdhouse_id = 21512;
		}else if(config.logID() == 1521){//Oak
			birdhouse_id = 21515;
		}else if(config.logID() == 1519){//Willow
			birdhouse_id = 21518;
		}else if(config.logID() == 6333){//Teak
			birdhouse_id = 21521;
		}else if(config.logID() == 1517){//Maple
			birdhouse_id = 22192;
		}else if(config.logID() == 6332){//Mahogany
			birdhouse_id = 22195;
		}else if(config.logID() == 1515){//Yew
			birdhouse_id = 22198;
		}else if(config.logID() == 1513){//Magic
			birdhouse_id = 22201;
		}else if(config.logID() == 19669){//Redwood
			birdhouse_id = 22204;
		}
	}


	@Override
	protected void shutDown() {
		// runs on plugin shutdown
		startClicked = false;
		plugStarted = false;
		log.info("Plugin stopped");

	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event) {

		if (!event.getGroup().equalsIgnoreCase("hasBirdHouseRun")) {
			return;
		}

		if (event.getKey().equals("startButton")) {
			startClicked = !startClicked;
			hopSeedID = config.seedID();
			logID = config.logID();
		}
	}












	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (timeout > 0)
		{
			timeout--;
		}
	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		String text;

		if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
			return;
		else if (client.getLocalPlayer().getAnimation() == 791)
		{
			text = "<col=00ff00>Bird-Running";
		}
		else
		{
			text = "<col=00ff00>One Click BirdHouse";
		}
		this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
				.getId(), 0, 0, 0, true);
		//Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
		client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
	{
		if (event.getMenuOption().equals("<col=00ff00>One Click BirdHouse"))
			System.out.println(state + " is State!");
			handleClick(event);
	}


	private void handleClick(MenuOptionClicked event)
	{

		if (timeout != 0)
		{
			log.debug("Consuming event because timeout is not 0");
			event.consume();
			return;

		}
		if ((client.getLocalPlayer().isMoving()
				|| client.getLocalPlayer().getPoseAnimation()
				!= client.getLocalPlayer().getIdlePoseAnimation()
				|| client.getLocalPlayer().getAnimation() == 791)
				& !isBankOpen()) //for some reason it consumes the first click at the bank?
		{
			log.debug("Consume event because not idle?");
			event.consume();
			return;
		}


		log.debug("state = " + state);
		switch (state)
		{
			case "WEAR_PENDANT":
				if (playerUtils.isItemEquipped(Set.of(ItemID.DIGSITE_PENDANT_1,ItemID.DIGSITE_PENDANT_2,ItemID.DIGSITE_PENDANT_3,ItemID.DIGSITE_PENDANT_4,ItemID.DIGSITE_PENDANT_5))){
					state = "TP_FOSSIL";
					return;
				}
				event.setMenuEntry(wearDigsitePendant());
				state = "TP_FOSSIL";
				break;
			case "TP_FOSSIL":

				event.setMenuEntry(castFossilTeleport());
				timeout += 3;
				state = "TOUCH_SHROOMS";
				break;
			case "TOUCH_SHROOMS":
				if(object.findNearestGameObject(30920) == null){
					timeout += 1;
					return;
				}
				event.setMenuEntry(treeToucher());
				timeout += 2;
				state = "SELECT_TPLOCATION";
				break;
			case "SELECT_TPLOCATION":
				Widget tpLocations = client.getWidget(608,1);
				if(tpLocations == null){
					timeout += 1;
					return;
				}
				event.setMenuEntry(goValley());
				timeout += 4;
				state = "FIRST_BIRDHOUSE";
				break;
			case "FIRST_BIRDHOUSE":
				if(object.findNearestGameObject(30568) == null){
					timeout += 4;
					return;
				}
				event.setMenuEntry(emptyBirdHouse(30568));
				timeout +=2;
				state = "SELECT_BIRDHOUSE1";
				break;
			case "SELECT_BIRDHOUSE1":
				event.setMenuEntry(selectItem(ItemID.CLOCKWORK));
				state = "REMAKE_BIRDHOUSE1";
				break;
			case "REMAKE_BIRDHOUSE1":
				if(!inventory.containsItem(ItemID.CLOCKWORK)){
					timeout +=1;
					return;
				}
				event.setMenuEntry(useClockWorkOnLog());
				timeout += 2;
				state = "PLACE_BIRDHOUSE1";
				break;
			case "PLACE_BIRDHOUSE1":
				if(!inventory.containsItem(birdhouse_id)){
					timeout += 1;
					return;
				}
				event.setMenuEntry(buildHouse(30568));
				timeout += 2;
				state = "SELECT_SEED1";
				break;
			case "SELECT_SEED1":
				event.setMenuEntry(selectItem(config.seedID()));
				state = "ADD_SEED1";
				break;
			case "ADD_SEED1":
				event.setMenuEntry(addSeeds(30568));
				timeout += 1;
				state = "WALK_TO_2";
				break;
			case "WALK_TO_2":
				walkTo1();
				timeout += 3;
				state = "SECOND_BIRDHOUSE";
				break;
			case "SECOND_BIRDHOUSE":
				event.setMenuEntry(emptyBirdHouse(30567));
				timeout +=2;
				state = "SELECT_BIRDHOUSE2";
				break;
			case "SELECT_BIRDHOUSE2":
				event.setMenuEntry(selectItem(ItemID.CLOCKWORK));
				state = "REMAKE_BIRDHOUSE2";
				break;
			case "REMAKE_BIRDHOUSE2":
				if(!inventory.containsItem(ItemID.CLOCKWORK)){
					timeout +=1;
					return;
				}
				event.setMenuEntry(useClockWorkOnLog());
				timeout += 2;
				state = "PLACE_BIRDHOUSE2";
				break;
			case "PLACE_BIRDHOUSE2":
				if(!inventory.containsItem(birdhouse_id)){
					timeout += 1;
					return;
				}
				event.setMenuEntry(buildHouse(30567));
				timeout += 2;
				state = "SELECT_SEED2";
				break;
			case "SELECT_SEED2":
				event.setMenuEntry(selectItem(config.seedID()));
				state = "ADD_SEED2";
				break;
			case "ADD_SEED2":
				event.setMenuEntry(addSeeds(30567));
				timeout += 2;
				state = "CLICK_SHROOM";
				break;
			case "CLICK_SHROOM":
				event.setMenuEntry(treeToucher2());
				timeout +=1;
				state = "WAIT_TP_2";
				break;
			case "WAIT_TP_2":
				Widget tpLocations2 = client.getWidget(608,1);
				if(tpLocations2 == null){
					timeout += 1;
					return;
				}
				event.setMenuEntry(goMeadow());
				timeout += 2;
				state = "THIRD_BIRDHOUSE";
				break;
			case "THIRD_BIRDHOUSE":
				GameObject boy_three = object.findNearestGameObject(30565);
				if(boy_three == null){
					timeout += 2;
					return;
				}
				event.setMenuEntry(emptyBirdHouse(30565));
				timeout += 2;
				state = "SELECT_BIRDHOUSE3";
				break;
			case "SELECT_BIRDHOUSE3":
				if(!inventory.containsItem(ItemID.CLOCKWORK)){
					state = "THIRD_BIRDHOUSE";
					timeout +=2;
					return;
				}
				event.setMenuEntry(selectItem(ItemID.CLOCKWORK));
				state = "REMAKE_BIRDHOUSE3";
				break;
			case "REMAKE_BIRDHOUSE3":
				event.setMenuEntry(useClockWorkOnLog());
				state = "PLACE_BIRDHOUSE3";
				timeout += 2;
				break;
			case "PLACE_BIRDHOUSE3":
				if(!inventory.containsItem(birdhouse_id)){
					timeout += 1;
					return;
				}
				event.setMenuEntry(buildHouse(30565));
				timeout += 3;
				state = "SELECT_SEED3";
				break;
			case "SELECT_SEED3":
				event.setMenuEntry(selectItem(config.seedID()));
				state = "ADD_SEED3";
				break;
			case "ADD_SEED3":
				event.setMenuEntry(addSeeds(30565));
				timeout += 2;
				state = "WALK_TO_4p1";
				break;
			case "WALK_TO_4p1":
				walkTo4P1();
				timeout += 4;
				state = "WALK_TO_4p2";
				break;
			case "WALK_TO_4p2":
				walkTo4P2();
				timeout += 4;
				state = "LAST_BIRDHOUSE";
				break;
			case "LAST_BIRDHOUSE":

				event.setMenuEntry(emptyBirdHouse(30566));
				timeout += 2;
				state = "SELECT_BIRDHOUSE4";
				break;
			case "SELECT_BIRDHOUSE4":
				if(!inventory.containsItem(ItemID.CLOCKWORK)){
					timeout+=2;
					return;
				}

				event.setMenuEntry(selectItem(ItemID.CLOCKWORK));
				state = "REMAKE_BIRDHOUSE4";
				break;
			case "REMAKE_BIRDHOUSE4":
				event.setMenuEntry(useClockWorkOnLog());
				state = "PLACE_BIRDHOUSE4";
				break;
			case "PLACE_BIRDHOUSE4":
				if(!inventory.containsItem(birdhouse_id)){
					timeout += 1;
					return;
				}
				event.setMenuEntry(buildHouse(30566));
				timeout += 3;
				state = "SELECT_SEED4";
				break;
			case "SELECT_SEED4":
				event.setMenuEntry(selectItem(config.seedID()));
				state = "ADD_SEED4";
				break;
			case "ADD_SEED4":
				event.setMenuEntry(addSeeds(30566));
				timeout += 2;
				state = "FINISHED";
				break;
			case "FINISHED":
				utils.sendGameMessage("FINISHED");



		}



	}

	private void walkTo4P1(){
		WorldPoint worldpoint = new WorldPoint(3682,3856,0);
		walkTile(worldpoint);
	}
	private void walkTo4P2(){
		WorldPoint worldpoint = new WorldPoint(3682,3831,0);
		walkTile(worldpoint);
	}

	private MenuEntry goMeadow(){

		return createMenuEntry(
				0,
				WIDGET_TYPE_6,
				-1,
				39845904,
				false);
	}

	private MenuEntry treeToucher2(){
		GameObject shroom = object.findNearestGameObject(30924);
		return createMenuEntry(
				30924,
				GAME_OBJECT_FIRST_OPTION,
				getLocation(shroom).getX(),
				getLocation(shroom).getY(),
				true);
	}

	private void walkTo1(){
		WorldPoint worldpoint = new WorldPoint(3768,3760,0);
		walkTile(worldpoint);
	}

	private MenuEntry addSeeds(int groundid){
		GameObject birdhouse = object.findNearestGameObject(groundid);
		return createMenuEntry(
				birdhouse.getId(),
				ITEM_USE_ON_GAME_OBJECT,
				getLocation(birdhouse).getX(),
				getLocation(birdhouse).getY(),
				true);
	}

	private MenuEntry selectItem(int itemID){
		WidgetItem sd = inventory.getWidgetItem(itemID);
		return createMenuEntry(
				sd.getId(),
				ITEM_USE,
				sd.getIndex(),
				9764864,
				true);
	}


	private MenuEntry buildHouse(int groundid){
		GameObject birdhouse = object.findNearestGameObject(groundid);
		return createMenuEntry(
				birdhouse.getId(),
				GAME_OBJECT_FIRST_OPTION,
				getLocation(birdhouse).getX(),
				getLocation(birdhouse).getY(),
				true);
	}
	private MenuEntry useClockWorkOnLog(){
		return createMenuEntry(
				config.logID(),
				MenuAction.ITEM_USE_ON_WIDGET_ITEM,
				inventory.getWidgetItem(config.logID()).getIndex(),
				9764864,
				false);
	}

	private MenuEntry emptyBirdHouse(int birdhID){
		GameObject birdhouse = object.findNearestGameObject(birdhID);
		return createMenuEntry(
				birdhID,
				GAME_OBJECT_THIRD_OPTION,
				getLocation(birdhouse).getX(),
				getLocation(birdhouse).getY(),
				true);
	}

	private MenuEntry goValley(){

		return createMenuEntry(
				0,
				WIDGET_TYPE_6,
				-1,
				39845896,
				false);
	}

	private MenuEntry treeToucher(){
		GameObject shroom = object.findNearestGameObject(30920);
		return createMenuEntry(
				30920,
				GAME_OBJECT_FIRST_OPTION,
				getLocation(shroom).getX(),
				getLocation(shroom).getY(),
				true);
	}

	private MenuEntry wearDigsitePendant(){
		WidgetItem dg = inventory.getWidgetItem(Set.of(ItemID.DIGSITE_PENDANT_1,ItemID.DIGSITE_PENDANT_2,ItemID.DIGSITE_PENDANT_3,ItemID.DIGSITE_PENDANT_4,ItemID.DIGSITE_PENDANT_5));

		return createMenuEntry(
				dg.getId(),
				ITEM_SECOND_OPTION,
				dg.getIndex(),
				WidgetInfo.INVENTORY.getId(),
				true);
	}

	private MenuEntry castFossilTeleport(){
		return createMenuEntry(
				3,
				CC_OP,
				-1,
				WidgetInfo.EQUIPMENT_AMULET.getId(),
				false);
	}
	private MenuEntry getBankMES()
	{

		GameObject bankT = object.findNearestBank();
		return createMenuEntry(
				bankT.getId(),
				MenuAction.of(bank.getBankMenuOpcode(bankT.getId())),
				0,
				0,
				false);
	}
	private MenuEntry depositAll()
	{
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				786474,
				true);
	}

	private MenuEntry withDrawLogs()
	{
		int logToGet = config.logID();
		return createMenuEntry(
				7,
				MenuAction.CC_OP_LOW_PRIORITY,
				getBankIndex(logToGet),
				786445,
				false);
	}


	public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick)
	{
		return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
				.setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
	}

	private boolean isBankOpen()
	{
		return client.getItemContainer(InventoryID.BANK) != null;
	}

	private int getBankIndex(int id)
	{
		WidgetItem bankItem = new BankItemQuery()
				.idEquals(id)
				.result(client)
				.first();
		return bankItem.getWidget().getIndex();
	}

	@Nullable
	private Collection<WidgetItem> getInventoryItems()
	{
		Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

		if (inventory == null)
		{
			return null;
		}

		return new ArrayList<>(inventory.getWidgetItems());
	}

	private Point getLocation(TileObject tileObject)
	{
		if (tileObject == null)
		{
			return new Point(0, 0);
		}
		if (tileObject instanceof GameObject)
			return ((GameObject) tileObject).getSceneMinLocation();
		return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
	}

	private WidgetItem getInventoryItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}

	private int getEmptySlots()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return 28 - inventoryWidget.getWidgetItems().size();
		}
		else
		{
			return -1;
		}
	}
	private void walkTile(WorldPoint worldpoint) {
		int x = worldpoint.getX() - client.getBaseX();
		int y = worldpoint.getY() - client.getBaseY();
		RSClient rsClient = (RSClient) client;
		rsClient.setSelectedSceneTileX(x);
		rsClient.setSelectedSceneTileY(y);
		rsClient.setViewportWalking(true);
		rsClient.setCheckClick(false);
	}


}


