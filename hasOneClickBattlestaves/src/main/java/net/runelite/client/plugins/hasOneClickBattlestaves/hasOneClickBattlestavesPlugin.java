package net.runelite.client.plugins.hasOneClickBattlestaves;

import com.google.inject.Provides;
import io.reactivex.rxjava3.annotations.Nullable;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
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

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-OneClickBattlestaves",
	enabledByDefault = false,
	description = "One click battlesaves",
	tags = {"has, battlestaves, has-one, has-craft, one-click"}
)

@Slf4j
public class hasOneClickBattlestavesPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasOneClickBattlestavesConfiguration config;

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
	Boolean twoRunsBeforeButlerArrives;
	int bankID;
	int stoveId;
	int timeout;
	private String state;
	int ticksOnWaitingForMenu;
	int ticksIdle;

	int levelsDone;




	@Provides
	hasOneClickBattlestavesConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasOneClickBattlestavesConfiguration.class);
	}

	private void resetVals() {
		state = "DETERMINE_STATE";
		timeout = 0;
		ticksOnWaitingForMenu = 0;
		ticksIdle = 0;
	}


	@Override
	protected void startUp()
	{
		resetVals();
	}




	@Override
	protected void shutDown() {
		// runs on plugin shutdown
		startClicked = false;
		plugStarted = false;
		log.info("Plugin stopped");
		utils.sendGameMessage(state + " stopped");

	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event) {

		if (!event.getGroup().equalsIgnoreCase("hasOneClickBattlestaves")) {
			return;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (timeout > 0)
		{
			timeout--;
		}
		if(playerUtils.isAnimating()){
			ticksIdle = 0;
		}else if(!playerUtils.isAnimating() && ticksIdle < config.idleTick() && !bank.isOpen()){
			ticksIdle++;
		}else if(ticksIdle >= config.idleTick()){
			state = "DETERMINE_STATE";
			resetVals();
		}

	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		String text;

		if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
			return;
		else if (client.getLocalPlayer().getAnimation() == 7531 || client.getLocalPlayer().getAnimation() == 7529 || client.getLocalPlayer().getAnimation() == config.animationID())
		{
			text = "<col=00ff00>Craf-Ting";
		}
		else
		{
			text = "<col=00ff00>One Click Crafting";
		}
		this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
				.getId(), 0, 0, 0, true);
		//Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
		client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
	{
		if (event.getMenuOption().equals("<col=00ff00>One Click Whining"))
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
				|| client.getLocalPlayer().getAnimation() == 7531 || client.getLocalPlayer().getAnimation() == 7529 )
				& !isBankOpen()) //for some reason it consumes the first click at the bank?
		{
			log.debug("Consume event because not idle?");
			event.consume();
			return;
		}
		System.out.println(state + " is State!");


		switch (state)
		{
			case "DETERMINE_STATE":
				if(inventory.containsItemAmount(config.item1ID(),1,false,false) && inventory.containsItemAmount(config.item2ID(),1,false,false)){
					state = "CRAFT";
				}else{
					state = "OPEN_BANK";
				}
				break;

			case "OPEN_BANK":
				event.setMenuEntry(getBankMES());
				state = "DEPOSIT_ALL";
				break;
			case "DEPOSIT_ALL":
				if(bank.isOpen()){
					event.setMenuEntry(depositLoot());
					state = "WITHDRAW_ITEM1";
					timeout += 1;
					break;
				}else{
					return;
				}

			case "WITHDRAW_ITEM1":
				if(!bank.contains(config.item1ID(),14)){
					state = "STOP";
					break;
				}
				event.setMenuEntry(withdrawNeeded(config.item1ID()));
				state = "WITHDRAW_ITEM2";
				timeout += 1;
				break;

			case "WITHDRAW_ITEM2":
				if(!bank.contains(config.item2ID(),14)){
					state = "STOP";
					break;
				}
				event.setMenuEntry(withdrawNeeded(config.item2ID()));
				state = "CRAFT";
				timeout += 1;
				break;
			case "CRAFT":
				if(!inventory.containsItem(config.item1ID())){
					state = "OPEN_BANK";
					break;
				}
				Widget skillMenu = client.getWidget(270,0);
				if(skillMenu != null){
					state = "SELECT_OPTION2";
					break;
				}
				event.setMenuEntry(useItem1OnItem2());
				timeout += 1;
				state = "SELECT_OPTION2";
				break;
			case "SELECT_OPTION2":
				Widget skillMenus = client.getWidget(270,0);
				if(skillMenus == null){
					return;
				}
				event.setMenuEntry(selectSkillMenu());
				state = "CRAFT";
				timeout += 15;
				break;
			case "STOP":
				shutDown();
				break;


		}




	}

	private MenuEntry selectSkillMenu()
	{

		return createMenuEntry(
				1,
				CC_OP,
				-1,
				17694734,
				false);
	}

	private MenuEntry useItem1OnItem2()
	{



		client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
		client.setSelectedItemSlot(getInventoryItem(config.item1ID()).getIndex());
		client.setSelectedItemID(config.item1ID());


		return createMenuEntry(
				config.item2ID(),
				ITEM_USE_ON_WIDGET_ITEM,
				inventory.getWidgetItem(config.item2ID()).getIndex(),
				WidgetInfo.INVENTORY.getId(),
				false);
	}

	private MenuEntry withdrawNeeded(int item)
	{

		return createMenuEntry(
				1,
				MenuAction.CC_OP_LOW_PRIORITY,
				getBankIndex(item),
				786445,
				false);
	}

	private MenuEntry getBankMES()
	{

		WallObject bankT = object.findNearestWallObject(10060);
		return createMenuEntry(
				bankT.getId(),
				GAME_OBJECT_SECOND_OPTION,
				getLocation(bankT).getX(),
				getLocation(bankT).getY(),
				true);
	}

	private MenuEntry depositLoot(){


		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				786474,
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


