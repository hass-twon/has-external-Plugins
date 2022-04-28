package net.runelite.client.plugins.hasCrafting;

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
import net.runelite.api.events.*;

import java.awt.Robot;

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


import org.checkerframework.checker.signature.qual.SignatureUnknown;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.sql.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static net.runelite.api.MenuAction.*;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "has-Crafting",
	enabledByDefault = false,
	description = "Makes crafting xp",
	tags = {"has, crafting, craft, gems"}

)
@Slf4j
public class hasCraftingPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private hasCraftingConfiguration config;

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

	long sleepLength;
	int timeout;
	LegacyMenuEntry targetMenu;
	private final Set<Integer> itemIds = new HashSet<>();
	private final Set<Integer> keepItem = Set.of(ItemID.CHISEL);
	Rectangle clickBounds;
	int childID;
	boolean plugStarted;
	//boolean clickOnPersonNow;
	String itemToBuy;
	boolean startClicked = false;
	int logID;
	int fletchValue;
	boolean deposited;
	boolean doAIO;
	@Provides
	hasCraftingConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(hasCraftingConfiguration.class);
	}

	private void resetVals() {

	}


	@Override
	protected void startUp()
	{
		plugStarted = true;

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

		if (!event.getGroup().equalsIgnoreCase("hasCrafting")) {
			return;
		}

		if (event.getKey().equals("startButton")) {
			log.debug("starting");
			startClicked = !startClicked;
			deposited = false;


		} else if (event.getKey().equals("testButton")) {
			System.out.println("runnig test");
			selectCorrectItemToCraft();

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




	@Subscribe
	private void onGameTick(GameTick tick) {
		if(!plugStarted || !startClicked){
			System.out.println("value of plug started is " + plugStarted);
			System.out.println("value of startclied is " + startClicked);
			return;
		}

		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN){
			if (timeout>0){
				timeout--;
				return;
			}




			if(inventory.containsItem(config.craftGem().getUncutID()) && inventory.containsItem(ItemID.CHISEL) && !playerUtils.isAnimating() && !bank.isOpen()){
				System.out.println("Player isnt animating and inventory contians correct items");
				Widget widget = client.getWidget(WidgetInfo.MULTI_SKILL_MENU);
				if (widget != null && !widget.isHidden())
				{
					selectCorrectItemToCraft();
				}else{

				clickToolThenObject();
				}
				timeout = 1;
				return;
			}else if(playerUtils.isAnimating()){
				Widget widget = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);
				if (widget != null && !widget.isHidden())
				{
					clickToolThenObject();
					timeout = 1;
					return;
				}else{
					System.out.println("You are crafting");
					timeout = 1;
					return;
				}

			} else if(!inventory.containsItem(config.craftGem().getUncutID())){
				if(!bank.isOpen()){
					openBank();
					return;
				}
				if(bank.isOpen() && !inventory.containsItem(ItemID.CHISEL)){

					bank.withdrawItem(ItemID.CHISEL);
					return;
				}

				}if(bank.isOpen() && inventory.containsItem(config.craftGem().getUncutID())){
					bank.close();
					return;
				}else if(bank.isOpen() && !inventory.containsItem(config.craftGem().getUncutID()) && !deposited){
					bank.depositAllExcept(keepItem);
					//bank.withdrawAllItem(logID);
				deposited = true;
					return;
				}else if(bank.isOpen() && deposited) {
				if(bank.contains(config.craftGem().getUncutID(),27)){
					bank.withdrawAllItem(config.craftGem().getUncutID());
					deposited = false;
					return;
				}else{

					utils.sendGameMessage("You do not have any availble gems");
					plugStarted = false;
					return;
				}

			}




		}

	}




	private void openBank(){


		System.out.println("Opening GE");
		NPC banker = npc.findNearestNpc(1613,1633,1634,3089);
		if(banker != null){
			int typeValue = 0;
			/*
			switch(banker.getId()){
				case (1613):
					typeValue = 19386;
				case(1633):
					typeValue = 19385;
				case (1634):
					typeValue = 19392;
				case(3089):
					typeValue = 19393;
			}
			 */

			targetMenu = new LegacyMenuEntry("Bank", "<col=ffff00>Banker", banker.getIndex(),NPC_THIRD_OPTION.getId() , 0, 0, false);
			utils.doActionMsTime(targetMenu, new Point(0,0),sleepDelay());
			//utils.doInvokeMsTime(targetMenu,sleepDelay());

		}else{
			utils.sendGameMessage("Cannot find banker");

		}
	}

	private void clickToolThenObject(){
		WidgetItem tool = inventory.getWidgetItem(ItemID.CHISEL);
		WidgetItem rawItem = inventory.getWidgetItem(config.craftGem().getUncutID());
		Widget tool1 = getInventoryItem(ItemID.CHISEL);
		Widget rawM = getInventoryItem(config.craftGem().getUncutID());


		if(rawItem == null|| tool == null){
			System.out.println("one of them is null");
			return;
		}
		setSelectedInventoryItem(tool1);
		targetMenu = new LegacyMenuEntry("","",0,WIDGET_TARGET_ON_WIDGET,rawM.getIndex(),9764864,true);
		utils.doInvokeMsTime(targetMenu,sleepDelay());
		/*
		executorService.submit(() ->
		{
			System.out.println("Trying to click");
			//menu.setModifiedEntry(new MenuEntry("", "", knife.getId(), MenuAction.ITEM_USE_ON_ITEM.getId(), knife.getIndex(), WidgetInfo.INVENTORY.getId(),
			//		false), log.getId(), log.getIndex(), MenuAction.ITEM_USE_ON_ITEM.getId());
			//mouse.click(knife.getCanvasBounds());
			menu.setModifiedEntry(targetMenu = new LegacyMenuEntry("", "", tool.getId(), MenuAction.ITEM_USE_ON_ITEM.getId(), tool.getIndex(), WidgetInfo.INVENTORY.getId(),
							false), rawItem.getId(), rawItem.getIndex(), MenuAction.ITEM_USE_ON_ITEM.getId());
			utils.doInvokeMsTime(targetMenu,sleepDelay());

		});

		 */

	}


	private void setSelectedInventoryItem(Widget item) {
		client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
		client.setSelectedSpellChildIndex(item.getIndex());
		client.setSelectedSpellItemId(item.getItemId());
	}
	private Widget getInventoryItem(int id) {
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
		if (inventoryWidget!=null && !inventoryWidget.isHidden())
		{
			return getWidgetItem(inventoryWidget,id);
		}
		if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
		{
			return getWidgetItem(bankInventoryWidget,id);
		}
		return null;
	}
	private Widget getWidgetItem(Widget widget,int id) {
		for (Widget item : widget.getDynamicChildren())
		{
			if (item.getItemId() == id)
			{
				return item;
			}
		}
		return null;
	}

	private void selectCorrectItemToCraft(){
		//fletchValue = config.craftItem().getIdValue();
		targetMenu = new LegacyMenuEntry("", "", 1,CC_OP.getId() , -1, 17694734, false);
		utils.doInvokeMsTime(targetMenu,sleepDelay());

	}




/*

	public void inventoryItemsCombine(Collection<Integer> ids, int item1ID, int opcode, boolean exceptItems, boolean interactAll, int minDelayBetween, int maxDelayBetween)
	{
		WidgetItem item1 = getInventoryWidgetItem(item1ID);
		if (item1 == null)
		{
			log.info("combine item1 item not found in inventory");
			return;
		}
		Collection<WidgetItem> inventoryItems = getAllInventoryItems();
		executorService.submit(() ->
		{
			try
			{
				iterating = true;
				for (WidgetItem item : inventoryItems)
				{
					if ((!exceptItems && ids.contains(item.getId()) || (exceptItems && !ids.contains(item.getId()))))
					{
						log.info("interacting inventory item: {}", item.getId());
						sleep(minDelayBetween, maxDelayBetween);
						setModifiedMenuEntry(new MenuEntry("", "", item1.getId(), opcode, item1.getIndex(), WidgetInfo.INVENTORY.getId(),
								false), item.getId(), item.getIndex(), MenuAction.ITEM_USE_ON_ITEM.getId());
						click(item1.getCanvasBounds());
						if (!interactAll)
						{
							break;
						}
					}
				}
				iterating = false;
			}
			catch (Exception e)
			{
				iterating = false;
				e.printStackTrace();
			}
		});
	}

 */

	/*


	public void combineItems(Collection<Integer> ids, int item1ID, int opcode, boolean exceptItems, boolean interactAll, int minDelayBetween, int maxDelayBetween)
	{
		WidgetItem item1 = getWidgetItem(item1ID);
		if (item1 == null)
		{
			log.info("combine item1 item not found in inventory");
			return;
		}
		Collection<WidgetItem> inventoryItems = getAllItems();
		executorService.submit(() ->
		{
			try
			{
				iterating = true;
				for (WidgetItem item : inventoryItems)
				{
					if ((!exceptItems && ids.contains(item.getId()) || (exceptItems && !ids.contains(item.getId()))))
					{
						log.info("interacting inventory item: {}", item.getId());
						sleep(minDelayBetween, maxDelayBetween);
						menu.setModifiedEntry(new MenuEntry("", "", item1.getId(), opcode, item1.getIndex(), WidgetInfo.INVENTORY.getId(),
							false), item.getId(), item.getIndex(), MenuAction.ITEM_USE_ON_ITEM.getId());
						mouse.click(item1.getCanvasBounds());
						if (!interactAll)
						{
							break;
						}
					}
				}
				iterating = false;
			}
			catch (Exception e)
			{
				iterating = false;
				e.printStackTrace();
			}
		});
	}
	 */

	private void typeItemName(){
		key.typeString(itemToBuy);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event){
		if (event.getCommand().equalsIgnoreCase("fletch")) clickToolThenObject();
		if (event.getCommand().equalsIgnoreCase("select")) selectCorrectItemToCraft();
	}


}